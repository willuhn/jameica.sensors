/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/RuleProcessor.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/03/01 23:51:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import de.willuhn.io.FileFinder;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.sensors.notify.notifier.Notifier;
import de.willuhn.jameica.sensors.notify.operator.Operator;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;

/**
 * Durchsucht das Workverzeichnis nach Notify-Regeln in Form von XML-Dateien
 * und verarbeitet diese.
 */
public class RuleProcessor
{
  private static Settings settings = new Settings(RuleProcessor.class);
  
  private Measurement measurement = null;
  
  /**
   * ct.
   * @param m die Messung.
   */
  public RuleProcessor(Measurement m)
  {
    this.measurement = m;
  }
  
  /**
   * Fuehrt die Regelverarbeitung fuer die uebergebene Messung durch.
   */
  public void process()
  {
    List<Rule> rules = this.findRules();
    for (Rule r:rules)
    {
      Sensor s = null;
      try
      {
        s = findSensor(r.getSensor());
        if (s == null)
          continue;
        
        handleRule(s,r);
      }
      catch (Exception e)
      {
        if (s != null)
          Logger.error("error while processing notify rule for sensor " + s.getUuid(),e);
        else
          Logger.error("error while processing notify rule",e);
      }
    }
  }
  
  /**
   * Bearbeitet die Benachrichtigungsregel.
   * @param s der Sensor.
   * @param r die Regel.
   * throws Exception
   */
  private void handleRule(Sensor s,Rule r) throws Exception
  {
    ////////////////////////////////////////////////////////////////////////
    // NULL-Checks
    String limit = getLimit(r);
    if (limit == null || limit.length() == 0)
    {
      Logger.warn("rule for sensor " + r.getSensor() + " has no limit");
      return;
    }
    
    Notifier n = r.getNotifier();
    if (n == null)
    {
      Logger.warn("rule for sensor " + r.getSensor() + " has no notifier");
      return;
    }
    
    Operator o = r.getOperator();
    if (o == null)
    {
      Logger.warn("rule for sensor " + r.getSensor() + " has no operator");
      return;
    }
    ////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////
    // Serializer
    Class<? extends Serializer> c = s.getSerializer();
    if (c == null)
      c = StringSerializer.class;
    Serializer serializer = c.newInstance();
    //
    ////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////
    // Messwert und Limit
    Object oLimit = serializer.unserialize(limit);
    Object oValue = s.getValue();
    ////////////////////////////////////////////////////////////////////////
    
    String subject = "[" + Application.getPluginLoader().getManifest(Plugin.class).getName() + "] " + s.getName();
    String body = "Sensor name  : " + s.getName() + "\n" +
                  "Sensor uuid  : " + s.getUuid() + "\n\n" +
                  "Current Value: " + serializer.format(oValue) + "\n" +
                  "Limit        : " + serializer.format(oLimit);

    String id = r.getID();
    boolean again = settings.getBoolean(id,false);
    
    if (o.matches(oValue,oLimit))
    {
      subject += " -" + (again ? " STILL " : " ") + "OUTSIDE limit. current value: " + serializer.format(oValue) + ", limit: " + serializer.format(oLimit);
      Logger.info(subject);
      settings.setAttribute(id,true); // Wir tragen den Vorfall in die Merkliste ein
      n.outsideLimit(subject,body,r.getParams(),again);
    }
    else if (again)
    {
      subject += " - INSIDE limit. current value: " + serializer.format(oValue) + ", limit: " + serializer.format(oLimit);
      Logger.info(subject);
      settings.setAttribute(id,(String) null); // wir entfernen ihn aus der Merkliste. Das NULL bewirkt ein Loeschen aus der Datei
      n.insideLimit(subject,body,r.getParams());
    }
  }
  
  /**
   * Durchsucht die Messung nach dem angegebenen Sensor.
   * @param uuid der Sensor.
   * @return der Sensor oder NULL, wenn er nicht gefunden wurde.
   */
  private Sensor findSensor(String uuid)
  {
    if (uuid == null || uuid.length() == 0)
    {
      Logger.warn("no sensor uuid given, skipping");
      return null;
    }
    
    List<Sensorgroup> groups = this.measurement.getSensorgroups();
    for (Sensorgroup g:groups)
    {
      List<Sensor> sensors = g.getSensors();
      for (Sensor s:sensors)
      {
        if (uuid.equals(s.getUuid()))
          return s;
      }
    }
    
    Logger.warn("sensor uuid " + uuid + " not found in measurement");
    return null;
  }
  
  /**
   * Liefert den Limit-Wert der Regel.
   * Die Funktion evaluiert evtl. vorhandene Ausdruecke im Limit-Wert.
   * @param r die Regel.
   * @return der Limit-Wert.
   * @throws Exception
   */
  private String getLimit(Rule r) throws Exception
  {
    String limit = r.getLimit();
    if (limit != null && limit.matches("\\{.*\\}"))
    {
      // Limit verweist auf den Wert eines anderen Sensors.
      String uuid = limit.substring(0,limit.length()-1).substring(1);
      Sensor s = findSensor(uuid);
      if (s == null)
        return null;
      Object o = s.getValue();
      Class<? extends Serializer> c = s.getSerializer();
      if (c == null)
        c = StringSerializer.class;
      limit = o != null ? c.newInstance().format(o) : null;
    }
    return limit;
  }
  
  /**
   * Liefert die gefundenen Regeln.
   * @return Liste der gefundenen Regeln.
   */
  private List<Rule> findRules()
  {
    List<Rule> rules = new ArrayList<Rule>();

    // Wir suchen im Pluginverzeichnis und im Work-Verzeichnis. Jeweils
    // im Unterverzeichnis "rules"
    
    File sys = new File(Application.getPluginLoader().getManifest(Plugin.class).getPluginDir(),"rules");
    rules.addAll(findRules(sys));
    
    File user = new File(Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath(),"rules");
    rules.addAll(findRules(user));
    
    return rules;
  }
  
  /**
   * Liefert die Regeln im angegebenen Verzeichnis.
   * @param dir das Verzeichnis, welches nach Regeln durchsucht werden soll.
   * @return Liste der gefundenen Regeln.
   */
  private List<Rule> findRules(File dir)
  {
    List<Rule> rules = new ArrayList<Rule>();
    
    try
    {
      FileFinder finder = new FileFinder(dir);
      finder.extension("xml");
      File[] files = finder.findRecursive();
      
      // Wir sortieren die gefundenen Dateien jetzt noch alphabetisch
      Arrays.sort(files);
      
      for (File f:files)
      {
        InputStream is = null;
        try
        {
          is = new BufferedInputStream(new FileInputStream(f));
          IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
          parser.setReader(new StdXMLReader(is));
          XPathEmu xpath = new XPathEmu((IXMLElement) parser.parse());
          IXMLElement[] list = xpath.getElements("rules/rule");
          for (IXMLElement i:list)
          {
            rules.add(new Rule(i));
          }
        }
        catch (Exception e)
        {
          Logger.error("error while reading file " + f,e);
        }
        finally
        {
          if (is != null)
          {
            try
            {
              is.close();
            }
            catch (Exception e)
            {
              Logger.error("error while closing file " + f,e);
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("error while searching for notify rules in " + dir,e);
    }
    return rules;
  }
}



/**********************************************************************
 * $Log: RuleProcessor.java,v $
 * Revision 1.3  2010/03/01 23:51:07  willuhn
 * @N Benachrichtigung, wenn Sensor zurueck im normalen Bereich ist
 * @N Merken des letzten Notify-Status, sodass nur beim ersten mal eine Mail gesendet wird
 *
 * Revision 1.2  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 17:08:18  willuhn
 * @N Mail-Benachrichtigung via javax.mail
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
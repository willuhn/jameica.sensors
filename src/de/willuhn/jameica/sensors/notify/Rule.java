/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/Rule.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/03/01 23:51:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.notify.notifier.Notifier;
import de.willuhn.jameica.sensors.notify.operator.Operator;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.XPathEmu;
import net.n3.nanoxml.IXMLElement;

/**
 * Implementierung einer einzelnen Benachrichtigungsregel.
 */
public class Rule
{
  private IXMLElement node = null;
  
  /**
   * ct.
   * @param node
   */
  Rule(IXMLElement node)
  {
    this.node = node;
  }
  
  /**
   * Liefert die UUID des zu ueberwachenden Sensors.
   * @return UUID des Sensors.
   * @throws Exception
   */
  public String getSensor() throws Exception
  {
    IXMLElement sensor = this.node.getFirstChildNamed("sensor");
    return sensor != null ? sensor.getContent() : null;
  }
  
  /**
   * Liefert den Grenzwert.
   * @return der Grenzwert.
   * @throws Exception
   */
  public String getLimit() throws Exception
  {
    IXMLElement limit = this.node.getFirstChildNamed("limit");
    return limit != null ? limit.getContent() : null;
  }
  
  /**
   * Liefert den Operator, der entscheidet, ob der Grenzwert ueberschritten ist.
   * @return der Operator.
   * @throws Exception
   */
  public Operator getOperator() throws Exception
  {
    IXMLElement operator = this.node.getFirstChildNamed("operator");
    return operator != null ? (Operator) load(operator.getContent()) : null;
  }
  
  /**
   * Liefert den Notifier, der die Benachrichtigung absendet.
   * @return der Notifier.
   * @throws Exception
   */
  public Notifier getNotifier() throws Exception
  {
    IXMLElement notifier = this.node.getFirstChildNamed("notifier");
    return notifier != null ? (Notifier) load(notifier.getContent()) : null;
  }
  
  /**
   * Liefert die optionalen Regel-Parameter.
   * @return optionale Regel-Parameter.
   * @throws Exception
   */
  public Map<String,String> getParams() throws Exception
  {
    XPathEmu xpath = new XPathEmu(this.node);
    
    IXMLElement[] params = xpath.getElements("params/param");
    Map<String,String> map = new HashMap<String,String>();
    for (IXMLElement e:params)
    {
      String name = e.getAttribute("name",null);
      String value = e.getAttribute("value",null);
      if (name != null && value != null)
        map.put(name,value);
    }
    
    return map;
  }
  
  /**
   * Erzeugt einen Identifier fuer die Regel.
   * @return ein Identifier.
   * @throws Exception
   */
  String getID() throws Exception
  {
    StringBuffer sb = new StringBuffer();

    sb.append(this.getSensor());

    Operator o = this.getOperator();
    if (o != null)
      sb.append(o.getClass());

    sb.append(this.getLimit());
    
    return sb.toString();
  }


  /**
   * Laedt und instanziiert die angegebene Klasse.
   * @param classname Name der Klasse.
   * @return Instanz.
   * @throws Exception
   */
  private Object load(String classname) throws Exception
  {
    try
    {
      ClassLoader l = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getClassLoader();
      return l.loadClass(classname).newInstance();
    }
    catch (Exception e)
    {
      throw e;
    }
    catch (Throwable t)
    {
      // u.a. fuer NoClassDefFoundError
      throw new Exception("unable to load class " + classname,t);
    }
  }
}



/**********************************************************************
 * $Log: Rule.java,v $
 * Revision 1.3  2010/03/01 23:51:07  willuhn
 * @N Benachrichtigung, wenn Sensor zurueck im normalen Bereich ist
 * @N Merken des letzten Notify-Status, sodass nur beim ersten mal eine Mail gesendet wird
 *
 * Revision 1.2  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
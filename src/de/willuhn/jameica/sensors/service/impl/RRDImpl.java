/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/RRDImpl.java,v $
 * $Revision: 1.9 $
 * $Date: 2009/10/13 16:46:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.rrd4j.graph.RrdGraphInfo;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Sensor.Type;
import de.willuhn.jameica.sensors.devices.Sensorgroup.Consolidation;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.RRD;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ColorGenerator;

/**
 * Implementierung des RRD-Services.
 */
public class RRDImpl implements RRD
{
  private MessageConsumer consumer = null;
  
  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "rrd service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.consumer != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }

    this.consumer = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
  }
  
  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    
    Application.getMessagingFactory().unRegisterMessageConsumer(this.consumer);
  }
  
  /**
   * @see de.willuhn.jameica.sensors.service.RRD#render(de.willuhn.jameica.sensors.devices.Device, de.willuhn.jameica.sensors.devices.Sensorgroup, de.willuhn.jameica.sensors.devices.Sensor, java.util.Date, java.util.Date)
   */
  public byte[] render(Device device, Sensorgroup group, Sensor sensor, Date start, Date end) throws RemoteException
  {
    String basedir = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath();
    File deviceDir = new File(basedir,device.getUuid());
    File rrd = new File(deviceDir,group.getUuid() + ".rrd");
    if (!rrd.exists())
      throw new RemoteException("no rrd data found for device " + device.getName() + " [uuid: " + device.getUuid() + "], sensor group " + group.getName() + "[uuid: " + group.getUuid() + "]");

    // In RRD werden ja die UUIDs der Sensoren als Datasource-Name
    // verwendet - jedoch verkuerzt, wenn sie mehr als 20 Zeichen
    // haben. Damit wir das rueckwaerts wieder aufloesen zu koennen,
    // um in der Chartgrafik ordentliche Labels anzuzeigen, bauen
    // wir uns hier eine Map fuers Reverse-Lookup
    Map<String,Sensor> sensorMap = new HashMap<String,Sensor>();
    List<Sensor> sensors = new ArrayList<Sensor>();
    if (sensor != null)
      sensors.add(sensor);
    else
      sensors.addAll(group.getSensors());

    for (Sensor s:sensors)
    {
      sensorMap.put(createRrdName(s.getUuid()),s);
    }
    
    RrdGraphDef gd = new RrdGraphDef();

    try
    {
      // Fuer jeden Sensor eine Datasource mit der UUID als Name.

      //////////////////////////////////////////////////////////////////////////
      // Wir holen uns aus der RRD-Datei die Datasources, um die Plotter
      // zum Chart hizuzufuegen
      RrdDb db = null;
      try
      {
        db = new RrdDb(rrd.getAbsolutePath());
        String[] names = db.getDsNames();
        for (int i=0;i<names.length;++i)
        {
          Sensor s = sensorMap.get(names[i]); // Das sollte jetzt der zugehoerige Sensor sein.
          if (s == null)
          {
            Logger.warn(rrd.getAbsolutePath() + " contains datasource " + names[i] + ", but according sensor no longer exists, skipping");
            continue;
          }
          int[] color = ColorGenerator.create(ColorGenerator.PALETTE_OFFICE + i);
          gd.datasource(names[i],rrd.getAbsolutePath(),names[i],map(group.getConsolidation()));
          gd.line(names[i],new Color(color[0],color[1],color[2]),s.getName(),2);
        }
      }
      finally
      {
        db.close();
      }
      //////////////////////////////////////////////////////////////////////////
      
      gd.setImageFormat("PNG");
      gd.setTitle(group.getName());
      gd.setAntiAliasing(true);
      if (start != null) gd.setStartTime(start.getTime() / 1000L); // RRD verwendet nicht millis sondern Epochensekunden
      if (end != null) gd.setEndTime(end.getTime() / 1000L);

      RrdGraph gr = new RrdGraph(gd);
      BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB); // Die Groessenangabe wird ignoriert - es muss aber etwas angegeben sein
      gr.render(bi.getGraphics());
      
      RrdGraphInfo result = gr.getRrdGraphInfo();
      if (result == null)
        throw new RemoteException("unable to create image, RrdGraphInfo was null, don't know why");
      return result.getBytes();
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to create image",e);
    }
  }

  /**
   * Archiviert die Messergebnisse fuer ein Device.
   * @param uuid UUID des Devices.
   * @param m die Messung.
   * @throws Exception
   */
  private void archive(String uuid, Measurement m) throws Exception
  {
    String basedir = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath();
    
    // In diesem Verzeichnis speichern wir die RRD-Daten fuer das Device.
    File deviceDir = new File(basedir,uuid);
    if (!deviceDir.exists() && !deviceDir.mkdirs())
      throw new Exception("unable to create device dir " + deviceDir.getAbsolutePath());
    
    // Jetzt checken wir fuer jeden Sensor, ob er schon eine RRD-Datei hat
    List<Sensorgroup> groups = m.getSensorgroups();
    for (Sensorgroup group:groups)
    {
      List<Sensor> sensors = group.getSensors();
      if (sensors == null || sensors.size() == 0)
      {
        Logger.warn("sensor group " + group.getName() + " [uuid: " + group.getUuid() + "] from device [uuid: " + uuid + "] contains no sensor values, skipping");
        continue;
      }

      // Checken, ob die Sensorgruppe eine UUID hat. Falls ja, wird zusaetzlich
      // zur RRD-Datei pro Sensor nochmal eine erstellt, in der alle Sensoren
      // der Gruppe enthalten sind. Das ermoglicht, dass in einem Chart mehrere
      // Messwerte (die der Gruppe) gemeinsam angezeigt werden koennen.
      if (group.getUuid() != null)
      {
        // Jepp, wir haben eine. Dann erstellen wir eine RRD-Datei fuer die Gruppe.
        File rrdFile = new File(deviceDir,group.getUuid() + ".rrd");
        store(rrdFile,group.getConsolidation(),sensors.toArray(new Sensor[sensors.size()]));
      }

      // Und jetzt noch die fuer jeden Sensor einzeln.
      for (Sensor s:sensors)
      {
        File rrdFile = new File(deviceDir,s.getUuid() + ".rrd");
        store(rrdFile,group.getConsolidation(),s);
      }
    }
  }
  
  /**
   * Schreibt 1 - x Sensoren in die angegebene RRD-Datei.
   * Falls die Datei noch nicht existiert, wird sie automatisch angelegt.
   * @param rrdFile die RRD-Datei.
   * @param con die Konsolidierungsfunktion.
   * @param sensors die Sensoren.
   * @throws Exception
   */
  private void store(File rrdFile,Consolidation con, Sensor... sensors) throws Exception
  {
    RrdDb db = null;

    ConsolFun fun = map(con);
    
    if (!rrdFile.exists())
    {
      Logger.info("creating new rrd file " + rrdFile.getAbsolutePath());
      RrdDef def = new RrdDef(rrdFile.getAbsolutePath());
      
      // Wir holen uns den Heartbeat-Wert basierend auf dem Scheduler-Intervall
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
      int minutes = settings.getInt("scheduler.interval.minutes",5);

      // Absolute Werte (also nicht bereits als Durchschnittswert gemittelt) bewahren wir
      // einen Tag lang auf. Damit haben wir ueber die letzten 24h die volle Messwert-
      // Aufloesung. Per Default sind das Werte alle 5 Minuten.
      int rows = 60 / minutes; // Anzahl der Messwerte in einer Stunde (12)
      rows *= 24; // Anzahl der Messwerte an einem Tag (288)
      def.addArchive(fun,0.5,1,rows);

      // Bis maximal eine Woche nehmen wir Stunden-Mittelwerte. Heisst:
      // Bei Werten, die max. 7 Tage alte sind, werden die 12 Messungen (von 1 Stunde)
      // zu einem zusammengefasst.
      // "60 / minutes" ergibt "12" und sagt RDD, dass es aus 12 Werten 1 machen soll.
      // Der letzte Parameter gibt an, wie viele von denen aufgehoben werden
      // sollen. Konkret.
      rows = 24 * 7; // 24h (1 pro Stunde) * 7 Tage
      def.addArchive(fun,0.5,(60 / minutes),rows);
      
      // Bis maximal einen Monat nehmen wir 12h-Mittelwerte. Heisst:
      // Ein Tag besteht dann noch aus 2 Messwerten
      rows = 2 * 30; // 24h (2 pro Tag) * 30 Tage
      def.addArchive(fun,0.5,(12 * 60) / minutes,rows);

      // Alles, was noch aelter ist (also aelter als eine Woche), wird nur noch
      // mit taggenauer Aufloesung gespeichert. Ein Tag hat 288 5-Minutenhaeppchen,
      // (24 * 60 / 5).
      // Wir heben sie fuer 20 Jahre auf ;) Solange gibts Java wahrscheinlich gar nicht mehr ;)
      rows = 20 * 365;
      def.addArchive(fun,0.5,(24 * 60 / minutes),rows);

      for (Sensor sensor:sensors)
      {
        // Fuer jeden Sensor eine Datasource mit der UUID als Name.
        // Type ermitteln
        def.addDatasource(createRrdName(sensor.getUuid()),map(sensor.getType()), 2 * minutes * 60,Double.NaN,Double.NaN);
      }
      db = new RrdDb(def);
    }
    else
    {
      db = new RrdDb(rrdFile.getAbsolutePath());
    }
    
    try
    {
      // Messwerte speichern
      Sample s = db.createSample();
      for (Sensor sensor:sensors)
      {
        Object value = sensor.getValue();
        if (value != null)
        {
          try
          {
            // TODO: setValue() wirft eine IllegalArgumentException, wenn der RRD-Name
            // nicht existiert. Sprich: Kommt zu einer Sensor-Gruppe spaeter noch
            // ein Sensor hinzu, wird er nicht mehr im Chart dieser Gruppe erscheinen.
            // Mal schauen, ob das irgendwie loesbar ist
            s.setValue(createRrdName(sensor.getUuid()),Double.parseDouble(value.toString()));
          }
          catch (Exception e)
          {
            Logger.debug("sensor " + sensor.getName() + " [uuid: " + sensor.getUuid() + "] returned unparseble double: " + value + ": " + e.getMessage());
            continue;
          }
        }
      }
      s.update();
    }
    finally
    {
      db.close();
    }
  }
  
  /**
   * Liefert eine auf 20 Zeichen gekuerzte Version der UUID, damit RRD sie akzeptiert.
   * @param uuid die UUID.
   * @return gekuerzte Version.
   */
  private String createRrdName(String uuid)
  {
    if (uuid == null)
      return "";
    
    int len = uuid.length();
    
    if (len <= 20)
      return uuid;
    
    // Wenn der String laenger als 20 Zeichen ist, nehmen wir nur
    // die ersten und letzten 10 Zeichen. Sollte dann immer noch
    // eindeutig sein.
    return (uuid.substring(0,10) + uuid.substring(len-10));
  }
  
  /**
   * Mappt unsere Konsolidierungsfunktion auf die von RRDJ4.
   * Das machen wir nur aus einem Grund: In der Device-API sollen
   * sich keine Referenzen auf RRD4J befinden. Sie soll absolut frei
   * von 3rd-Party-Libs sein.
   * @param c unsere Konsolidierungsfunktion.
   * @return die Konsolidierungsfunktion von RRD4J.
   */
  private ConsolFun map(Consolidation c)
  {
    // Konsolidierungsfunktion ermitteln
    ConsolFun fun = ConsolFun.AVERAGE;
    
    try
    {
      if (c == null)
        c = Sensorgroup.CONSOLIDATION_DEFAULT;
      fun = Enum.valueOf(ConsolFun.class,c.name());
    }
    catch (Exception e)
    {
      Logger.error("unable to determine sensor type",e);
    }
    
    return fun;
  }
  
  /**
   * Mappt unseren Sensor-Typ auf den von RRD4J.
   * @param t unser Sensor-Typ.
   * @return der Sensor-Typ von RRD4J.
   */
  private DsType map(Type t)
  {
    DsType type = DsType.GAUGE;
    try
    {
      if (t == null)
        t = Sensor.TYPE_DEFAULT;
      type = Enum.valueOf(DsType.class,t.name());
    }
    catch (Exception e)
    {
      Logger.error("unable to determine sensor type",e);
    }
    
    return type;
  }
  
  /**
   * Mit dem Message-Consumer abonnieren wir die aktuellen Messwerte fuer die Archivierung.
   */
  private class MyMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{MeasureMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      MeasureMessage msg = (MeasureMessage) message;
      archive(msg.getDevice().getUuid(),msg.getMeasurement());
    }
  }
}


/**********************************************************************
 * $Log: RRDImpl.java,v $
 * Revision 1.9  2009/10/13 16:46:14  willuhn
 * @N Graph pro Sensor zeichnen
 *
 * Revision 1.8  2009/10/13 15:51:04  willuhn
 * @N Sensor-API um Konsolidierungsfunktion erweitert
 *
 * Revision 1.7  2009/09/28 14:26:46  willuhn
 * @N Unterstuetzung fuer die anderen Sensor-Typen von RRD
 *
 * Revision 1.6  2009/08/25 00:05:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2009/08/24 23:49:17  willuhn
 * @N Bei der Office-Farbpalette beginnen
 *
 * Revision 1.4  2009/08/24 17:22:30  willuhn
 * @N Archiv-Zeitraum verkuerzt
 *
 * Revision 1.3  2009/08/22 00:03:42  willuhn
 * @N Das Zeichnen der Charts funktioniert! ;)
 *
 * Revision 1.2  2009/08/21 18:07:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/21 17:27:36  willuhn
 * @N RRD-Service
 *
 **********************************************************************/

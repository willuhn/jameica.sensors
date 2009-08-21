/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/RRDImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/21 18:07:55 $
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
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.RRD;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Implementierung des RRD-Services.
 */
public class RRDImpl implements RRD
{
  private MessageConsumer consumer    = null;

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
   * @see de.willuhn.jameica.sensors.service.RRD#renderGroup(java.lang.String, java.lang.String, java.util.Date, java.util.Date)
   */
  public byte[] renderGroup(String deviceId, String groupId, Date start, Date end) throws RemoteException
  {
    String basedir = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath();
    File deviceDir = new File(basedir,deviceId);
    File rrd = new File(deviceDir,groupId + ".rrd");
    if (!rrd.exists())
      throw new RemoteException("no rrd data found for device [uuid: " + deviceId + "], sensor group [uuid: " + groupId + "]");
    
    RrdGraphDef gd = new RrdGraphDef();
    gd.setFilename(rrd.getAbsolutePath());
    if (start != null) gd.setStartTime(start.getTime() / 1000L); // RRD verwendet nicht millis sondern Epochensekunden
    if (end != null) gd.setEndTime(end.getTime() / 1000L);

    // TODO:
    try
    {
      RrdDb db = new RrdDb(rrd.getAbsolutePath());
      String[] names = db.getDsNames();
      for (String name:names)
      {
        gd.datasource(name,rrd.getAbsolutePath(),name,ConsolFun.AVERAGE);
        gd.line("room",new Color(255,0,0),"Raum-Temperatur",2);
      }

      gd.setFilename("/tmp/install/temp.png");
      gd.setImageFormat("PNG");
      gd.setMaxValue(0d);
      gd.setMaxValue(60d);
      gd.setTitle("Temperaturen");
      gd.setVerticalLabel("°C");

      RrdGraph gr = new RrdGraph(gd);
      BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB); // Die Groessenangabe wird irgendwie ignoriert
      gr.render(bi.getGraphics());
      return null; 
    
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to load rdd file",e);
    }
  }

  /**
   * @see de.willuhn.jameica.sensors.service.RRD#renderSensor(java.lang.String, java.lang.String, java.util.Date, java.util.Date)
   */
  public byte[] renderSensor(String deviceId, String sensorId, Date start, Date end) throws RemoteException
  {
    String basedir = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath();
    File deviceDir = new File(basedir,deviceId);
    return null; // TODO
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
        store(rrdFile,sensors.toArray(new Sensor[sensors.size()]));
      }

      // Und jetzt noch die fuer jeden Sensor einzeln.
      for (Sensor s:sensors)
      {
        File rrdFile = new File(deviceDir,s.getUuid() + ".rrd");
        store(rrdFile,s);
      }
    }
  }
  
  /**
   * Schreibt 1 - x Sensoren in die angegebene RRD-Datei.
   * Falls die Datei noch nicht existiert, wird sie automatisch angelegt.
   * @param rrdFile die RRD-Datei.
   * @param sensors die Sensoren.
   * @throws Exception
   */
  private void store(File rrdFile,Sensor... sensors) throws Exception
  {
    RrdDb db = null;

    if (!rrdFile.exists())
    {
      Logger.info("creating new rrd file " + rrdFile.getAbsolutePath());
      RrdDef def = new RrdDef(rrdFile.getAbsolutePath());
      
      // Ich weiss nicht, ob es sinnvoll ist, diese Werte konfigurierbar zu machen
      def.addArchive(ConsolFun.AVERAGE,0.5,1,288);  // letzte 24h (24h = 1440min -> 5min-Intervall -> 288 Werte
      def.addArchive(ConsolFun.AVERAGE,0.5,12,168); // letzte Woche (7 Tage = 168h), Stundenmittel (60min/5)
      def.addArchive(ConsolFun.AVERAGE,0.5,288,365); // letztes Jahr (365 Tage), Tagesmittel (24*60m/5)

      // Wir holen uns den Heartbeat-Wert basierend auf dem Scheduler-Intervall
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
      int minutes = settings.getInt("scheduler.interval.minutes",5);

      for (Sensor sensor:sensors)
      {
        // Fuer jeden Sensor eine Datasource mit der UUID als Name.
        def.addDatasource(createRrdName(sensor.getUuid()),DsType.GAUGE,minutes * 60,Double.NaN,Double.NaN);
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
        double d = 0.0d;
        if (value != null)
        {
          try
          {
            d = Double.parseDouble(value.toString());
          }
          catch (Exception e)
          {
            Logger.debug("sensor " + sensor.getName() + " [uuid: " + sensor.getUuid() + "] returned unparseble double: " + value + ": " + e.getMessage());
            continue;
          }
        }
        s.setValue(createRrdName(sensor.getUuid()),d);
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
 * Revision 1.2  2009/08/21 18:07:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/21 17:27:36  willuhn
 * @N RRD-Service
 *
 **********************************************************************/

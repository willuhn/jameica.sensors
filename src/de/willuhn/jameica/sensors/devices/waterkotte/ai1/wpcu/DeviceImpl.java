/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/waterkotte/ai1/wpcu/DeviceImpl.java,v $
 * $Revision: 1.11 $
 * $Date: 2012/04/17 22:32:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1.wpcu;

import gnu.io.SerialPort;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.SerialParameters;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung der Waterkotte Ai1 mit dem WPCU-Steuergeraet.
 */
public class DeviceImpl implements Device, Configurable
{
  private final static I18N i18n         = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static Settings settings = new Settings(DeviceImpl.class);
  private static boolean msgPrinted = false;
  
  private final static long TIMEOUT = 24 * 60 * 60 * 1000L; // Das Timeout fuer die Extrem-Werte

  // Cache fuer die Hoechst- und Tiefs-Werte der letzten 24h.
  private final Map<String,Extreme> extremes  = new HashMap<String,Extreme>();
  
  /**
   * @see de.willuhn.jameica.sensors.devices.Device#collect()
   */
  public Measurement collect() throws IOException
  {
    String device = settings.getString("serialport.device",null);
    if (device == null || device.length() == 0)
    {
      // Wir zeigen den Hinweistext nur beim ersten Mal an.
      if (!msgPrinted)
        Logger.warn("device " + this.getName() + "[uuid: " + this.getUuid() + "] not configured - no serial port defined");
      msgPrinted = true;
      return null;
    }

    SerialParameters params = new SerialParameters();
    params.setPortName(device);
    params.setBaudRate(settings.getInt("serialport.baudrate",9600));
    params.setDatabits(settings.getInt("serialport.databits",8));
    params.setParity(settings.getInt("serialport.parity",SerialPort.PARITY_NONE));
    params.setStopbits(settings.getInt("serialport.stopbits",1));
    params.setEncoding(settings.getString("serialport.encoding",Modbus.SERIAL_ENCODING_RTU));
    params.setEcho(false);
    
    SerialConnection conn = null;
    try
    {
      Logger.debug("open connection to " + device);
      conn = new SerialConnection(params);
      conn.open();
      
      ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(1,60);
      request.setHeadless();
      request.setUnitID(settings.getInt("modbus.unitid",1));
      
      ModbusSerialTransaction tr = new ModbusSerialTransaction(conn);
      tr.setRequest(request);
      tr.setRetries(settings.getInt("modbus.retries",3));
      tr.setTransDelayMS(settings.getInt("modbus.delay.millis",2000));
      tr.execute();
      
      ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) tr.getResponse();
      Logger.debug("response: " + response.getHexMessage());

      // Eigentlich koennten wir die Register auch einzeln auslesen.
      // Da ich die Offsets und Felder aber noch nicht richtig kenne,
      // ist es im Moment einfacher, alle Daten in einen Byte-Stream
      // zu schreiben und dann anhand der Offsets manuell zu lesen.
      Register[] registers = response.getRegisters();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      for (int i=0;i<registers.length;++i)
      {
        bos.write(registers[i].toBytes());
      }
      
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
      
      // Der folgende Code sollte spaeter mal noch modularisiert werden.
      // Lohnt sich fuer die paar Messwerte aber noch nicht.
      Measurement m = new Measurement();

      //////////////////////////////////////////////////////////////////////////
      // Aussentemperatur
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.outdoor");
        g.setName(i18n.tr("Außentemperaturen"));
        
        Sensor<Float> current = createSensor(dis,56,"temp.outdoor.current",i18n.tr("Aktuell"));
        g.getSensors().add(current);
        g.getSensors().add(this.createExtreme(current,Extreme.Type.MAX));
        g.getSensors().add(this.createExtreme(current,Extreme.Type.MIN));
        
        g.getSensors().add(createSensor(dis,60,"temp.outdoor.1h",i18n.tr("Mittelwert 1h")));
        g.getSensors().add(createSensor(dis,64,"temp.outdoor.24h",i18n.tr("Mittelwert 24h")));
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Heizung
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.heater");
        g.setName(i18n.tr("Heizungstemperaturen"));
        g.getSensors().add(createSensor(dis,68,"temp.heater.return.target",i18n.tr("Rücklauf Soll")));

        {
          Sensor<Float> current = createSensor(dis,72,"temp.heater.return.real",i18n.tr("Rücklauf Ist"));
          g.getSensors().add(current);
          g.getSensors().add(this.createExtreme(current,Extreme.Type.MAX));
          g.getSensors().add(this.createExtreme(current,Extreme.Type.MIN));
        }
        
        {
          Sensor<Float> current = createSensor(dis,76,"temp.heater.out.real",i18n.tr("Vorlauf Ist"));
          g.getSensors().add(current);
          g.getSensors().add(this.createExtreme(current,Extreme.Type.MAX));
          g.getSensors().add(this.createExtreme(current,Extreme.Type.MIN));
        }
        
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////
      // Warmwasser
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.water");
        g.setName(i18n.tr("Warmwassertemperaturen"));
        g.getSensors().add(createSensor(dis,80,"temp.water.target",i18n.tr("Soll")));
        
        Sensor<Float> current = createSensor(dis,84,"temp.water.real",i18n.tr("Ist"));
        g.getSensors().add(current);
        g.getSensors().add(this.createExtreme(current,Extreme.Type.MAX));
        g.getSensors().add(this.createExtreme(current,Extreme.Type.MIN));
        
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Waerme-Quelle (Sonde in der Tiefenbohrung)
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.system");
        g.setName(i18n.tr("System-Temperaturen"));

        Sensor<Float> current = createSensor(dis,96,"temp.system.source.in",i18n.tr("Wärmequelle Eingang"));
        g.getSensors().add(current);
        g.getSensors().add(this.createExtreme(current,Extreme.Type.MAX));
        g.getSensors().add(this.createExtreme(current,Extreme.Type.MIN));
        
        g.getSensors().add(createSensor(dis,100,"temp.system.source.out",i18n.tr("Wärmequelle Ausgang")));
        g.getSensors().add(createSensor(dis,104,"temp.system.evaporator",i18n.tr("Verdampfer")));
        g.getSensors().add(createSensor(dis,108,"temp.system.condenser",i18n.tr("Kondensator")));
        g.getSensors().add(createSensor(dis,112,"temp.system.suction",i18n.tr("Saugleitung")));
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////
      return m;
    }
    catch (IOException ioe)
    {
      throw ioe;
    }
    catch (Exception e)
    {
      Logger.error("error while fetching data from device",e); // Kann man mit der IOException leider nicht weiterwerfen
      throw new IOException("error while fetching data from device: " + e.getMessage());
    }
    finally
    {
      if (conn != null)
      {
        try
        {
          conn.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close serial connection",e);
        }
      }
    }
  }

  /**
   * Erstellt einen neuen Sensor.
   * @param data der Stream.
   * @param offset Offset, ab dem gelesen werden soll.
   * @param id ID.
   * @param name sprechender Name des Sensors.
   * @return der erzeugte Sensor.
   * @throws IOException
   */
  private Sensor<Float> createSensor(DataInputStream data, int offset, String id, String name) throws IOException
  {
    try
    {
      // Wir markieren den Startpunkt - auf den springen wir dann wieder zurueck
      data.mark(-1); // ist unten ein ByteArrayInputStream - da wird das eh ignoriert ;)

      // Wir springen an die gewuenschte Position
      data.skipBytes(offset);
      
      Sensor<Float> s = new Sensor<Float>();
      s.setName(name);
      s.setUuid(this.getUuid() + "." + id); // wir haengen noch die Device-UUID davor, damit es global eindeutig ist
      s.setSerializer(TempSerializer.class);
      s.setValue(data.readFloat());
      return s;
    }
    finally
    {
      // An den Anfang zurueckspringen
      data.reset();
    }
  }

  /**
   * Erzeut eine Kopie des Sensors - jedoch mit dem 24h-Extrem des Sensors.
   * @param sensor der Sensor.
   * @return die Kopie des Sensors - jedoch mit dem 24h-Extrem.
   */
  private Sensor<Float> createExtreme(Sensor<Float> sensor, Extreme.Type type)
  {
    String key = sensor.getUuid() + "." + type.key;
    Extreme extreme = extremes.get(key);

    // Es gibt noch gar keinen Maximal-Wert. Dann legen wir einen neuen an
    if (extreme == null)
    {
      extreme = new Extreme(sensor,type);
      extremes.put(key,extreme);
    }
    else
    {
      // Ansonsten ggf. aktualisierten Wert uebernehmen
      extreme.update(sensor.getValue());
    }

    return extreme.sensor;
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#getName()
   */
  public String getName()
  {
    return "Waterkotte Ai1 (WPCU)";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    return "waterkotte.ai1.wpcu.device";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#isEnabled()
   */
  public boolean isEnabled()
  {
    // Wir checken einfach, ob die Heizung konfiguriert ist
    return settings.getString("serialport.device",null) != null;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    List<Parameter> params = new ArrayList<Parameter>();
    
    // Wir haengen an die ID des Parameters immer noch unsere Device-UUID dran,
    // damit er eindeutig wird.
    params.add(new Parameter(i18n.tr("Serieller Port"),i18n.tr("Für Linux meist /dev/ttyS0, für Windows COM1"),settings.getString("serialport.device",null),this.getUuid() + ".serialport.device"));
    params.add(new Parameter(i18n.tr("Baud-Rate"),i18n.tr("Serielle Übertragungsgeschwindigkeit (meist 9600)"),settings.getString("serialport.baudrate","9600"),this.getUuid() + ".serialport.baudrate"));
    params.add(new Parameter(i18n.tr("Modbus-Adresse"),i18n.tr("Modbus-Adresse der Anlage (meist 1)"),settings.getString("modbus.unitid","1"),this.getUuid() + ".modbus.unitid"));
    return params;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#setParameters(java.util.List)
   */
  public void setParameters(List<Parameter> parameters)
  {
    for (Parameter p:parameters)
    {
      String id = p.getUuid();
      
      // Wir schneiden unsere Device-UUID wieder ab
      id = id.substring(this.getUuid().length()+1); // das "+1" ist fuer den "." als Trennzeichen

      String oldValue = settings.getString(id,null);
      String newValue = p.getValue();
      
      String s1 = oldValue == null ? "" : oldValue;
      String s2 = newValue == null ? "" : newValue;
      if (!s1.equals(s2))
      {
        Logger.info("parameter \"" + p.getName() + "\" [" + id + "] changed. old value: " + oldValue + ", new value: " + newValue);
        settings.setAttribute(id,newValue);
      }
    }
  }

  /**
   * Haelt die Extrem-Werte der letzten 24h.
   */
  private static class Extreme
  {
    /**
     * Der Typ des Extems.
     */
    private static enum Type
    {
      MAX("max",i18n.tr("24h Maximum")),
      MIN("min",i18n.tr("24h Minimum"));
      
      private String key   = null;
      private String title = null;
      
      /**
       * ct.
       * @param key
       * @param title
       */
      private Type(String key, String title)
      {
        this.key   = key;
        this.title = title;
      }
    }
    
    private Sensor<Float> sensor = null;
    private Type type            = null;
    private long timestamp       = System.currentTimeMillis();
    
    /**
     * ct.
     * @param sensor der Sensor, fuer den der Extrem-Wert gebildet werden soll (wird gecloned).
     * @param type die Art des Extrems.
     */
    private Extreme(Sensor<Float> sensor, Type type)
    {
      this.sensor = (Sensor<Float>) sensor.clone();
      this.sensor.setUuid(sensor.getUuid() + "." + type.key);
      this.sensor.setName(i18n.tr("{0} ({1})",sensor.getName(),type.title));
      this.type = type;
    }
    
    /**
     * Prueft, ob der Extrem-Wert abgelaufen ist.
     * @return true, wenn er abgelaufen ist.
     */
    private boolean isExpired()
    {
      long now = System.currentTimeMillis();
      return (now - this.timestamp) > TIMEOUT;
    }
    
    /**
     * Prueft, ob der uebergebene Wert im Vergleich zum aktuellen ein neues Extrem ist.
     * @param extreme der zu pruefende Wert,
     * @return true, wenn es ein neues Extrem ist.
     */
    private boolean isExtreme(Float extreme)
    {
      Float old = this.sensor.getValue();
      
      if (this.type == Type.MAX)
        return (extreme.compareTo(old) > 0);
      
      // Minimum
      return (extreme.compareTo(old) < 0);
    }
    
    /**
     * Prueft, ob der uebergebene Wert ein neuer Extrem-Wert innerhalb der letzten 24h ist.
     * Wenn es ein neuer Extrem-Wert ist, wird er uebernommen, sonst ignoriert.
     * @param extreme das potentielle neue Extrem.
     */
    private void update(Float extreme)
    {
      // a) Wert ist aelter als 24h -> der alte ist abgelaufen, wir nehmen den neuen
      // b) oder Wert ist neues Extrem -> der alte ist kein Extrem mehr, wir uebernehmen den neuen
      if (this.isExpired() || (this.isExtreme(extreme)))
      {
        // Zeitstempel aktualisieren
        this.timestamp = System.currentTimeMillis();
        
        // Neuen Wert uebernehmen
        this.sensor.setValue(extreme);
      }
    }
  }
  
}


/**********************************************************************
 * $Log: DeviceImpl.java,v $
 * Revision 1.11  2012/04/17 22:32:25  willuhn
 * @B wrong uuid
 *
 * Revision 1.10  2012/04/17 22:25:05  willuhn
 * @N 24h-Maximal- und -Minimal-Werte
 *
 * Revision 1.9  2009/09/16 11:26:14  willuhn
 * @C Auth fuer /sensors von /webadmin wiederverwenden
 **********************************************************************/

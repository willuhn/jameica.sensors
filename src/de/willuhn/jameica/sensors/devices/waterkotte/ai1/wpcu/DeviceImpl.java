/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/waterkotte/ai1/wpcu/DeviceImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/08/21 17:27:37 $
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

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.SerialParameters;
import de.willuhn.jameica.sensors.Plugin;
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
public class DeviceImpl implements Device
{
  private final static I18N i18n         = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static Settings settings = new Settings(DeviceImpl.class);
  
  /**
   * @see de.willuhn.jameica.sensors.devices.Device#collect()
   */
  public Measurement collect() throws IOException
  {
    String device = settings.getString("serialport.device",null);
    if (device == null)
    {
      Logger.warn("device " + this.getName() + "[uuid: " + this.getUuid() + "] not configured");
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
        g.getSensors().add(createSensor(dis,56,"temp.outdoor.current",i18n.tr("Aktuell")));
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
        g.getSensors().add(createSensor(dis,72,"temp.heater.return.real",i18n.tr("Rücklauf Ist")));
        g.getSensors().add(createSensor(dis,76,"temp.heater.out.real",i18n.tr("Vorlauf Ist")));
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
        g.getSensors().add(createSensor(dis,84,"temp.water.real",i18n.tr("Ist")));
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Waerme-Quelle (Sonde in der Tiefenbohrung)
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.system");
        g.setName(i18n.tr("System-Temperaturen"));
        g.getSensors().add(createSensor(dis,96,"temp.system.source.in",i18n.tr("Wärmequelle Eingang")));
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
  private Sensor createSensor(DataInputStream data, int offset, String id, String name) throws IOException
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
}


/**********************************************************************
 * $Log: DeviceImpl.java,v $
 * Revision 1.7  2009/08/21 17:27:37  willuhn
 * @N RRD-Service
 *
 * Revision 1.6  2009/08/21 14:26:00  willuhn
 * @N null als Rueckgabewert tolerieren
 *
 * Revision 1.5  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.4  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.3  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.2  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.3  2009/08/18 23:27:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 * Revision 1.1  2009/08/18 16:29:19  willuhn
 * @N DIE SCHEISSE GEHT! ;)
 *
 **********************************************************************/

/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/waterkotte/ai1/wpcu/DeviceImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/19 10:34:43 $
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
import java.util.List;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.SerialParameters;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.beans.ValueGroup;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.waterkotte.ai1.wpcu.values.TempValue;
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
      Logger.info("open connection to " + device);
      conn = new SerialConnection(params);
      conn.open();
      
      ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(1,60);
      request.setHeadless();
      request.setUnitID(settings.getInt("device.waterkotte.ai1.wpcu.unitid",1));
      
      ModbusSerialTransaction tr = new ModbusSerialTransaction(conn);
      tr.setRequest(request);
      tr.setRetries(settings.getInt("device.waterkotte.ai1.wpcu.retries",3));
      tr.setTransDelayMS(settings.getInt("device.waterkotte.ai1.wpcu.delay.millis",2000));
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
      List<ValueGroup> groups = new ArrayList<ValueGroup>();
      m.setValueGroups(groups);

      int valueCount = 0;
      //////////////////////////////////////////////////////////////////////////
      // Aussentemperatur
      {
        List<Value> l = new ArrayList<Value>();
        l.add(new TempValue(i18n.tr("Aktuell"),dis,56));
        l.add(new TempValue(i18n.tr("Mittelwert 1h"),dis,60));
        l.add(new TempValue(i18n.tr("Mittelwert 24h"),dis,64));
        ValueGroup g = new ValueGroup();
        g.setName(i18n.tr("Außentemperaturen"));
        g.setValues(l);
        groups.add(g);
        valueCount += l.size();
      }
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Heizung
      {
        List<Value> l = new ArrayList<Value>();
        l.add(new TempValue(i18n.tr("Rücklauf Soll"),dis,68));
        l.add(new TempValue(i18n.tr("Rücklauf Ist"),dis,72));
        l.add(new TempValue(i18n.tr("Vorlauf Ist"),dis,76));
        ValueGroup g = new ValueGroup();
        g.setName(i18n.tr("Heizungstemperaturen"));
        g.setValues(l);
        groups.add(g);
        valueCount += l.size();
      }
      //////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////
      // Warmwasser
      {
        List<Value> l = new ArrayList<Value>();
        l.add(new TempValue(i18n.tr("Soll"),dis,80));
        l.add(new TempValue(i18n.tr("Ist"),dis,84));
        ValueGroup g = new ValueGroup();
        g.setName(i18n.tr("Warmwassertemperaturen"));
        g.setValues(l);
        groups.add(g);
        valueCount += l.size();
      }
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Waerme-Quelle (Sonde in der Tiefenbohrung)
      {
        List<Value> l = new ArrayList<Value>();
        l.add(new TempValue(i18n.tr("Wärmequelle Eingang"),dis,96));
        l.add(new TempValue(i18n.tr("Wärmequelle Ausgang"),dis,100));
        l.add(new TempValue(i18n.tr("Verdampfer"),dis,104));
        l.add(new TempValue(i18n.tr("Kondensator"),dis,108));
        l.add(new TempValue(i18n.tr("Saugleitung"),dis,112));
        ValueGroup g = new ValueGroup();
        g.setName(i18n.tr("System-Temperaturen"));
        g.setValues(l);
        groups.add(g);
        valueCount += l.size();
      }
      //////////////////////////////////////////////////////////////////////////
      
      Logger.info("collected " + valueCount + " values in " + groups.size() + " groups");
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
   * @see de.willuhn.jameica.sensors.devices.Device#getName()
   */
  public String getName()
  {
    return "Waterkotte Ai1 (WPCU)";
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

/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/test/Attic/DeviceImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/09/15 17:00:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.DateSerializer;
import de.willuhn.jameica.sensors.devices.DecimalSerializer;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Das Device dient nur zum Testen. Es liefert lediglich einen Messwert mit der aktuellen Uhrzeit.
 * Es ist eher dazu gedacht, Funktionen des Frameworks (wie etwa die Archivierung) zu testen,
 * ohne konkrete Hardware-Devices ansprechen zu muessen.
 */
public class DeviceImpl implements Device, Configurable
{
  private final static I18N i18n         = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static Settings settings = new Settings(DeviceImpl.class);

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#collect()
   */
  public Measurement collect() throws IOException
  {
    Measurement m = new Measurement();

    {
      Sensorgroup group = new Sensorgroup();
      group.setUuid(this.getUuid() + ".date");
      group.setName(i18n.tr("Datum und Uhrzeit"));

      Sensor<Date> s = new Sensor<Date>();
      s.setUuid(this.getUuid() + ".date.current");
      s.setName(i18n.tr("Aktuelles Datum"));
      s.setValue(new Date());
      s.setSerializer(DateSerializer.class);

      group.getSensors().add(s);
      m.getSensorgroups().add(group);
    }

    {
      Sensorgroup group = new Sensorgroup();
      group.setName(i18n.tr("Dummy-Value"));
      group.setUuid(this.getUuid() + ".dummy");
      Sensor<Integer> s = new Sensor<Integer>();
      s.setUuid(this.getUuid() + ".dummy.value");
      s.setName("Dummy-Value");
      s.setSerializer(DecimalSerializer.class);
      s.setValue(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
      
      group.getSensors().add(s);
      m.getSensorgroups().add(group);
    }
    
    return m;
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    return "jameica.sensors.test.device";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#getName()
   */
  public String getName()
  {
    return "Test-Device";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#isEnabled()
   */
  public boolean isEnabled()
  {
    // muss explizit aktiviert werden
    return settings.getBoolean("enabled",true);
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    List<Parameter> params = new ArrayList<Parameter>();
    params.add(new Parameter(i18n.tr("Test-Device aktiviert"),i18n.tr("Test-Device, welches lediglich Dummy-Werte zum Testen liefert. Mögliche Werte: true/false"),settings.getString("enabled","true"),this.getUuid() + ".enabled"));
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
  
  

}


/**********************************************************************
 * $Log: DeviceImpl.java,v $
 * Revision 1.7  2009/09/15 17:00:16  willuhn
 * @N Konfigurierbarkeit aller Module ueber das Webfrontend
 *
 * Revision 1.6  2009/08/22 00:03:42  willuhn
 * @N Das Zeichnen der Charts funktioniert! ;)
 *
 * Revision 1.5  2009/08/21 18:07:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.3  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.2  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.1  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/

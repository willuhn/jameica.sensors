/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/test/Attic/DeviceImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/08/20 22:08:42 $
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
import java.util.Date;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.beans.Valuegroup;
import de.willuhn.jameica.sensors.beans.Value.Type;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.util.UUIDUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.I18N;

/**
 * Das Device dient nur zum Testen. Es liefert lediglich einen Messwert mit der aktuellen Uhrzeit.
 * Es ist eher dazu gedacht, Funktionen des Frameworks (wie etwa die Archivierung) zu testen,
 * ohne konkrete Hardware-Devices ansprechen zu muessen.
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
    
    Value<Date> v = new Value<Date>();
    v.setName(i18n.tr("Aktuelles Datum"));
    v.setValue(new Date());
    v.setType(Type.DATE);
    
    Valuegroup g = new Valuegroup();
    g.setName(i18n.tr("Datum und Uhrzeit"));
    g.setUuid(UUIDUtil.create("jameica.sensors.test.group.date"));
    g.getValues().add(v);
    
    Measurement m = new Measurement();
    m.getValuegroups().add(g);
    
    return m;
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#getUuid()
   */
  public String getUuid()
  {
    return UUIDUtil.create("jameica.sensors.test.device");
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
    return settings.getBoolean("enabled",false);
  }

}


/**********************************************************************
 * $Log: DeviceImpl.java,v $
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

/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/test/Attic/DeviceImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/19 23:46:29 $
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
import java.util.Date;
import java.util.List;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.beans.ValueGroup;
import de.willuhn.jameica.sensors.beans.Value.Type;
import de.willuhn.jameica.sensors.devices.Device;
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
    
    List<Value> lv = new ArrayList<Value>();
    lv.add(v);
    
    ValueGroup g = new ValueGroup();
    g.setName(i18n.tr("Datum und Uhrzeit"));
    g.setValues(lv);

    List<ValueGroup> lg = new ArrayList<ValueGroup>();
    lg.add(g);
    Measurement m = new Measurement();
    m.setValueGroups(lg);
    
    return m;
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#getId()
   */
  public String getId()
  {
    return this.getClass().getName();
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
 * Revision 1.1  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/

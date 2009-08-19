/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/DeviceRegistry.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/19 10:34:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Hilfsklasse mit statischen Methoden zum Ermitteln der Geraete.
 */
public class DeviceRegistry
{
  private static List<Device> devices = null;
  
  /**
   * Liefert die Liste der gefundenen Geraete.
   * @return Liste der Geraete.
   * Die Funktion liefert nie NULL und wirft auch keine Exception sondern
   * liefert stattdessen eine leere Liste.
   */
  public static synchronized List<Device> getDevices()
  {
    if (devices != null)
      return devices;
    
    devices = new ArrayList<Device>();
    
    try
    {
      ClassFinder finder = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getClassLoader().getClassFinder();
      Class<Device>[] classes = finder.findImplementors(Device.class);
      for (Class<Device> c:classes)
      {
        try
        {
          devices.add(c.newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load device " + c.getName() + ", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no devices found");
    }
    return devices;
  }
}


/**********************************************************************
 * $Log: DeviceRegistry.java,v $
 * Revision 1.1  2009/08/19 10:34:42  willuhn
 * @N initial import
 *
 * Revision 1.1  2009/08/18 16:29:19  willuhn
 * @N DIE SCHEISSE GEHT! ;)
 *
 **********************************************************************/

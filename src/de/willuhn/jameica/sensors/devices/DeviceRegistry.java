/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
      ClassFinder finder = Application.getPluginLoader().getManifest(Plugin.class).getClassLoader().getClassFinder();
      Class<Device>[] classes = finder.findImplementors(Device.class);
      for (Class<Device> c:classes)
      {
        try
        {
          devices.add(c.getDeclaredConstructor().newInstance());
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

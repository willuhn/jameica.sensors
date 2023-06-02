/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.controller;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.sensors.messaging.LimitMessageConsumer;
import de.willuhn.jameica.sensors.messaging.LiveMeasurement;
import de.willuhn.logging.Logger;

/**
 * Controller-Bean fuer einen Sensor-Status.
 */
@Lifecycle(Type.REQUEST)
public class Status
{
  private Map<Class<? extends Serializer>,Serializer> cache = new HashMap<Class<? extends Serializer>,Serializer>();
  
  /**
   * Liefert die Live-Messwerte.
   * @return die Live-Messwerte.
   */
  public Map<Device,Measurement> getMeasurements()
  {
    return LiveMeasurement.getValues();
  }
  
  /**
   * Formatiert den Messwert des Sensors.
   * @param s Sensor.
   * @return Format des Sensors.
   */
  public String format(Sensor s)
  {
    Object value = s.getValue();
    try
    {
      Class c = s.getSerializer();
      // Mal schauen, ob wir den Serializer schon instanziiert haben
      Serializer si = cache.get(c);
      if (si == null)
      {
        si = (Serializer) c.getDeclaredConstructor().newInstance();
        cache.put(c,si);
      }
      return si.format(value);
    }
    catch (Exception e)
    {
      Logger.error("unable to format value " + value + " for sensor " + s.getName() + " [" + s.getUuid() + "]",e);
    }
    return new StringSerializer().format(value);
  }
  
  /**
   * Prueft, ob der Sensor ausserhalb des Limits ist.
   * @param s der zu pruefende Sensor.
   * @return true, wenn der Sensor ausserhalb des Limits ist.
   */
  public boolean outsideLimit(Sensor s)
  {
    if (s == null)
      return false;
    return LimitMessageConsumer.outsideLimit(s.getUuid());
  }
}

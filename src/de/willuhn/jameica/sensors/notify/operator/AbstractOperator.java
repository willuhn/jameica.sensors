/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify.operator;

import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;

/**
 * Abstrakte Basis-Klasse der Operatoren.
 */
public abstract class AbstractOperator implements Operator
{
  /**
   * Bereitet das Limit so vor, dass es verglichen werden kann.
   * @param serializer der Serializer des Sensors.
   * @param limit das Limit laut Regel.
   * @return das deserialisierte Limit.
   * @throws IllegalArgumentException
   */
  Comparable prepareLimit(Class<? extends Serializer> serializer, String limit) throws IllegalArgumentException
  {
    if (limit == null || limit.trim().length() == 0)
      throw new IllegalArgumentException("no limit given");

    if (serializer == null)
      serializer = StringSerializer.class;
    
    try
    {
      Serializer s = serializer.getDeclaredConstructor().newInstance();
      return prepareSensor(s.unserialize(limit.trim()));
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("unable to load serializer",e);
    }
  }
  
  /**
   * Bereitet den Messwert so vor, dass er verglichen werden kann.
   * @param value der Messwert.
   * @return der vergleichbare Messwert.
   */
  Comparable prepareSensor(Object value)
  {
    if (value == null)
      return null;
    
    if (value instanceof Number)
      return Double.valueOf(((Number)value).doubleValue());
    
    if (value instanceof Comparable)
      return (Comparable) value;
    
    return value.toString();
  }
  
  /**
   * Prueft die Parameter auf Gueltigkeit.
   * @param sensor der Sensor.
   * @param limit das Limit.
   * @throws IllegalArgumentException
   */
  void check(Sensor sensor, String limit) throws IllegalArgumentException
  {
    if (sensor == null)
      throw new IllegalArgumentException("no sensor given");

    if (limit == null || limit.trim().length() == 0)
      throw new IllegalArgumentException("no limit given");

    Object value = sensor.getValue();
    if (value == null)
      throw new IllegalArgumentException("sensor has no value");
  }
}

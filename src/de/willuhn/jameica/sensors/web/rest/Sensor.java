/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.rest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.messaging.LiveMeasurement;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.rest.AutoRestBean;

/**
 * REST-Bean fuer den Zugriff auf Sensor-Werte.
 */
@Doc("jameica.sensors: Bietet Zugriff auf die Messwerte einzelner Sensoren")
public class Sensor implements AutoRestBean
{
  /**
   * Liefert den Messwert des angegebenen Sensors.
   * @param uuid die UUID des Sensors.
   * @return der Messwert.
   * @throws Exception
   */
  @Doc(value="Liefert den aktuellen Messwert des angegebenen Sensors",
       example="sensors/value/jameica.sensors.vm.device.mem.total")
  @Path("/sensors/value/(.*)")
  public Object value(String uuid) throws Exception
  {
    if (uuid == null || uuid.length() == 0)
      throw new IOException("no uuid given");

    Map<Device,Measurement> values = LiveMeasurement.getValues();
    Iterator<Device> it = values.keySet().iterator();
    while (it.hasNext())
    {
      Device d = it.next();
      Measurement m = values.get(d);
      List<Sensorgroup> groups = m.getSensorgroups();
      for (Sensorgroup g:groups)
      {
        List<de.willuhn.jameica.sensors.devices.Sensor> sensors = g.getSensors();
        for (de.willuhn.jameica.sensors.devices.Sensor s:sensors)
        {
          if (uuid.equals(s.getUuid()))
          {
            Object value = s.getValue();
            
            // Sensor hat keinen Wert
            if (value == null)
              return "";

            // Haben wir einen Serializer?
            Class<Serializer> serializer = s.getSerializer();
            if (serializer != null)
            {
              Serializer sl = serializer.getDeclaredConstructor().newInstance();
              return sl.format(s.getValue());
            }
            
            // Unformatiert zurueckliefern
            return value;
          }
        }
      }
    }
    throw new IOException("no sensor found for uuid: " + uuid);
  }
}

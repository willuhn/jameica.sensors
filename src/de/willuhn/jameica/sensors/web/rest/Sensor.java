/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/rest/Sensor.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/05/11 14:59:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.rest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.messaging.LiveMeasurement;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.annotation.Response;
import de.willuhn.jameica.webadmin.rest.AutoRestBean;

/**
 * REST-Bean fuer den Zugriff auf Sensor-Werte.
 */
public class Sensor implements AutoRestBean
{
  @Response
  private HttpServletResponse response = null;

  /**
   * Liefert den Messwert des angegebenen Sensors.
   * @param uuid die UUID des Sensors.
   * @throws Exception
   */
  @Path("/sensors/value/(.*)")
  public void value(String uuid) throws Exception
  {
    if (uuid == null || uuid.length() == 0)
      throw new ServletException("no uuid given");

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
            {
              response.getWriter().print("");
              return;
            }

            // Haben wir einen Serializer?
            Class<Serializer> serializer = s.getSerializer();
            if (serializer != null)
            {
              Serializer sl = serializer.newInstance();
              response.getWriter().print(sl.format(s.getValue()));
              return;
            }
            
            // Unformatiert zurueckliefern
            response.getWriter().print(value.toString());
            return;
          }
        }
      }
    }
    throw new IOException("no sensor found for uuid: " + uuid);
  }
}



/**********************************************************************
 * $Log: Sensor.java,v $
 * Revision 1.2  2010/05/11 14:59:50  willuhn
 * @N Automatisches Deployment von REST-Beans
 *
 * Revision 1.1  2010/02/10 13:47:56  willuhn
 * @N REST-Support zur Abfrage einzelner Werte
 *
 **********************************************************************/
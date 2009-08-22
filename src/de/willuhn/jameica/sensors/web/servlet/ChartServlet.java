/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/servlet/ChartServlet.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/22 00:03:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.messaging.LiveMeasurement;
import de.willuhn.jameica.sensors.service.RRD;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Servlet, welches aufgerufen wird, um die Chartdaten on-the-fly zu rendern.
 * Im HTTP-Request <i>muessen</i> folgende Parameter gesetzt sein:
 * Name  : Wert
 * device: UUID des Devices
 * group : UUID der Sensor-Group
 * 
 * Die UUIDs kann man sich z.Bsp. via {@link LiveMeasurement#getValues()} holen.
 * 
 * Ausserdem <i>koennen</i> folgende Parameter uebergeben werden.
 * Name  : Wert
 * from  : Startdatum als UNIX-Timestamp
 * to    : Enddatum als UNIX-Timestamp
 * 
 * Wichtig: UNIX-Timestamp sind Epochen-<i>Sekunden</i>. Nicht Millisekunden.
 * Also den Wert von {@link Date#getTime()} vorher noch durch 1000 teilen.  
 * Sind diese Parameter angegeben, wird im Chart nur der betreffende
 * Zeitraum gerendert.
 */
public class ChartServlet extends HttpServlet
{
  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   * Wir ueberschreiben "service" statt "doGet" weil uns egal ist, ob wir
   * die Daten via GET oder POST oder sonstwie kriegen.
   */
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String device = request.getParameter("device");
    if (device == null || device.length() == 0)
      throw new ServletException("parameter 'device' missing");

    String group = request.getParameter("group");
    if (group == null || group.length() == 0)
      throw new ServletException("parameter 'group' missing");
    
    // Checken, ob optionale Parameter vorhanden sind
    String from = request.getParameter("from");
    Date start = null;
    if (from != null && from.length() > 0)
    {
      try {
        start = new Date(Long.parseLong(from) * 1000L); // vorher wieder in Millis umrechnen
      }
      catch (Exception e) {
        throw new ServletException("invalid parameter 'from', value: " + from);
      }
    }
  
    String to = request.getParameter("to");
    Date end = null;
    if (to != null && to.length() > 0)
    {
      try {
        end = new Date(Long.parseLong(to) * 1000L);
      }
      catch (Exception e) {
        throw new ServletException("invalid parameter 'to', value: " + to);
      }
    }
    
    
    // OK. Jetzt brauchen wir die Objekte. Die holen wir uns vom
    // LiveMeasurement. Irgendwann koennte man noch die Device-API
    // so erweitern, dass man sie von dort abfragen kann. Ist
    // sinnvoll, wenn Daten aktuell nicht mehr abgefragt werden
    // aber das RRD-Archiv noch da ist
    Device d      = null;
    Sensorgroup g = null;
    Map<Device,Measurement> values = LiveMeasurement.getValues();
    Iterator<Device> it = values.keySet().iterator();
    while (it.hasNext() && g == null)
    {
      d = it.next();
      if (device.equals(d.getUuid()))
      {
        // Device passt schonmal. Mal schauen, ob wir die 
        // Gruppe finden
        Measurement m = values.get(d);
        if (m == null)
          throw new ServletException("sensorgroup not found for device"); // TODO: Wenn wir die Daten von der Device-API kriegen, muss das hier nicht mehr sein
        List<Sensorgroup> groups = m.getSensorgroups();
        for (Sensorgroup sg:groups)
        {
          if (group.equals(sg.getUuid()))
          {
            // gefunden
            g = sg;
            break;
          }
        }
      }
    }

    if (g == null)
      throw new ServletException("sensorgroup not found for device");
    

    // So, koemmer jetzt endlich? ;)
    try
    {
      RRD rrd = (RRD) Application.getServiceFactory().lookup(Plugin.class,"rrd");
      byte[] image = rrd.renderGroup(d,g,start,end);
      
      response.setContentType("image/png"); // Die Information sollte vom RRD-Service kommen
      response.setContentLength(image.length);
      OutputStream os = response.getOutputStream();
      os.write(image);
      os.flush();
      
      // TODO: Muss ich das machen oder uebernimmt das Jetty?
      os.close();
    }
    catch (Exception e)
    {
      Logger.error("unable to render image",e);
      throw new ServletException("error while rendering image");
    }
  }

}


/**********************************************************************
 * $Log: ChartServlet.java,v $
 * Revision 1.1  2009/08/22 00:03:42  willuhn
 * @N Das Zeichnen der Charts funktioniert! ;)
 *
 **********************************************************************/

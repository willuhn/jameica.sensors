/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/controller/Status.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/19 10:34:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.controller;

import java.util.Map;

import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.messaging.LiveMeasurement;
import de.willuhn.jameica.webadmin.annotation.Lifecycle;
import de.willuhn.jameica.webadmin.annotation.Lifecycle.Type;

@Lifecycle(Type.SESSION)
public class Status
{
  /**
   * Liefert die Live-Messwerte.
   * @return die Live-Messwerte.
   */
  public Map<Device,Measurement> getMeasurements()
  {
    return LiveMeasurement.getValues();
  }
}


/**********************************************************************
 * $Log: Status.java,v $
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/

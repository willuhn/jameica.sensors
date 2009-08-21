/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Sensorgroup.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/21 13:34:17 $
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

/**
 * Interface fuer eine Sensor-Gruppe.
 */
public class Sensorgroup
{
  private String name = null;
  
  private List<Sensor> sensors = null;
  
  /**
   * Liefert einen sprechenden Namen fuer die Sensor-Gruppe.
   * @return sprechender Name der Sensor-Gruppe.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Speichert einen sprechenden Namen fuer die Sensor-Gruppe.
   * @param name sprechender Name fuer die Sensor-Gruppe.
   */
  public void setName(String name)
  {
    this.name = name; 
  }
  
  /**
   * Liefert die Liste der Sensoren dieser Gruppe.
   * @return die Liste der Sensoren dieser Gruppe.
   */
  public List<Sensor> getSensors()
  {
    if (this.sensors == null)
      this.sensors = new ArrayList<Sensor>();
    return this.sensors;
  }
}


/**********************************************************************
 * $Log: Sensorgroup.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/

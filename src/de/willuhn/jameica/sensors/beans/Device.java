/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Device.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/20 18:07:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.beans;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Bean fuer die Persistierung eines Devices (bzw. der Messwerte.
 */
@Entity
@Table(name="device")
public class Device
{
  @Id
  private String id = null;
  
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY,mappedBy="device")
  private List<Measurement> measurements = null;

  /**
   * Liefert die ID des Devices.
   * @return ID des Devices.
   */
  public String getId()
  {
    return this.id;
  }

  /**
   * Speichert die ID des Devices.
   * @param id ID des Devices.
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Liefert alle Messergebnisse.
   * @return alle Messergebnisse.
   */
  public List<Measurement> getMeasurements()
  {
    return this.measurements;
  }

}


/**********************************************************************
 * $Log: Device.java,v $
 * Revision 1.2  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.1  2009/08/19 23:46:28  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/

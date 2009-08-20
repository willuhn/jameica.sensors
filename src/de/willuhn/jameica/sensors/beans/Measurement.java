/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Attic/Measurement.java,v $
 * $Revision: 1.3 $
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

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Container fuer eine Messung.
 */
@Entity
@Table(name="measurement")
public class Measurement
{
  @Id
  @GeneratedValue
  private Long id = null;
  
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name = "device_id", nullable=false)
  private Device device = null;
  
  @Temporal(TemporalType.TIME)
  private Date date = null;

  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY,mappedBy="measurement")
  private List<Valuegroup> valueGroups = null;
  
  /**
   * Liefert die Liste der Werte-Gruppen.
   * @return Liste der Werte-Gruppen.
   */
  public List<Valuegroup> getValueGroups()
  {
    return this.valueGroups;
  }
  
  /**
   * Speichert die Liste der Werte-Gruppen.
   * @param list Liste der Werte-Gruppen.
   */
  public void setValueGroups(List<Valuegroup> list)
  {
    this.valueGroups = list;
  }
  
  /**
   * Liefert den Zeitpunkt der Messung.
   * @return Zeitpunkt der Messung.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Speichert den Zeitpunkt der Messung.
   * @param date Zeitpunkt der Messung.
   */
  public void setDate(Date date)
  {
    this.date = date;
  }

  /**
   * Liefert die ID der Messung.
   * @return ID der Messung.
   */
  public Long getId()
  {
    return this.id;
  }

  /**
   * Speichert die ID der Messung.
   * @param id ID der Messung.
   */
  public void setId(Long id)
  {
    this.id = id;
  }

  /**
   * Liefert das Device.
   * @return das Device.
   */
  public Device getDevice()
  {
    return this.device;
  }

  /**
   * Speichert das Device.
   * @param device das Device.
   */
  public void setDevice(Device device)
  {
    this.device = device;
  }
  
  
}


/**********************************************************************
 * $Log: Measurement.java,v $
 * Revision 1.3  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.2  2009/08/19 23:46:28  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 * Revision 1.1  2009/08/18 16:29:19  willuhn
 * @N DIE SCHEISSE GEHT! ;)
 *
 **********************************************************************/

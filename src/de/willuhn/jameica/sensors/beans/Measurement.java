/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Attic/Measurement.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/08/20 22:08:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
  
  @Temporal(TemporalType.TIME)
  private Date date = null;

  @OneToMany(cascade=CascadeType.ALL)
  @JoinColumn(name="measurement_id")
  private List<Valuegroup> valuegroups = null;
  
  /**
   * Liefert die Liste der Werte-Gruppen.
   * @return Liste der Werte-Gruppen.
   */
  public List<Valuegroup> getValuegroups()
  {
    if (this.valuegroups == null)
      this.valuegroups = new ArrayList<Valuegroup>();
    return this.valuegroups;
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
}


/**********************************************************************
 * $Log: Measurement.java,v $
 * Revision 1.4  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
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

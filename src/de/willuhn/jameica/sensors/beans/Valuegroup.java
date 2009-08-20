/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Attic/Valuegroup.java,v $
 * $Revision: 1.2 $
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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Gruppiert eine Liste von Messwerten.
 * Hintergrund: Die Messwerte der Messgeraete sind meist thematisch
 * gruppiert (Heizung, Warmwasser, Kuehlung, Kompressor, etc).
 * Damit die Messwerte in einer GUI strukturiert angezeigt werden
 * koennen, gruppiert sie diese Klasse thematisch.
 */
@Entity
@Table(name="valuegroup", uniqueConstraints = {
    @UniqueConstraint(columnNames="uuid")
})
public class Valuegroup
{
  @Id
  @GeneratedValue
  private Long id = null;
  
  private String uuid = null;
  
  @OneToMany(cascade=CascadeType.ALL)
  @JoinColumn(name="valuegroup_id")
  private List<Value> values = null;
  
  @Transient
  private transient String name = null;

  /**
   * Liefert die Liste der Messwerte.
   * @return Liste der Messwerte.
   */
  public List<Value> getValues()
  {
    if (this.values == null)
      this.values = new ArrayList<Value>();
    return this.values;
  }
  
  /**
   * Liefert die ID der Wertegruppe.
   * @return ID der Wertegruppe.
   */
  public Long getId()
  {
    return this.id;
  }

  /**
   * Liefert einen sprechenden Namen.
   * @return sprechender Name.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Speichert den sprechenden Namen.
   * @param name sprechender Name.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Liefert die eindeutige Kennung des Devices.
   * @return UUID des Devices.
   */
  public String getUuid()
  {
    return this.uuid;
  }

  /**
   * Speichert die eindeutige Kennung des Devices.
   * @param uuid die UUID des Devices.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }
}


/**********************************************************************
 * $Log: Valuegroup.java,v $
 * Revision 1.2  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.1  2009/08/20 18:07:43  willuhn
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
 **********************************************************************/

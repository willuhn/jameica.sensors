/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Value.java,v $
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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.willuhn.logging.Logger;

/**
 * Bean fuer einen einzelnen Mess-Wert.
 */
@Entity
@Table(name="value")
public class Value<T extends Serializable>
{
  /**
   * Legt fest, von welchem Typ der Parameter ist.
   */
  public static enum Type
  {
    STRING,
    TEMPERATURE,
    DATE,
  }

  @Id
  @GeneratedValue
  private Long id = null;
  
  @Column(name="value")
  protected String serialized = null;

  @Enumerated(EnumType.STRING)
  private Type type = null;

  // Brauchen wir nicht in der Datenbank
  @Transient
  private transient String name = null;
  
  @Transient
  private transient T value = null;

  /**
   * Liefert einen sprechenden Namen fuer den Messwert.
   * @return Sprechender Name fuer den Messwert.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Speichert einen sprechenden Namen fuer den Messwert.
   * @param name Sprechender Name fuer den Messwert.
   */
  public void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * Liefert den gemessenen Wert.
   * @return der Messwert.
   * Typ abhaengig von der Implementierung.
   */
  public T getValue()
  {
    return this.value;
  }
  
  /**
   * Speichert den Messwert.
   * @param value der Messwert.
   */
  public void setValue(T value)
  {
    this.value = value;
  }
  
  /**
   * Liefert den Typ des Messwertes.
   * @return Typ des Messwertes.
   */
  public Type getType()
  {
    return this.type;
  }
  
  /**
   * Speichert den Typ des Messwertes.
   * @param type Typ des Messwertes.
   */
  public void setType(Type type)
  {
    this.type = type;
  }
  
  /**
   * Liefert die ID des Messwertes.
   * @return ID des Messwertes.
   */
  public Long getId()
  {
    return this.id;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.value == null ? null : this.value.toString();
  }
  
  /**
   * Serialisiert den Messwert.
   */
  @PrePersist
  public void serialize()
  {
    this.serialized = (this.value == null ? null : this.value.toString());
  }
  
  /**
   * Deserialisiert den Wert.
   * Sollte von abgeleiteten Klassen ueberschrieben werden.
   */
  @PostLoad
  public void unserialize()
  {
    if (this.value == null)
      return;
    
    if (this.value instanceof String)
      this.value = (T) this.serialized;
    Logger.warn("dont know how to unserialize type " + this.value.getClass().getName());
  }
  
}


/**********************************************************************
 * $Log: Value.java,v $
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
 * Revision 1.2  2009/08/19 00:43:06  willuhn
 * @N hibernate fuer Persistierung
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/

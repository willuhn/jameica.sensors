/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

/**
 * Interface eines einzelnen Sensors.
 * @param <T> der Typ des Sensors.
 */
public class Sensor<T> implements UniqueItem, Cloneable
{
  /**
   * Default-Typ von Messwerten.
   */
  public final static Type TYPE_DEFAULT = Type.GAUGE;
  
  private T value     = null;
  private String name = null;
  private String uuid = null;
  private Type type   = TYPE_DEFAULT;
  
  private Class<? extends Serializer> serializer = StringSerializer.class;
  
  /**
   * Typ des Messwertes.
   * Siehe auch http://wiki.secitec.net/doku.php?id=tutorials:rrdtool
   * bzw. http://oss.oetiker.ch/rrdtool/tut/rrd-beginners.en.html
   */
  public enum Type
  {
    /**
     * Speichert keine Veränderungen pro Zeitraum sondern die aktuellen Werte, 
     * ohne irgendwelche Divisionen oder dergleichen.
     * Das ist der Default-Wert.
     */
    GAUGE,
    
    /**
     * Ansteigender Wert, der die Veränderungen über den Zeitraum zum vorherigen
     * Wert speichert, z.B. Traffic-Counter bei einem Router.
     */
    COUNTER,
    
    /**
     * Aehnlich zu COUNTER, nur werden auch negative Werte erlaubt
     * (Z.Bsp. Veränderung von Festplattenspeicher).
     */
    DERIVE,
    
    /**
     * Speichert ebenfalls die Veränderung über den Zeitraum, allerdings wird der
     * vorherige Wert als 0 angenommen. Es speichert also nur den derzeitigen
     * Wert, dividiert durch das Stepintervall.
     */
    ABSOLUTE
  }
  
  /**
   * Liefert einen sprechenden Namen fuer den Sensor.
   * @return sprechender Name des Sensors.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Speichert einen sprechenden Namen fuer den Sensor.
   * @param name sprechender Name fuer den Sensor.
   */
  public void setName(String name)
  {
    this.name = name; 
  }

  /**
   * Liefert den Messwert.
   * @return der Messwert.
   */
  public T getValue()
  {
    return this.value;
  }
  
  /**
   * Speichert den Messwert.
   * @param value
   */
  public void setValue(T value)
  {
    this.value = value;
  }
  
  /**
   * Liefert den zu verwendenden Serializer fuer die Messwerte.
   * @return der zu verwendende Serializer fuer die Messwerte.
   */
  public Class<? extends Serializer> getSerializer()
  {
    return this.serializer;
  }
  
  /**
   * Speichert den Serializer.
   * @param serializer der Serializer.
   */
  public void setSerializer(Class<? extends Serializer> serializer)
  {
    this.serializer = serializer;
  }
  
  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    return this.uuid;
  }
  
  /**
   * Speichert die eindeutige ID fuer das Objekt.
   * Diese ID sollte sich niemals aendern, da sich sonst bereits
   * archivierte Messwerte nicht mehr diesem Objekt zuordnen lassen.
   * @param uuid die eindeutige ID fuer das Objekt.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
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
   * @see java.lang.Object#clone()
   */
  @SuppressWarnings("javadoc")
  public Object clone()
  {
    try
    {
      Sensor clone = (Sensor) super.clone();
      // Da alle properties primitiv bzw. immutable sind, muessen wir
      // nichts manuell clonen - das macht alles Java selbst
      return clone;
    }
    catch (CloneNotSupportedException e)
    {
      throw new RuntimeException(e);
    }
  }

}

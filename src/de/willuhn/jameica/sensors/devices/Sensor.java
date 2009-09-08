/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Sensor.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/09/08 10:38:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

/**
 * Interface eines einzelnen Sensors.
 */
public class Sensor<T> implements UniqueItem
{
  private T value     = null;
  private String name = null;
  private String uuid = null;
  private Class<? extends Serializer> serializer = StringSerializer.class;
  
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
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

}


/**********************************************************************
 * $Log: Sensor.java,v $
 * Revision 1.2  2009/09/08 10:38:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/

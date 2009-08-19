/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Attic/ValueGroup.java,v $
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

package de.willuhn.jameica.sensors.beans;

import java.util.List;

/**
 * Gruppiert eine Liste von Messwerten.
 * Hintergrund: Die Messwerte der Messgeraete sind meist thematisch
 * gruppiert (Heizung, Warmwasser, Kuehlung, Kompressor, etc).
 * Damit die Messwerte in einer GUI strukturiert angezeigt werden
 * koennen, gruppiert sie diese Klasse thematisch.
 */
public class ValueGroup
{
  private String name        = null;
  private List<Value> values = null;
  
  /**
   * Liefert einen sprechenden Namen fuer die Messwert-Gruppe.
   * @return Sprechender Name fuer die Messwert-Gruppe.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Speichert einen sprechenden Namen fuer die Messwert-Gruppe.
   * @param name Sprechender Name fuer die Messwert-Gruppe.
   */
  public void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * Liefert die Liste der Messwerte.
   * @return Liste der Messwerte.
   */
  public List<Value> getValues()
  {
    return this.values;
  }
  
  /**
   * Speichert die Liste der Messwerte.
   * @param values Liste der Messwerte.
   */
  public void setValues(List<Value> values)
  {
    this.values = values;
  }

}


/**********************************************************************
 * $Log: ValueGroup.java,v $
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/

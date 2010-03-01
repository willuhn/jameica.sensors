/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/Attic/SmallerThanOperator.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/01 13:16:12 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify;

/**
 * Implementierung eines Operators, der dann ausloest, wenn
 * der Messwert das Limit unterschritten hat.
 */
public class SmallerThanOperator implements Operator
{
  /**
   * @see de.willuhn.jameica.sensors.notify.Operator#matches(java.lang.Object, java.lang.Object)
   */
  public boolean matches(Object value, Object limit)
  {
    if (value == null || limit == null)
      return false;

    Comparable cValue = null;
    if (value instanceof Comparable) cValue = (Comparable) value;
    else                             cValue = value.toString();
    
    Comparable cLimit = null;
    if (limit instanceof Comparable) cLimit = (Comparable) limit;
    else                             cLimit = limit.toString();
    
    
    return cValue.compareTo(cLimit) < 0;
  }

}



/**********************************************************************
 * $Log: SmallerThanOperator.java,v $
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
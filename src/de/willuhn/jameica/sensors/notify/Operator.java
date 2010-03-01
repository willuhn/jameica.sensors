/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/Attic/Operator.java,v $
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
 * Interface, welches entscheidet, ob der Sensor-Wert eine Benachrichtigung
 * ausloesen soll oder nicht.
 * Die Implementierungen sind typischerweise einfache mathematische
 * Operatoren wie "kleiner als", "groesser als" oder "gleich".
 */
public interface Operator
{
  /**
   * Prueft, ob der Messwert das Testkriterium erfuellt.
   * Falls die Implementierung beispielsweise pruefen soll, ob ein
   * Maximalwert ueberschritten ist, muss sie dann "true" zurueckliefern,
   * wenn "value" groesser als "limit" ist.
   * @param value der aktuelle Messwert.
   * @param test der festgelegte Grenzwert.
   * @return true, wenn eine Benachrichtigung erfolgen soll.
   */
  public boolean matches(Object value, Object limit);
}



/**********************************************************************
 * $Log: Operator.java,v $
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
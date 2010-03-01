/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/notifier/Notifier.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/03/01 23:51:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify.notifier;

import java.util.Map;

/**
 * Interface fuer die verschiedenen Benachrichtigungsarten (Mail, Log, etc.).
 */
public interface Notifier
{
  /**
   * Wird aufgerufen, wenn der Sensor einen Wert ausserhalb des Limits geliefert hat.
   * Die Implementierung muss dann hier die Benachrichtigung versenden.
   * @param Betreff-Text.
   * @param Beschreibungstext.
   * @param optionale Zustell-Parameter gemaess "params" der XML-Regeldatei.
   * @param again true, wenn das Limit schon bei der letzten Messung ueberschritten
   * wurde. Der Notifier kann dann selbst entscheiden, ob er die Benachrichtigung
   * nochmal sendet oder nicht.
   * @throws Exception
   */
  public void outsideLimit(String subject, String description, Map<String,String> params, boolean again) throws Exception;
  
  /**
   * Wird einmalig aufgerufen, wenn der Sensor vorher ausserhalb des Limit
   * war und jetzt zurueckgekehrt ist.
   * @param Betreff-Text.
   * @param Beschreibungstext.
   * @param optionale Zustell-Parameter gemaess "params" der XML-Regeldatei.
   * @throws Exception
   */
  public void insideLimit(String subject, String description, Map<String,String> params) throws Exception;
}



/**********************************************************************
 * $Log: Notifier.java,v $
 * Revision 1.2  2010/03/01 23:51:07  willuhn
 * @N Benachrichtigung, wenn Sensor zurueck im normalen Bereich ist
 * @N Merken des letzten Notify-Status, sodass nur beim ersten mal eine Mail gesendet wird
 *
 * Revision 1.1  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2010/03/01 17:08:18  willuhn
 * @N Mail-Benachrichtigung via javax.mail
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
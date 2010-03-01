/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/notifier/Notifier.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/01 18:12:23 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify.notifier;

import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.notify.Rule;

/**
 * Interface fuer die verschiedenen Benachrichtigungsarten (Mail, Log, etc.).
 */
public interface Notifier
{
  /**
   * Wird aufgerufen, wenn der Sensor einen Wert geliefert hat, der gemaess
   * Konfiguration zu einer Benachrichtigung fuehren soll.
   * Die Implementierung muss dann hier die Benachrichtigung versenden.
   * @param sensor der ausloesende Sensor.
   * @param rule die ausloesende Regel.
   * @throws Exception
   */
  public void notify(Sensor sensor, Rule rule) throws Exception;
}



/**********************************************************************
 * $Log: Notifier.java,v $
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
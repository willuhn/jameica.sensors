/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/Attic/Notifier.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/01 13:16:12 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify;

import java.util.Map;

import de.willuhn.jameica.sensors.devices.Sensor;

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
   * @param params optionale Zustell-Parameter. Hier kann z.Bsp. die
   * Mailadresse des Empfaengers oder der Benachrichtigungstext enthalten sein. 
   */
  public void notify(Sensor sensor, Map<String,String> params);
}



/**********************************************************************
 * $Log: Notifier.java,v $
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
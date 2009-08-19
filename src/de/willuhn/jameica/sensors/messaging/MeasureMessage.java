/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/messaging/MeasureMessage.java,v $
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

package de.willuhn.jameica.sensors.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.devices.Device;

/**
 * Message, die vom Scheduler-Service verschickt wird, wenn er neue Messwerte geholt hat.
 * Die Message kann abonniert werden, um die Messwerte zu archivieren, oder um die
 * aktuellen Werte auf einem Webfrontend anzuzeigen.
 */
public class MeasureMessage implements Message
{
  private Device device           = null;
  private Measurement measurement = null;
  
  /**
   * ct.
   * @param device das Geraet, von dem die Messwerte stammen.
   * @param measurement die Messung.
   */
  public MeasureMessage(Device device, Measurement measurement)
  {
    this.device      = device;
    this.measurement = measurement;
  }

  /**
   * Liefert das Geraet, von dem die Messwerte stammen.
   * @return das Geraet, von dem die Messwerte stammen.
   */
  public Device getDevice()
  {
    return this.device;
  }

  /**
   * Liefert die Messwerte.
   * @return die Messwerte.
   */
  public Measurement getMeasurement()
  {
    return this.measurement;
  }
}


/**********************************************************************
 * $Log: MeasureMessage.java,v $
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/

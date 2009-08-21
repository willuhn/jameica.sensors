/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/RRD.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/21 18:07:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.Service;

/**
 * Service, der die RRD-Charts erzeugt.
 */
public interface RRD extends Service
{
  /**
   * Erzeugt eine Chartgrafik fuer eine Sensor-Gruppe.
   * @param deviceId UUID des Devices.
   * @param groupId UUID der Sensor-Gruppe.
   * @param start Start-Datum.
   * @param end End-Datum.
   * @return die erzeugte Grafik im PNG-Format.
   * @throws RemoteException wenn es zu einem Fehler kam oder keine Daten vorliegen.
   */
  public byte[] renderGroup(String deviceId, String groupId,Date start, Date end) throws RemoteException;

  /**
   * Erzeugt eine Chartgrafik fuer einen einzelnen Sensor.
   * @param deviceId UUID des Devices.
   * @param sensordId UUID der Sensor-Gruppe.
   * @param start Start-Datum.
   * @param end End-Datum.
   * @return die erzeugte Grafik im PNG-Format.
   * @throws RemoteException wenn es zu einem Fehler kam oder keine Daten vorliegen.
   */
  public byte[] renderSensor(String deviceId, String groupId,Date start, Date end) throws RemoteException;
}


/**********************************************************************
 * $Log: RRD.java,v $
 * Revision 1.2  2009/08/21 18:07:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/21 17:27:37  willuhn
 * @N RRD-Service
 *
 **********************************************************************/

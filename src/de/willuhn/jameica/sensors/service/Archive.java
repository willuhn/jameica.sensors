/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/Archive.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/21 13:34:17 $
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

import de.willuhn.datasource.Service;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;

/**
 * Der Archiv-Service.
 */
public interface Archive extends Service
{
  /**
   * Archiviert eine Messung fuer das angegebene Device.
   * @param device das Geraet.
   * @param m die Messung.
   * @throws RemoteException
   */
  public void archive(Device device, Measurement m) throws RemoteException;

}


/**********************************************************************
 * $Log: Archive.java,v $
 * Revision 1.2  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.1  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/

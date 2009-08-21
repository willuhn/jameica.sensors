/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/UniqueItem.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/21 13:34:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

import de.willuhn.jameica.sensors.util.UUIDUtil;

/**
 * Interface fuer ein eindeutig identifizierbares Objekt.
 */
public interface UniqueItem
{
  /**
   * Liefert eine eindeutige ID fuer das Objekt.
   * Diese ID sollte sich niemals aendern, da sich sonst bereits
   * archivierte Messwerte nicht mehr diesem Objekt zuordnen lassen.
   * Ob es sich hier tatsaechlich um eine "echte" UUID handelt,
   * (die z.Bsp. mit {@link UUIDUtil#create(String)} erzeugt wurde)
   * oder einen anderen String, spielt keine Rolle. Hauptsache es
   * ist eindeutig und aendert sich nicht.
   * @return eindeutige ID des Objektes.
   */
  public String getUuid();
}


/**********************************************************************
 * $Log: UniqueItem.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/

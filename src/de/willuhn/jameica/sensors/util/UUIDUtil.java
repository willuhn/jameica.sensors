/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/util/Attic/UUIDUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/20 22:08:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.util;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import de.willuhn.logging.Logger;

/**
 * Statische Hilfsklasse zum Erzeugen von UUIDs.
 */
public class UUIDUtil
{
  private final static String ENCODING = "iso-8859-1";
  
  /**
   * Erzeugt aus einem uebergebenen String immer wieder die gleiche UUID.
   * @param uniqueName eindeutiger Name.
   * @return die erzeugte UUID.
   */
  public static String create(String uniqueName)
  {
    byte[] data = null;
    try
    {
      data = uniqueName.getBytes(ENCODING);
    }
    catch (UnsupportedEncodingException e)
    {
      Logger.error("charset " + ENCODING + " not supported on this plattform, fallback to system default");
    }

    if (data == null)
      data = uniqueName.getBytes();

    return UUID.nameUUIDFromBytes(data).toString();
  }
}


/**********************************************************************
 * $Log: UUIDUtil.java,v $
 * Revision 1.1  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 **********************************************************************/

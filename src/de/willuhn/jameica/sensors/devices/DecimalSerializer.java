/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/DecimalSerializer.java,v $
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Serialisiert Dezimal-Werte.
 */
public class DecimalSerializer extends StringSerializer
{
  private static NumberFormat NF = DecimalFormat.getInstance();

  /**
   * @see de.willuhn.jameica.sensors.devices.StringSerializer#unserialize(java.lang.String)
   */
  public Object unserialize(String s)
  {
    if (s == null || s.length() == 0)
      return null;
    try
    {
      return NF.parseObject(s);
    }
    catch (ParseException e)
    {
      throw new IllegalArgumentException(e);
    }
  }

}


/**********************************************************************
 * $Log: DecimalSerializer.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/

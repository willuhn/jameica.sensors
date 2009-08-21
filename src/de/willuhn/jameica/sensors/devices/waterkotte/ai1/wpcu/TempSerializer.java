/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/waterkotte/ai1/wpcu/TempSerializer.java,v $
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

package de.willuhn.jameica.sensors.devices.waterkotte.ai1.wpcu;

import java.text.DecimalFormat;

import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.system.Application;

/**
 * Ein einzelner Temperatur-Wert.
 */
public class TempSerializer extends StringSerializer
{
  public static DecimalFormat DECIMALFORMAT = (DecimalFormat) DecimalFormat.getInstance(Application.getConfig().getLocale());
  
  static
  {
    DECIMALFORMAT.applyPattern("##0.00");
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#format(java.lang.Object)
   */
  public String format(Object value)
  {
    return value == null ? "-" : DECIMALFORMAT.format(value) + " °C";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#unserialize(java.lang.String)
   */
  public Object unserialize(String s) throws IllegalArgumentException
  {
    if (s == null || s.length() == 0)
      return null;
    
    return Float.parseFloat(s);
  }
}


/**********************************************************************
 * $Log: TempSerializer.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/

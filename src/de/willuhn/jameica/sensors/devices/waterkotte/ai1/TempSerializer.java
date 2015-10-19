/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1;

import java.text.DecimalFormat;

import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.system.Application;

/**
 * Ein einzelner Temperatur-Wert.
 */
public class TempSerializer extends StringSerializer
{
  /**
   * Dezimal-Format fuer Temperatur-Angaben.
   */
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

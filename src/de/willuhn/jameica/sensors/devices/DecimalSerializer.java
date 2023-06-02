/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

import java.text.DecimalFormat;
import java.text.NumberFormat;

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
      // Erstmal mit Punkt als Dezimaltrenner versuchen
      return Double.valueOf(Double.parseDouble(s));
    }
    catch (Exception e)
    {
      // Fallback
      try
      {
        return NF.parseObject(s);
      }
      catch (Exception e2)
      {
        throw new IllegalArgumentException(e);
      }
    }
  }

}

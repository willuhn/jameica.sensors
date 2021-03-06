/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1;


/**
 * Ein einzelner Temperatur-Wert.
 */
public class TempSerializer extends DecimalSerializer
{
  /**
   * @see de.willuhn.jameica.sensors.devices.waterkotte.ai1.DecimalSerializer#format(java.lang.Object)
   */
  @Override
  public String format(Object value)
  {
    return value == null ? "-" : (super.format(value) + " �C");
  }
}

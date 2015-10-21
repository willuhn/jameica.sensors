/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1;


/**
 * Serializer fuer einen einheitenlosen.
 */
public class KwSerializer extends DecimalSerializer
{
  /**
   * @see de.willuhn.jameica.sensors.devices.waterkotte.ai1.DecimalSerializer#format(java.lang.Object)
   */
  @Override
  public String format(Object value)
  {
    return value == null ? "-" : (super.format(value) + " KW");
  }
}

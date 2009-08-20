/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/waterkotte/ai1/wpcu/values/Attic/TempValue.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/20 22:08:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1.wpcu.values;

import java.io.DataInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.system.Application;

/**
 * Ein einzelner Temperatur-Wert.
 */
public class TempValue extends Value<Float>
{
  public static DecimalFormat DECIMALFORMAT = (DecimalFormat) DecimalFormat.getInstance(Application.getConfig().getLocale());
  
  static
  {
    DECIMALFORMAT.applyPattern("##0.00");
  }

  /**
   * ct.
   * @param name Name des Messwertes.
   * @param data Stream, aus dem der Wert gelesen wird.
   * @param offset Offset, ab dem gelesen wird.
   * @throws IOException
   */
  public TempValue(String name, DataInputStream data, int offset) throws IOException
  {
    this.setName(name);

    try
    {
      // Wir markieren den Startpunkt - auf den springen wir dann wieder zurueck
      data.mark(-1); // ist unten ein ByteArrayInputStream - da wird das eh ignoriert ;)

      // Wir springen an die gewuenschte Position
      data.skipBytes(offset);
      this.setValue(data.readFloat());
    }
    finally
    {
      // An den Anfang zurueckspringen
      data.reset();
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    Float value = this.getValue();
    if (value == null)
      return "-";
    
    return DECIMALFORMAT.format(value) + " °C";
  }

  /**
   * @see de.willuhn.jameica.sensors.beans.Value#getType()
   */
  public de.willuhn.jameica.sensors.beans.Value.Type getType()
  {
    return Type.TEMPERATURE;
  }

  /**
   * @see de.willuhn.jameica.sensors.beans.Value#unserialize()
   */
  public void unserialize()
  {
    setValue(Float.parseFloat(this.serialized));
  }
  
  
}


/**********************************************************************
 * $Log: TempValue.java,v $
 * Revision 1.2  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.2  2009/08/18 23:40:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/

/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Attic/Measurement.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/19 10:34:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.beans;

import java.util.Date;
import java.util.List;

/**
 * Container fuer eine Messung.
 */
public class Measurement
{
  private Date date             = new Date();
  private List<ValueGroup> list = null;
  
  /**
   * Liefert die Liste der Werte-Gruppen.
   * @return Liste der Werte-Gruppen.
   */
  public List<ValueGroup> getValueGroups()
  {
    return this.list;
  }
  
  /**
   * Speichert die Liste der Werte-Gruppen.
   * @param list Liste der Werte-Gruppen.
   */
  public void setValueGroups(List<ValueGroup> list)
  {
    this.list = list;
  }
  
  /**
   * Liefert den Zeitpunkt der Messung.
   * @return Zeitpunkt der Messung.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Speichert den Zeitpunkt der Messung.
   * @param date Zeitpunkt der Messung.
   */
  public void setDate(Date date)
  {
    this.date = date;
  }
}


/**********************************************************************
 * $Log: Measurement.java,v $
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 * Revision 1.1  2009/08/18 16:29:19  willuhn
 * @N DIE SCHEISSE GEHT! ;)
 *
 **********************************************************************/

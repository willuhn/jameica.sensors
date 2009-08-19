/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Device.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/19 23:46:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

import java.io.IOException;

import de.willuhn.jameica.sensors.beans.Measurement;

/**
 * Basis-Interface, welches von einem Messgeraet implementiert werden muss.
 * Implementierungen muessen der Bean-Konvention entsprechen und einen
 * parameterlosen Konstruktor mit public-Modifier besitzen.
 */
public interface Device
{
  /**
   * Fuehrt eine Messung auf dem Geraet durch und liefert die Messwerte.
   * @return die Messwerte.
   * @throws IOException
   */
  public Measurement collect() throws IOException;
  
  /**
   * Liefert true, wenn die Messwerte des Geraetes regelmaessig abgefragt werden sollen.
   * @return true, wenn die Messwerte des Geraetes regelmaessig abgefragt werden sollen.
   */
  public boolean isEnabled();
  
  /**
   * Liefert eine Bezeichung fuer das Geraet.
   * @return Bezeichnung des Geraetes.
   */
  public String getName();
  
  /**
   * Liefert eine ID fuer dieses Device, die global (also ueber alle installierten)
   * Devices eindeutig sein muss.
   * Anhand dieser ID werden die Messwerte in der Datenbank dem Geraet zugeordnet.
   * Die ID darf also niemals geaendert werden, sonst koennen die bisherigen
   * Messwerte in der Datenbank nicht mehr diesem Geraet zugeordnet werden.
   * Am sinnvollsten ist, hier einfach den kompletten Klassennamen der
   * Implementierung samt Package (also "this.getClass().getName()") zu verwenden. Man
   * kann aber auch einen beliebigen anderen String (max. 255 Zeichen) verwenden.
   * Er darf anschliessend nur nicht mehr veraendert werden.
   * @return eindeutige ID fuer das Device.
   */
  public String getId();
}


/**********************************************************************
 * $Log: Device.java,v $
 * Revision 1.2  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.2  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 * Revision 1.1  2009/08/18 16:29:19  willuhn
 * @N DIE SCHEISSE GEHT! ;)
 *
 **********************************************************************/

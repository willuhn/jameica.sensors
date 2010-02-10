/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/Plugin.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/02/10 13:47:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors;

import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.sensors.web.rest.Sensor;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Basis-Klasse des Plugins.
 */
public class Plugin extends AbstractPlugin
{

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(new QueryMessage(new Sensor()));
  }
}


/**********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.2  2010/02/10 13:47:56  willuhn
 * @N REST-Support zur Abfrage einzelner Werte
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.3  2009/08/02 23:32:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2009/07/20 17:21:49  willuhn
 * @C Haufenweise Code entfernt, Cleanup
 *
 * Revision 1.1  2009/07/20 12:27:40  willuhn
 * @N initial checkin
 *
 **********************************************************************/

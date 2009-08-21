/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/messaging/Attic/ArchiveMessageConsumer.java,v $
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

package de.willuhn.jameica.sensors.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.service.Archive;
import de.willuhn.jameica.system.Application;

/**
 * Abonniert Archiv-Nachrichten und leitet sie an den Archiv-Service weiter.
 */
public class ArchiveMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{MeasureMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    MeasureMessage msg = (MeasureMessage) message;
    
    // Archiv-Service holen
    Archive archive = (Archive) Application.getServiceFactory().lookup(Plugin.class,"archive");
    if (!archive.isStarted())
      return; // Archive laeuft nicht.
    
    archive.archive(msg.getDevice(),msg.getMeasurement());
  }

}


/**********************************************************************
 * $Log: ArchiveMessageConsumer.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/

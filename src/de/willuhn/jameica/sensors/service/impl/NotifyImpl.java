/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service.impl;

import java.rmi.RemoteException;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.notify.RuleProcessor;
import de.willuhn.jameica.sensors.service.Notify;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Service-Implementierung fuer die Benachrichtigungen.
 */
public class NotifyImpl implements Notify
{
  private MessageConsumer mc = null;
  private RuleProcessor processor = null;
  
  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "notify service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.mc != null && this.processor != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service already started, skipping request");
      return;
    }
    this.mc = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    
    this.processor = new RuleProcessor();
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    
    try
    {
      Application.getMessagingFactory().unRegisterMessageConsumer(this.mc);
    }
    finally
    {
      this.mc = null;
      this.processor = null;
    }
  }
  
  
  /**
   * Empfaengt die Mess-Nachrichten.
   */
  private class MyMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
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
      processor.process(((MeasureMessage)message).getMeasurement());
    }
  }

}

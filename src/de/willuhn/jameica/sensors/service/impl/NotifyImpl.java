/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/NotifyImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/01 13:16:12 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service.impl;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.notify.Notifier;
import de.willuhn.jameica.sensors.notify.Operator;
import de.willuhn.jameica.sensors.notify.Rule;
import de.willuhn.jameica.sensors.notify.RuleFinder;
import de.willuhn.jameica.sensors.service.Notify;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Service-Implementierung fuer die Benachrichtigungen.
 */
public class NotifyImpl implements Notify
{
  private MessageConsumer mc = null;
  
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
    return this.mc != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    this.mc = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
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
      Measurement m = ((MeasureMessage) message).getMeasurement();
      List<Rule> rules = new RuleFinder().getRules();
      for (Rule r:rules)
      {
        Sensor s = findSensor(m,r.getSensor());
        if (s == null)
          continue;
        
        handleRule(s,r);
      }
    }
    
    /**
     * Bearbeitet die Benachrichtigungsregel.
     * @param s der Sensor.
     * @param r die Regel.
     */
    private void handleRule(Sensor s,Rule r)
    {
      try
      {
        ////////////////////////////////////////////////////////////////////////
        // NULL-Checks
        String limit = r.getLimit();
        if (limit == null || limit.length() == 0)
        {
          Logger.warn("rule for sensor " + r.getSensor() + " has no limit");
          return;
        }
        
        Notifier n = r.getNotifier();
        if (n == null)
        {
          Logger.warn("rule for sensor " + r.getSensor() + " has no notifier");
          return;
        }
        
        Operator o = r.getOperator();
        if (o == null)
        {
          Logger.warn("rule for sensor " + r.getSensor() + " has no operator");
          return;
        }
        ////////////////////////////////////////////////////////////////////////
        
        Class<? extends Serializer> c = s.getSerializer();
        if (c == null)
          c = StringSerializer.class;
        Serializer serializer = c.newInstance();
        
        Object oLimit = serializer.unserialize(limit);
        Object oValue = s.getValue();
        if (o.matches(oValue,oLimit))
        {
          Logger.info("limit exceeded for sensor " + s.getUuid() + ". current value: " + serializer.format(oValue) + ", limit: " + serializer.format(oLimit) + ". Notifying via " + n.getClass().getName());
          n.notify(s,r.getNotifierParams());
        }
      }
      catch (Exception e)
      {
        Logger.error("error while processing notify rule",e);
      }
    }
    
    /**
     * Durchsucht die Messung nach dem angegebenen Sensor.
     * @param m die Messung.
     * @param uuid der Sensor.
     * @return der Sensor oder NULL, wenn er nicht gefunden wurde.
     */
    private Sensor findSensor(Measurement m, String uuid)
    {
      if (uuid == null || uuid.length() == 0)
      {
        Logger.warn("rule contains no sensor uuid, skipping");
        return null;
      }
      
      List<Sensorgroup> groups = m.getSensorgroups();
      for (Sensorgroup g:groups)
      {
        List<Sensor> sensors = g.getSensors();
        for (Sensor s:sensors)
        {
          if (uuid.equals(s.getUuid()))
            return s;
        }
      }
      
      Logger.warn("sensor uuid " + uuid + " not found in measurement");
      return null;
    }
    
  }

}



/**********************************************************************
 * $Log: NotifyImpl.java,v $
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
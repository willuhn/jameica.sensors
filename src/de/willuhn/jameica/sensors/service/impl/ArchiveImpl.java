/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/ArchiveImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/19 23:46:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.Archive;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Archiv-Services.
 */
public class ArchiveImpl implements Archive
{
  private EntityManager em           = null;
  private MyMessageConsumer consumer = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "archive service";
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
    return this.consumer != null;
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
    
    this.consumer = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
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
      Application.getMessagingFactory().unRegisterMessageConsumer(this.consumer);
      
      if (this.em != null)
        this.em.close();
    }
    finally
    {
      this.consumer = null;
      this.em = null;
    }
  }
  
  /**
   * Archiviert neue Messwerte fuer ein Geraet.
   * @param deviceId ID des Devices.
   * @param m die neuen Messwerte.
   */
  private void archive(String deviceId, Measurement m)
  {
    EntityManager em = getEntityManager();
    
    // Wir suchen erstmal das Device.
    Device d = em.find(Device.class,deviceId);
    if (d == null)
    {
      Logger.info("creating new device entry in archive");
      d = new Device();
      d.setId(deviceId);
    }
    
    d.addMeasurement(m);

    EntityTransaction tx = null;
    try
    {
      tx = em.getTransaction();
      tx.begin();
      em.persist(d);
      tx.commit();
    }
    catch (PersistenceException pe)
    {
      if (tx != null)
        tx.rollback();
      throw pe;
    }
  }
  
  /**
   * Liefert den EntityManager oder erstellt bei Bedarf einen neuen.
   * @return der EntityManager.
   */
  private synchronized EntityManager getEntityManager()
  {
    // TODO
    if (this.em == null)
    {
      Map params = new HashMap();
      params.put(Persistence.PERSISTENCE_PROVIDER,"org.hibernate.ejb.HibernatePersistence");
      params.put("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
      params.put("hibernate.connection.url","jdbc:mysql://server:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1");
      params.put("hibernate.connection.username","jameica_sensors");
      params.put("hibernate.connection.password","jameica_sensors");
      params.put("hibernate.dialect","org.hibernate.dialect.MySqlDialect");
      params.put("hibernate.show_sql","true");
      params.put("hibernate.hbm2ddl.auto","create,update,validate");
      EntityManagerFactory ef = Persistence.createEntityManagerFactory("jameica.sensors",params);
      this.em = ef.createEntityManager();
    }
    return this.em;
  }
  
  /**
   * Mit dem Message-Consumer abonnieren wir die aktuellen Messwerte fuer die Archivierung.
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
      MeasureMessage msg = (MeasureMessage) message;
      archive(msg.getDevice().getId(),msg.getMeasurement());
    }
    
  }

}


/**********************************************************************
 * $Log: ArchiveImpl.java,v $
 * Revision 1.1  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/

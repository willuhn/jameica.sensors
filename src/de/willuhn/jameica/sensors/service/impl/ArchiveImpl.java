/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/ArchiveImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/08/20 23:26:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service.impl;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.beans.Valuegroup;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.Archive;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Archiv-Services.
 */
public class ArchiveImpl implements Archive
{
  private EntityManager entityManager = null;
  private MyMessageConsumer consumer  = null;

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
      
      if (this.entityManager != null)
        this.entityManager.close();
    }
    finally
    {
      this.consumer = null;
      this.entityManager = null;
    }
  }
  
  /**
   * Archiviert neue Messwerte fuer ein Geraet.
   * @param uuid UUID des Devices.
   * @param m die neuen Messwerte.
   */
  private void archive(String uuid, Measurement m)
  {
    EntityManager em = getEntityManager();
    
    
    Device d = null;
    
    try
    {
      // Wir suchen erstmal das Device.
      Query q = em.createQuery("from Device where uuid = ?");
      q.setParameter(1,uuid);
      d = (Device) q.getSingleResult();
    }
    catch (NoResultException e)
    {
      Logger.info("adding new device [uuid: " + uuid + "] to archive");
      d = new Device();
      d.setUuid(uuid);
    }
    
    // Bevor wir die Messergebnisse speichern, muessen wir noch schauen,
    // ob wir die Valuegroups schon im Archiv haben
    List<Valuegroup> groups = m.getValuegroups();
    List<Valuegroup> newList = new ArrayList<Valuegroup>();
    for (Valuegroup group:groups)
    {
      Valuegroup fromArchive = find(group.getUuid());
      if (fromArchive != null)
      {
        // jepp, die Gruppe haben wir schon. Wir haengen die Messwerte dort dran
        List<Value> values = group.getValues();
        List<Value> existing = fromArchive.getValues();
        for (Value value:values)
        {
          existing.add(value);
        }
        newList.add(fromArchive);
      }
      else
      {
        Logger.info("adding new value group [uuid: " + group.getUuid() + "] to archive");
        newList.add(group);
      }
    }
    
    groups.clear();
    groups.addAll(newList);
    
    d.getMeasurements().add(m);

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
   * Sucht die angegebene Valuegroup im Archiv.
   * @param uuid die UUID der Valuegroup.
   * @return die Valuegroup aus dem Archiv oder NULL, wenn sie da noch nicht existiert.
   */
  private Valuegroup find(String uuid)
  {
    try
    {
      EntityManager em = getEntityManager();
      Query q = em.createQuery("from Valuegroup where uuid = ?");
      q.setParameter(1,uuid);
      return (Valuegroup) q.getSingleResult();
    }
    catch (NoResultException e)
    {
      // ignore
    }
    return null;
  }
  
  /**
   * Liefert den EntityManager oder erstellt bei Bedarf einen neuen.
   * @return der EntityManager.
   */
  private synchronized EntityManager getEntityManager()
  {
    if (this.entityManager == null)
    {
      Map params = new HashMap();
      System.out.println("PROVIDER:" + org.hibernate.ejb.HibernatePersistence.PROVIDER);
      params.put("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
      params.put("hibernate.connection.url","jdbc:mysql://server:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1");
      params.put("hibernate.connection.username","jameica_sensors");
      params.put("hibernate.connection.password","jameica_sensors");
      params.put("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
      params.put("hibernate.show_sql","true");
      // params.put("hibernate.hbm2ddl.auto","update"); // create,update,validate
      EntityManagerFactory ef = Persistence.createEntityManagerFactory("jameica_sensors",params);
      this.entityManager = ef.createEntityManager();
    }
    return this.entityManager;
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
      archive(msg.getDevice().getUuid(),msg.getMeasurement());
    }
    
  }

}


/**********************************************************************
 * $Log: ArchiveImpl.java,v $
 * Revision 1.4  2009/08/20 23:26:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.2  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.1  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/

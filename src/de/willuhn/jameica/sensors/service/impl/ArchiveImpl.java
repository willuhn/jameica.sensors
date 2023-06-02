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
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.Archive;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des Archiv-Services.
 */
public class ArchiveImpl implements Archive, Configurable
{
  private final static String DRIVER = "com.mysql.jdbc.Driver";

  private EntityManager entityManager = null;
  private MessageConsumer consumer    = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName()
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
      Logger.warn("service already started, skipping request");
      return;
    }

    Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    if (!settings.getBoolean("hibernate.enabled",false))
    {
      Logger.info("archive service disabled");
      return;
    }

    this.consumer = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
    Logger.info("archive service started");
  }
  
  /**
   * Liefert den EntityManager.
   * @return der EntityManager.
   */
  private EntityManager getEntityManager()
  {
    if (this.entityManager == null)
    {
      Logger.info("init entity manager");
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
      Map params = new HashMap();
      
      params.put("hibernate.connection.driver_class",settings.getString("hibernate.connection.driver_class",DRIVER));
      params.put("hibernate.connection.url",settings.getString("hibernate.connection.url","jdbc:mysql://localhost:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1&serverTimezone=Europe/Paris"));
      params.put("hibernate.connection.username",settings.getString("hibernate.connection.username","jameica_sensors"));
      params.put("hibernate.connection.password",settings.getString("hibernate.connection.password","jameica_sensors"));
      params.put("hibernate.dialect",settings.getString("hibernate.dialect","org.hibernate.dialect.MySQLDialect"));
      params.put("hibernate.show_sql",settings.getString("hibernate.show_sql","false"));
      params.put("hibernate.hbm2ddl.auto",settings.getString("hibernate.hbm2ddl.auto","update")); // create,update,validate

      EntityManagerFactory ef = Persistence.createEntityManagerFactory("jameica_sensors",params);
      this.entityManager = ef.createEntityManager();
    }
    return this.entityManager;
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
    
    Application.getMessagingFactory().unRegisterMessageConsumer(this.consumer);
    
    try
    {
      if (this.entityManager != null)
        this.entityManager.close();
    }
    finally
    {
      this.entityManager = null;
      this.consumer = null;
    }
  }
  
  /**
   * Archiviert die Messergebnisse fuer ein Device.
   * @param uuid UUID des Devices.
   * @param m die Messung.
   */
  private void archive(String uuid, Measurement m)
  {
    ClassLoader cl = Application.getPluginLoader().getPlugin(Plugin.class).getManifest().getClassLoader();
    
    EntityTransaction tx = null;
    EntityManager em     = this.getEntityManager();
    
    try
    {
      int count = 0;
      tx = em.getTransaction();
      tx.begin();
      
      
      //////////////////////////////////////////////////////////////////////////
      // Device
      Device d = (Device) findObject("Device",uuid);
      if (d == null)
      {
        Logger.info("adding new device [uuid: " + uuid + "] to archive");
        d = new Device();
        d.setUuid(uuid);
        
        em.persist(d);
      }
      //////////////////////////////////////////////////////////////////////////
      

      //////////////////////////////////////////////////////////////////////////
      // jetzt holen wir alle Sensoren und speichern die Messergebnisse
      // Vor jedem Sensor pruefen wir noch, ob wir ihn schon in der Datenbank haben
      List<Sensorgroup> groups = m.getSensorgroups();
      for (Sensorgroup group:groups)
      {
        // Die Sensor-Gruppe selbst muss nicht archiviert werden. Sie dient
        // nur der strukturierten Ausgabe auf einer GUI
        List<Sensor> sensors = group.getSensors();

        if (sensors == null || sensors.size() == 0)
        {
          Logger.warn("sensor group " + group.getName() + " [uuid: " + group.getUuid() + "] from device [uuid: " + uuid + "] contains no sensor values, skipping");
          continue;
        }
        
        for (Sensor sensor:sensors)
        {
          de.willuhn.jameica.sensors.beans.Sensor archiveSensor = (de.willuhn.jameica.sensors.beans.Sensor) findObject("Sensor",sensor.getUuid());
          if (archiveSensor == null)
          {
            Logger.info("adding new sensor [uuid: " + sensor.getUuid() + "] to archive");
            archiveSensor = new de.willuhn.jameica.sensors.beans.Sensor();
            archiveSensor.setUuid(sensor.getUuid());

            // Serializer nicht vergessen
            Class serializer = sensor.getSerializer();
            archiveSensor.setSerializer(serializer != null ? serializer.getName() : null);
            
            // Sensor zum Device hinzufuegen
            d.getSensors().add(archiveSensor);
            em.persist(archiveSensor);
          }
          
          // Messwerte speichern
          try
          {
            Serializer s = (Serializer) cl.loadClass(archiveSensor.getSerializer()).getDeclaredConstructor().newInstance();
            Value value = new Value();
            value.setDate(m.getDate()); // Datum aus Messung uebernehmen
            value.setValue(s.serialize(sensor.getValue()));
            archiveSensor.getValues().add(value);

            em.persist(value);
            count++;
          }
          catch (Exception e)
          {
            // Wegen einem einzelnen Messwert brechen wir nicht ab.
            Logger.error("unable to serialize sensor value",e);
          }
        }
      }

      
      // OK, alles speichern
      tx.commit();
      Logger.info("added " + count + " values to archive");
    }
    catch (PersistenceException pe)
    {
      if (tx != null && tx.isActive())
        tx.rollback();
      throw pe;
    }
  }
  
  /**
   * Sucht das angegebene Objekt im Archiv.
   * @param table Tabellenname.
   * @param uuid die UUID des Objekts.
   * @return der Sensor aus dem Archiv oder NULL, wenn er da noch nicht existiert.
   */
  private Object findObject(String table, String uuid)
  {
    try
    {
      Query q = getEntityManager().createQuery("from " + table + " where uuid = ?1");
      q.setParameter(1,uuid);
      return q.getSingleResult();
    }
    catch (NoResultException e)
    {
      // ignore
    }
    return null;
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

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
    Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    

    List<Parameter> params = new ArrayList<Parameter>();
    params.add(new Parameter(i18n.tr("Archiv-Service aktiviert"),i18n.tr("Aktiviert/Deaktiviert das Schreiben der Messwerte in die Datenbank. Mögliche Werte: true/false"),settings.getString("hibernate.enabled","false"),"hibernate.enabled"));
    params.add(new Parameter(i18n.tr("JDBC-Treiber"),i18n.tr("Für MySQL z.Bsp. {0}",DRIVER),settings.getString("hibernate.connection.driver_class",DRIVER),"hibernate.connection.driver_class"));
    params.add(new Parameter(i18n.tr("JDBC-URL"),i18n.tr("Für MySQL z.Bsp. jdbc:mysql://localhost:3306/jameica_sensors"),settings.getString("hibernate.connection.url","jdbc:mysql://localhost:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1&serverTimezone=Europe/Paris"),"hibernate.connection.url"));
    params.add(new Parameter(i18n.tr("JDBC-Username"),i18n.tr("Name des Datenbank-Benutzers"),settings.getString("hibernate.connection.username","jameica_sensors"),"hibernate.connection.username"));
    params.add(new Parameter(i18n.tr("JDBC-Passwort"),i18n.tr("Passwort des Datenbank-Benutzers"),settings.getString("hibernate.connection.password","jameica_sensors"),"hibernate.connection.password"));
    params.add(new Parameter(i18n.tr("Hibernate-Dialekt"),i18n.tr("Für MySQL z.Bsp. org.hibernate.dialect.MySQLDialect"),settings.getString("hibernate.dialect","org.hibernate.dialect.MySQLDialect"),"hibernate.dialect"));
    return params;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#setParameters(java.util.List)
   */
  public void setParameters(List<Parameter> parameters)
  {
    Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

    int count = 0;
    for (Parameter p:parameters)
    {
      String id = p.getUuid();
      
      String oldValue = settings.getString(id,null);
      String newValue = p.getValue();
      
      String s1 = oldValue == null ? "" : oldValue;
      String s2 = newValue == null ? "" : newValue;
      if (!s1.equals(s2))
      {
        Logger.info("parameter \"" + p.getName() + "\" [" + id + "] changed. old value: " + oldValue + ", new value: " + newValue);
        settings.setAttribute(id,newValue);
        count++;
      }
    }
    
    if (count > 0)
    {
      Logger.info("restarting archive service");
      try
      {
        if (this.isStarted())
          this.stop(true);
        if (!this.isStarted())
          this.start();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to restart archive service",re);
      }
    }
  }
}

/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/ArchiveImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/08/21 13:34:17 $
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
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.service.Archive;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Archiv-Services.
 */
public class ArchiveImpl implements Archive
{
  private EntityManager em = null;

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
    return this.em != null;
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

    try
    {
      Map params = new HashMap();
      params.put("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
      params.put("hibernate.connection.url","jdbc:mysql://server:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1");
      params.put("hibernate.connection.username","jameica_sensors");
      params.put("hibernate.connection.password","jameica_sensors");
      params.put("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
      params.put("hibernate.show_sql","false");
      params.put("hibernate.hbm2ddl.auto","update"); // create,update,validate

      EntityManagerFactory ef = Persistence.createEntityManagerFactory("jameica_sensors",params);
      this.em = ef.createEntityManager();
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to create entity manager",e);
    }
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
      if (this.em != null)
        this.em.close();
    }
    finally
    {
      this.em = null;
    }
  }
  
  /**
   * @see de.willuhn.jameica.sensors.service.Archive#archive(de.willuhn.jameica.sensors.devices.Device, de.willuhn.jameica.sensors.devices.Measurement)
   */
  public void archive(de.willuhn.jameica.sensors.devices.Device device, Measurement m)
  {
    ClassLoader cl = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getClassLoader();
    
    EntityTransaction tx = null;
    try
    {
      int count = 0;
      tx = this.em.getTransaction();
      tx.begin();
      
      
      //////////////////////////////////////////////////////////////////////////
      // Device
      Device d = (Device) findObject("Device",device.getUuid());
      if (d == null)
      {
        Logger.info("adding new device [uuid: " + device.getUuid() + "] to archive");
        d = new Device();
        d.setUuid(device.getUuid());
        
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
            Serializer s = (Serializer) cl.loadClass(archiveSensor.getSerializer()).newInstance();
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
      Query q = this.em.createQuery("from " + table + " where uuid = ?");
      q.setParameter(1,uuid);
      return q.getSingleResult();
    }
    catch (NoResultException e)
    {
      // ignore
    }
    return null;
  }
}


/**********************************************************************
 * $Log: ArchiveImpl.java,v $
 * Revision 1.7  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.6  2009/08/21 00:43:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2009/08/20 23:26:25  willuhn
 * *** empty log message ***
 *
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

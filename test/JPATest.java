/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/test/JPATest.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/20 22:08:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.beans.Valuegroup;
import de.willuhn.jameica.sensors.beans.Value.Type;
import de.willuhn.jameica.sensors.util.UUIDUtil;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Testet das Schreiben von Messwerten in die Datenbank.
 */
public class JPATest
{
  private static EntityManager em = null;
  
  @BeforeClass
  public static void setUp() throws Exception
  {
    Logger.setLevel(Level.INFO);

    Map params = new HashMap();

    params.put("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
    params.put("hibernate.connection.url","jdbc:mysql://server:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1");
    params.put("hibernate.connection.username","jameica_sensors");
    params.put("hibernate.connection.password","jameica_sensors");
    params.put("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
    params.put("hibernate.show_sql","true");
    params.put("hibernate.hbm2ddl.auto","create"); // ,update,validate");
    EntityManagerFactory ef = Persistence.createEntityManagerFactory("jameica_sensors",params);
    em = ef.createEntityManager();
  }

  @AfterClass
  public static void tearDown() throws Exception
  {
    if (em != null)
      em.close();
  }
  
  
  /**
   * @throws Exception
   */
  @Test
  public void test001() throws Exception
  {
    Device d = new Device();
    d.setUuid(UUIDUtil.create("unit.test.001"));
    
    Measurement m = new Measurement();
    m.setDate(new Date());
    
    
    Valuegroup g = new Valuegroup();
    g.setUuid(UUIDUtil.create("unit.test.001.group1"));
    
    Value<String> v = new Value<String>();
    v.setValue(new Date().toString());
    v.setType(Type.STRING);

    g.getValues().add(v);
    m.getValuegroups().add(g);
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
      pe.printStackTrace();
      if (tx != null && tx.isActive())
        tx.rollback();
      throw pe;
    }
    
  }

  /**
   * @throws Exception
   */
  @Test
  public void test002() throws Exception
  {
    Query q = em.createQuery("from Device where uuid = ?");
    q.setParameter(1,UUIDUtil.create("unit.test.001"));
    Device d = (Device) q.getSingleResult();
    Assert.assertEquals(d.getMeasurements().size(),1);
  }

  /**
   * @throws Exception
   */
  @Test
  public void test003() throws Exception
  {
    Query q = em.createQuery("from Device where uuid = ?");
    q.setParameter(1,UUIDUtil.create("unit.test.001"));
    Device d = (Device) q.getSingleResult();

    Measurement m = new Measurement();
    m.setDate(new Date());
    
    q = em.createQuery("from Valuegroup where uuid = ?");
    q.setParameter(1,UUIDUtil.create("unit.test.001.group1"));
    Valuegroup g = (Valuegroup) q.getSingleResult();

    Value<String> v = new Value<String>();
    v.setValue(new Date().toString());
    v.setType(Type.STRING);

    g.getValues().add(v);
    m.getValuegroups().add(g);
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
      pe.printStackTrace();
      if (tx != null && tx.isActive())
        tx.rollback();
      throw pe;
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void test004() throws Exception
  {
    Query q = em.createQuery("from Valuegroup where uuid = ?");
    q.setParameter(1,UUIDUtil.create("unit.test.001.group1"));
    q.getSingleResult(); // Wirft eine Exception, wenn die Gruppe doppelt existiert
  }

  /**
   * @throws Exception
   */
  @Test
  public void test005() throws Exception
  {
    Query q = em.createQuery("from Device where uuid = ?");
    q.setParameter(1,UUIDUtil.create("unit.test.001"));
    Device d = (Device) q.getSingleResult();
    Assert.assertEquals(d.getMeasurements().size(),2);
  }

}


/**********************************************************************
 * $Log: JPATest.java,v $
 * Revision 1.2  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.1  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 **********************************************************************/

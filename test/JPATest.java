/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/test/JPATest.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/20 18:07:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Measurement;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.beans.Valuegroup;
import de.willuhn.jameica.sensors.beans.Value.Type;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Testet das Schreiben von Messwerten in die Datenbank.
 */
public class JPATest
{
  private EntityManager em = null;
  
  @Before
  public void setUp() throws Exception
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
    this.em = ef.createEntityManager();
  }

  @After
  public void tearDown() throws Exception
  {
    if (this.em != null)
      this.em.close();
  }
  
  
  /**
   * @throws Exception
   */
  @Test
  public void test001() throws Exception
  {
    Value<Date> v = new Value<Date>();
    v.setName("test");
    v.setValue(new Date());
    v.setType(Type.DATE);
    
    List<Value> lv = new ArrayList<Value>();
    lv.add(v);
    
    Valuegroup g = new Valuegroup();
    g.setName("date");
    g.setValues(lv);
    
    v.setValuegroup(g); // reverse

    List<Valuegroup> lg = new ArrayList<Valuegroup>();
    lg.add(g);
    Measurement m = new Measurement();
    m.setValueGroups(lg);
    
    g.setMeasurement(m); // reverse

    Device d = new Device();
    d.setId("test");
    
    m.setDevice(d);

    EntityTransaction tx = null;
    try
    {
      tx = em.getTransaction();
      tx.begin();
      em.persist(d);

      em.persist(m);
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

}


/**********************************************************************
 * $Log: JPATest.java,v $
 * Revision 1.1  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 **********************************************************************/

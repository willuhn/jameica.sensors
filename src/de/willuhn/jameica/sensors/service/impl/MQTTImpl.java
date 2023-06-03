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

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.MQTT;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des MQTT-Services.
 */
public class MQTTImpl implements MQTT, Configurable
{
  private final static String PARAM_ENABLED  = "mqtt.enabled";
  private final static String PARAM_URL      = "mqtt.url";
  private final static String PARAM_CLIENTID = "mqtt.clientid";
  private final static String PARAM_USERNAME = "mqtt.username";
  private final static String PARAM_PASSWORD = "mqtt.password";
  
  private final static String URL_DEFAULT      = "tcp://localhost:1883";
  private final static String CLIENTID_DEFAULT = "jameica.sensors";

  
  private IMqttClient client = null;
  private MessageConsumer consumer = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName()
  {
    return "mqtt service";
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

    final Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    if (!settings.getBoolean(PARAM_ENABLED,false))
    {
      Logger.info("mqtt service disabled");
      return;
    }

    this.consumer = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
    Logger.info("mqtt service started");
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
      if (this.client != null)
        this.client.close();
    }
    catch (Exception e)
    {
      Logger.error("unable to close mqtt client",e);
    }
    finally
    {
      this.client = null;
      this.consumer = null;
    }
  }
  
  /**
   * Liefert den MQTT-Client.
   * @return der MQTT-Client.
   * @throws MqttException 
   */
  private IMqttClient getClient() throws MqttException
  {
    if (this.client != null)
      return this.client;
    
    final Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    final String url      = settings.getString(PARAM_URL,URL_DEFAULT);
    final String id       = settings.getString(PARAM_CLIENTID,CLIENTID_DEFAULT);
    final String username = settings.getString(PARAM_USERNAME,null);
    final char[] password = settings.getString(PARAM_PASSWORD,"").toCharArray();

    Logger.info("init mqtt client [url: " + url + ", client-id: " + id + "]");
    this.client = new MqttClient(url,id,null);

    final MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    
    if (username != null && username.length() > 0 && password != null && password.length > 0)
    {
      Logger.info("setting mqtt username + password [username: " + username + "]");
      options.setUserName(username);
      options.setPassword(password);
    }
    
    this.client.connect(options);
    return this.client;
  }
  
  /**
   * Publiziert die Messergebnisse fuer ein Device.
   * @param uuid UUID des Devices.
   * @param m die Messung.
   */
  private void publish(String uuid, Measurement m)
  {
    int count = 0;
    for (Sensorgroup group:m.getSensorgroups())
    {
      final List<Sensor> sensors = group.getSensors();

      if (sensors == null || sensors.size() == 0)
      {
        Logger.warn("sensor group " + group.getName() + " [uuid: " + group.getUuid() + "] from device [uuid: " + uuid + "] contains no sensor values, skipping");
        continue;
      }
      
      for (Sensor sensor:sensors)
      {
        // Messwerte senden
        try
        {
          final Object value = sensor.getValue();
          
          // Sensor hat keinen Wert
          if (value == null)
            continue;
          
          Class<? extends Serializer> c = sensor.getSerializer();
          if (c == null)
            c = StringSerializer.class;
          
          final Serializer s = c.getDeclaredConstructor().newInstance();
          final String payload = s.format(value);
          if (payload == null || payload.length() == 0)
            continue;
          
          final String topic = sensor.getUuid().replace('.','/');
          final byte[] data = payload.getBytes(StandardCharsets.UTF_8);

          final MqttMessage msg = new MqttMessage();
          msg.setQos(0);
          msg.setRetained(true);
          msg.setPayload(data);
          this.getClient().publish(topic,msg);
          Logger.info("published " + data.length + " bytes to topic \"" + topic + "\"");
          count++;
        }
        catch (Exception e)
        {
          // Wegen einem einzelnen Messwert brechen wir nicht ab.
          Logger.error("unable to publish sensor value",e);
        }
      }
    }

    Logger.info("published " + count + " values to mqtt");
  }
  
  /**
   * Mit dem Message-Consumer abonnieren wir die aktuellen Messwerte fuer den Versand.
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
      publish(msg.getDevice().getUuid(),msg.getMeasurement());
    }
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    final I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
    final Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    

    final List<Parameter> params = new ArrayList<Parameter>();
    params.add(new Parameter(i18n.tr("MQTT-Service aktiviert"),i18n.tr("Aktiviert/Deaktiviert das Senden der Messwerte an einen MQTT-Broker/-Server. Mögliche Werte: true/false"),settings.getString(PARAM_ENABLED,"false"),PARAM_ENABLED));
    params.add(new Parameter(i18n.tr("URL"),i18n.tr("Die URL des MQTT-Brokers/-Servers, z.B. {0}",URL_DEFAULT),settings.getString(PARAM_URL,URL_DEFAULT),PARAM_URL));
    params.add(new Parameter(i18n.tr("Client-ID"),i18n.tr("Zu verwendende Client-ID gegenüber dem MQTT-Broker/-Server"),settings.getString(PARAM_CLIENTID,CLIENTID_DEFAULT),PARAM_CLIENTID));
    params.add(new Parameter(i18n.tr("Benutzername"),i18n.tr("Benutzername für den MQTT-Broker/-Server"),settings.getString(PARAM_USERNAME,""),PARAM_USERNAME));
    params.add(new Parameter(i18n.tr("Passwort"),i18n.tr("Passwort für den MQTT-Broker/-Server"),settings.getString(PARAM_PASSWORD,""),PARAM_PASSWORD));
    return params;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#setParameters(java.util.List)
   */
  public void setParameters(List<Parameter> parameters)
  {
    final Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

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
      Logger.info("restarting mqtt service");
      try
      {
        if (this.isStarted())
          this.stop(true);
        if (!this.isStarted())
          this.start();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to restart mqtt service",re);
      }
    }
  }
}

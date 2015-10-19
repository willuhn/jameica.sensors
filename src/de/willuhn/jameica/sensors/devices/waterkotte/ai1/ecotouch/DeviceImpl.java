/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1.ecotouch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.waterkotte.ai1.AbstractDevice;
import de.willuhn.jameica.sensors.devices.waterkotte.ai1.TempSerializer;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Implementierung der Waterkotte Ai1 EcoTouch.
 * Der Code stammt von https://github.com/openhab/openhab/tree/master/bundles/binding/org.openhab.binding.ecotouch
 */
public class DeviceImpl extends AbstractDevice
{
  private final static Settings settings = new Settings(DeviceImpl.class);
  private static boolean msgPrinted = false;
  
  private final static String PARAM_HOSTNAME = "hostname";
  private final static String PARAM_USERNAME = "username";
  private final static String PARAM_PASSWORD = "password";
  private List<String> cookies = null;
  
  private final static Pattern PATTERN = Pattern.compile("#(.+)\\s+S_OK[^0-9-]+([0-9-]+)\\s+([0-9-]+)");
  
  /**
   * Versucht, das Login durchzufuehren.
   * @throws IOException
   */
  private void login() throws IOException
  {
    String host = settings.getString(PARAM_HOSTNAME,null);
    if (host == null || host.length() == 0)
    {
      // Wir zeigen den Hinweistext nur beim ersten Mal an.
      if (!msgPrinted)
        Logger.warn("device " + this.getName() + "[uuid: " + this.getUuid() + "] not configured - no hostname defined");
      msgPrinted = true;
      return;
    }

    final String login = "http://" + host + "/cgi/login?username=" + URLEncoder.encode(settings.getString(PARAM_USERNAME,""), "UTF-8") + "&password=" + URLEncoder.encode(settings.getString(PARAM_PASSWORD,""), "UTF-8");
    Logger.debug("login using url: " + login);
    HttpURLConnection conn = (HttpURLConnection) new URL(login).openConnection();
    final int code = conn.getResponseCode();
    final String msg = conn.getResponseMessage();
    
    if (code != HttpURLConnection.HTTP_OK)
      throw new IOException("login failed for url " + login + ": " + code + " - " + msg);

    Logger.info("login successful to: " + host);
    this.cookies = conn.getHeaderFields().get("Set-Cookie");
    Logger.info("received cookies: " + (cookies != null && cookies.size() > 0 ? cookies.size() : "<none>"));
  }
  
  /**
   * @see de.willuhn.jameica.sensors.devices.Device#collect()
   */
  public Measurement collect() throws IOException
  {
    login();
    
    try
    {
      Measurement m = new Measurement();

      //////////////////////////////////////////////////////////////////////////
      // Aussentemperatur
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.outdoor");
        g.setName(i18n.tr("Außentemperaturen"));
        
        Sensor<Float> current = fetch(Tag.TEMP_OUTDOOR);
        g.getSensors().add(current);
        g.getSensors().add(this.createExtreme(current,Extreme.MAX));
        g.getSensors().add(this.createExtreme(current,Extreme.MIN));
        
        g.getSensors().add(fetch(Tag.TEMP_OUTDOOR_1H));
        g.getSensors().add(fetch(Tag.TEMP_OUTDOOR_24H));
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Heizung
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.heater");
        g.setName(i18n.tr("Heizungstemperaturen"));
        g.getSensors().add(fetch(Tag.TEMP_HEATER_RETURN_TARGET));

        {
          Sensor<Float> current = fetch(Tag.TEMP_HEATER_RETURN_REAL);
          g.getSensors().add(current);
          g.getSensors().add(this.createExtreme(current,Extreme.MAX));
          g.getSensors().add(this.createExtreme(current,Extreme.MIN));
        }
        
        {
          Sensor<Float> current = fetch(Tag.TEMP_HEATER_OUT_REAL);
          g.getSensors().add(current);
          g.getSensors().add(this.createExtreme(current,Extreme.MAX));
          g.getSensors().add(this.createExtreme(current,Extreme.MIN));
        }
        
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////
      // Warmwasser
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.water");
        g.setName(i18n.tr("Warmwassertemperaturen"));
        g.getSensors().add(fetch(Tag.TEMP_WATER_TARGET));
        
        Sensor<Float> current = fetch(Tag.TEMP_WATER_REAL);
        g.getSensors().add(current);
        g.getSensors().add(this.createExtreme(current,Extreme.MAX));
        g.getSensors().add(this.createExtreme(current,Extreme.MIN));
        
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Waerme-Quelle (Sonde in der Tiefenbohrung)
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".temp.system");
        g.setName(i18n.tr("System-Temperaturen"));

        Sensor<Float> current = fetch(Tag.TEMP_SYSTEM_SOURCE_IN);
        g.getSensors().add(current);
        g.getSensors().add(this.createExtreme(current,Extreme.MAX));
        g.getSensors().add(this.createExtreme(current,Extreme.MIN));
        
        g.getSensors().add(fetch(Tag.TEMP_SYSTEM_SOURCE_OUT));
        g.getSensors().add(fetch(Tag.TEMP_SYSTEM_EVAPORATOR));
        g.getSensors().add(fetch(Tag.TEMP_SYSTEM_CONDENSER));
        g.getSensors().add(fetch(Tag.TEMP_SYSTEM_SUCTION));
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////
      // Kennzahlen
      {
        Sensorgroup g = new Sensorgroup();
        g.setUuid(this.getUuid() + ".kpi");
        g.setName(i18n.tr("Kennzahlen"));

        {
          Sensor<Float> current = fetch(Tag.COP_HEATING);
          g.getSensors().add(current);
          g.getSensors().add(this.createExtreme(current,Extreme.MAX));
          g.getSensors().add(this.createExtreme(current,Extreme.MIN));
        }
        
        {
          Sensor<Float> current = fetch(Tag.POWER_COMPRESSOR);
          g.getSensors().add(current);
          g.getSensors().add(this.createExtreme(current,Extreme.MAX));
          g.getSensors().add(this.createExtreme(current,Extreme.MIN));
        }
        
        m.getSensorgroups().add(g);
      }
      //////////////////////////////////////////////////////////////////////////

      return m;
    }
    catch (IOException ioe)
    {
      throw ioe;
    }
    catch (Exception e)
    {
      Logger.error("error while fetching data from device",e); // Kann man mit der IOException leider nicht weiterwerfen
      throw new IOException("error while fetching data from device: " + e.getMessage());
    }
  }
  
  /**
   * Ruft einen einzelnen Wert ab.
   * @param tag das Tag.
   * @return der Sensor-Wert.
   * @throws Exception
   */
  private Sensor fetch(Tag tag) throws Exception
  {
    final String url = "http://" + settings.getString(PARAM_HOSTNAME,null) + "/cgi/readTags?n=1&t1=" + tag.getTag();
    
    String body = null;
    
    for (int i=0;i<3;++i) // Wir versuchen es maximal 3 mal
    {
      BufferedReader reader = null;
      
      try
      {
        Logger.debug("trying to fetch " + url);
        URLConnection conn = new URL(url).openConnection();
        if (cookies != null)
        {
          for (String cookie : cookies)
          {
            conn.addRequestProperty("Cookie",cookie.split(";", 2)[0]);
          }
        }
        
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        final StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
          sb.append(line + "\n");
        }
        body = sb.toString();

        // Wir haben einen Wert
        if (body.contains("#" + tag.getTag()))
          break;
        
        // Einloggen und erneut versuchen
        login();
      }
      catch (Exception e)
      {
        Logger.write(Level.DEBUG,"error while fetching tag " + tag.getId(),e);
        login();
      }
      finally
      {
        IOUtil.close(reader);
      }
    }

    if (body == null || !body.contains("#" + tag.getTag()))
      throw new IOException("got no value for tag " + tag.getId());

    Matcher m = PATTERN.matcher(body);
    boolean b = m.find();
    if (!b)
      throw new IOException("got invalid value for tag " + tag.getId() + ", value: " + body);

    final int raw = Integer.parseInt(m.group(3));
    final BigDecimal value = new BigDecimal(raw).divide(BigDecimal.TEN);

    Sensor<Float> s = new Sensor<Float>();
    s.setName(tag.getDescription());
    s.setUuid(this.getUuid() + "." + tag.getId()); // wir haengen noch die Device-UUID davor, damit es global eindeutig ist
    s.setSerializer(TempSerializer.class);
    s.setValue(value.floatValue());
    return s;

  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#getName()
   */
  public String getName()
  {
    return "Waterkotte Ai1 (EcoTouch)";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    // Konfigurierbar, damit die EcoTouch nach aussen auch eine WPCU "emulieren" kann.
    return settings.getString("uuid","waterkotte.ai1.ecotouch.device");
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#isEnabled()
   */
  public boolean isEnabled()
  {
    // Wir checken einfach, ob die Heizung konfiguriert ist
    return settings.getString(PARAM_HOSTNAME,null) != null;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    List<Parameter> params = new ArrayList<Parameter>();
    
    params.add(new Parameter(i18n.tr("Hostname"),i18n.tr("Hostname oder IP-Adresse der Anlage"),settings.getString(PARAM_HOSTNAME,null),this.getUuid() + "." + PARAM_HOSTNAME));
    params.add(new Parameter(i18n.tr("Username"),i18n.tr("Benutzername für die Authentifizierung"),settings.getString(PARAM_USERNAME,null),this.getUuid() + "." + PARAM_USERNAME));
    params.add(new Parameter(i18n.tr("Passwort"),i18n.tr("Passwort für die Authentifizierung"),settings.getString(PARAM_PASSWORD,null),this.getUuid() + "." + PARAM_PASSWORD));
    return params;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#setParameters(java.util.List)
   */
  public void setParameters(List<Parameter> parameters)
  {
    for (Parameter p:parameters)
    {
      String id = p.getUuid();
      
      // Wir schneiden unsere Device-UUID wieder ab
      id = id.substring(this.getUuid().length()+1); // das "+1" ist fuer den "." als Trennzeichen

      String oldValue = settings.getString(id,null);
      String newValue = p.getValue();
      
      String s1 = oldValue == null ? "" : oldValue;
      String s2 = newValue == null ? "" : newValue;
      if (!s1.equals(s2))
      {
        Logger.info("parameter \"" + p.getName() + "\" [" + id + "] changed. old value: " + oldValue + ", new value: " + newValue);
        settings.setAttribute(id,newValue);
      }
    }
  }

}

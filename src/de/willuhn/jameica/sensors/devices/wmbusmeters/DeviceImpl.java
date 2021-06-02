/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.wmbusmeters;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.DecimalSerializer;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensor.Type;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Device, welches die Werte von wmbusmeters liest.
 * Siehe https://github.com/weetmuts/wmbusmeters
 */
public class DeviceImpl implements Device, Configurable
{
  private final static I18N i18n         = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static Settings settings = new Settings(DeviceImpl.class);

  private List<WmbusData> day = new LinkedList<WmbusData>();
  private final ObjectMapper om = new ObjectMapper();
  private WmbusData last = null;
  private Date started = null;

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#collect()
   */
  public Measurement collect() throws IOException
  {
    final String f = settings.getString("file",null);
    if (f == null)
      throw new IOException("no file configured");
    
    final File file = new File(f);
    if (!file.isFile() || !file.canRead())
      throw new IOException("cannot read file " + file);
    
    final WmbusData data = this.om.readerFor(WmbusData.class).readValue(file);
    final Measurement m = new Measurement();
    m.setDate(data.timestamp);

    {
      Sensorgroup group = new Sensorgroup();
      group.setUuid(this.getUuid() + "." + data.name); // Wir verwenden hier nicht die ID. Sonst koennte man beim Zaehlerwechsel nicht die Werte fortschreiben
      group.setName(data.name);
      m.getSensorgroups().add(group);
      
      {
        Sensor<BigDecimal> s = new Sensor();
        s.setUuid(group.getUuid() + ".total");
        s.setName("total (m³)");
        s.setType(Type.GAUGE);
        s.setSerializer(DecimalSerializer.class);
        s.setValue(data.total_m3);
        group.getSensors().add(s);
      }

      {
        Sensor<BigDecimal> s = new Sensor();
        s.setUuid(group.getUuid() + ".usage");
        s.setName("usage (m³)");
        s.setType(Type.COUNTER);
        s.setSerializer(DecimalSerializer.class);
        s.setValue(data.total_m3);
        group.getSensors().add(s);
      }

      {
        Sensor<BigDecimal> s = new Sensor();
        s.setUuid(group.getUuid() + ".maxfloat");
        s.setName("max float (m³/h)");
        s.setType(Type.GAUGE);
        s.setSerializer(DecimalSerializer.class);
        s.setValue(data.max_flow_m3h);
        group.getSensors().add(s);
      }

      // Sensor fuer den Tageszaehler
      {
        this.day.add(data);
        
        // Wir haben mindestens 2 Messungen. Dann werfen wir alles raus,
        // was aelter als 24h ist. Die juengste davon nehmen wir als 24h-Vergleichswert
        // Damit erzeugen wir einen fliessenden 24h-Verbrauch
        if (this.day.size() > 1)
        {
          final List<WmbusData> toRemove = new LinkedList<WmbusData>();
          for (WmbusData o:this.day)
          {
            long millis = data.timestamp.getTime() - o.timestamp.getTime();
            long hours = millis / 1000 / 60 / 60;
            if (hours >= 24)
              toRemove.add(o);
          }
          
          // Wenn wir noch keine Werte haben, die 24h her sind, nehmen wir einfach den letzten
          // Dann koennen wir auch schon vorher Werte liefern, die im Tagesverlauf dann ansteigen
          final WmbusData d = toRemove.size() > 0 ? toRemove.get(toRemove.size() - 1) : this.day.get(0);
          final BigDecimal usage = data.total_m3.subtract(d.total_m3);
          Sensor<BigDecimal> s = new Sensor();
          s.setUuid(group.getUuid() + ".daily");
          s.setName("daily (m³)");
          s.setType(Type.GAUGE);
          s.setSerializer(DecimalSerializer.class);
          s.setValue(usage);
          group.getSensors().add(s);

          // Alle Werte wegwerfen, die aeltr als 24h sind
          if (toRemove.size() > 0)
            this.day.removeAll(toRemove);
        }
      }
      
      // Sensor fuer den Dauerfluss
      {
        long duration = 0L;
        boolean changed = (this.last != null && !Objects.equals(this.last.total_m3,data.total_m3));
        
        if (changed)
        {
          // Zaehlerstand hat sich seit dem letzten Mal geaendert. Wenn "started" noch
          // kein Datum hat, setzen wir es
          if (this.started == null)
          {
            this.started = new Date();
          }
          else
          {
            // Datum war schon gesetzt. Dann checken, wie lange esschon besteht
            final long diff = System.currentTimeMillis() - this.started.getTime();
            duration = (diff / 1000 / 60);
          }
          
          
        }
        else
        {
          // Wasserfluss war gestoppt
          this.started = null;
        }
        
        
        Sensor<Long> s = new Sensor();
        s.setUuid(group.getUuid() + ".floatduration");
        s.setName("float duration (minutes)");
        s.setType(Type.GAUGE);
        s.setSerializer(DecimalSerializer.class);
        s.setValue(Long.valueOf(duration));
        group.getSensors().add(s);
      }
    }
    

    this.last = data;
    return m;
  }
  
  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    return "jameica.sensors.wmb.device";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#getName()
   */
  public String getName()
  {
    return "wmbusmeters";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#isEnabled()
   */
  public boolean isEnabled()
  {
    return settings.getBoolean("enabled",false);
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    List<Parameter> params = new ArrayList<Parameter>();
    params.add(new Parameter(i18n.tr("wmbusmeters aktiviert"),i18n.tr("Liefert Messwerte von wmbusmeters. Mögliche Werte: true/false"),settings.getString("enabled","false"),this.getUuid() + ".enabled"));
    params.add(new Parameter(i18n.tr("JSON-Datei"),i18n.tr("Pfad zur Datei mit den JSON-Messwerten"),settings.getString("file","/var/log/wmbusmeters/meter_readings/wasser"),this.getUuid() + ".file"));
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

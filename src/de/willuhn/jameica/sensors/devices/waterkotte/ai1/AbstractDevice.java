/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.History;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung der Waterkotte Ai1.
 */
public abstract class AbstractDevice implements Device, Configurable
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  // Cache fuer die Hoechst- und Tiefs-Werte der letzten 24h.
  private final Map<String,History> extremes = new HashMap<String,History>();
  
  /**
   * Erzeut eine Kopie des Sensors - jedoch mit dem 24h-Extrem des Sensors.
   * @param sensor der Sensor.
   * @return die Kopie des Sensors - jedoch mit dem 24h-Extrem.
   */
  protected Sensor<Float> createExtreme(Sensor<Float> sensor, Extreme type)
  {
    String key = sensor.getUuid() + "." + type.key;
    History history = extremes.get(key);

    // Es gibt noch gar keine Queue fuer die Werte. Dann legen wir eine an
    if (history == null)
    {
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
      int minutes = settings.getInt("scheduler.interval.minutes",5);
      int size = 24 * 60 / minutes;
      history = new History(size);
      extremes.put(key,history);
    }

    Float value = sensor.getValue();

    // Wert hinzufuegen
    history.push(value);
    
    // Extrem-Wert ermitteln
    List<Float> values = history.elements();
    for (Float f:values)
    {
      if (type == Extreme.MAX && f.compareTo(value) > 0)
      {
        value = f;
        continue;
      }
      if (type == Extreme.MIN && f.compareTo(value) < 0)
      {
        value = f;
        continue;
      }
    }

    Sensor clone = (Sensor<Float>) sensor.clone();
    clone.setUuid(key);
    clone.setValue(value);
    clone.setName(i18n.tr("{0} ({1})",sensor.getName(),type.title));
    return clone;
  }

  /**
   * Der Typ des Extems.
   */
  protected static enum Extreme
  {
    /**
     * Definiert ein Maximum.
     */
    MAX("max",i18n.tr("24h Maximum")),
    
    /**
     * Definiert ein Minimum.
     */
    MIN("min",i18n.tr("24h Minimum"));
    
    private String key   = null;
    private String title = null;
    
    /**
     * ct.
     * @param key
     * @param title
     */
    private Extreme(String key, String title)
    {
      this.key   = key;
      this.title = title;
    }
  }
}

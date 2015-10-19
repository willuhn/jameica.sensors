/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.waterkotte.ai1.ecotouch;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Die verfuegbaren Tags.
 * Siehe https://raw.githubusercontent.com/openhab/openhab/master/bundles/binding/org.openhab.binding.ecotouch/src/main/java/org/openhab/binding/ecotouch/EcoTouchTags.java
 */
public enum Tag
{
  /**
   * Aussentemperatur aktuell.
   */
  TEMP_OUTDOOR("A1","temp.outdoor.current","Aktuell"),
  
  /**
   * Aussentemperatur 1h.
   */
  TEMP_OUTDOOR_1H("A2","temp.outdoor.1h","Mittelwert 1h"),
  
  /**
   * Aussentemperatur 2h.
   */
  TEMP_OUTDOOR_24H("A3","temp.outdoor.24h","Mittelwert 24h"),
  
  /**
   * Waermequelle Eingang.
   */
  TEMP_SYSTEM_SOURCE_IN("A4","temp.system.source.in","Wärmequelle Eingang"),

  /**
   * Waermequelle Ausgang.
   */
  TEMP_SYSTEM_SOURCE_OUT("A5","temp.system.source.out","Wärmequelle Ausgang"),

  /**
   * Verdampfer
   */
  TEMP_SYSTEM_EVAPORATOR("A6","temp.system.evaporator","Verdampfer"),

  /**
   * Saugleitung
   */
  TEMP_SYSTEM_SUCTION("A7","temp.system.suction","Saugleitung"),

  /**
   * Ruecklauf Soll.
   */
  TEMP_HEATER_RETURN_TARGET("A10","temp.heater.return.target","Rücklauf Soll"),

  /**
   * Ruecklauf Ist.
   */
  TEMP_HEATER_RETURN_REAL("A11","temp.heater.return.real","Rücklauf Ist"),

  /**
   * Vorlauf Ist.
   */
  TEMP_HEATER_OUT_REAL("A12","temp.heater.out.real","Vorlauf Ist"),

  /**
   * Kondensator.
   */
  TEMP_SYSTEM_CONDENSER("A14","temp.system.condenser","Kondensator"),

  /**
   * Warmwasser Ist.
   */
  TEMP_WATER_REAL("A19","temp.water.real","Ist"),

  /**
   * Warmwasser Soll.
   */
  TEMP_WATER_TARGET("A37","temp.water.target","Soll"),

  /**
   * Leistung Kompressor.
   */
  POWER_COMPRESSOR("A25","power.compressor","Leistung Kompressor"),

  /**
   * COP Heizleistung.
   */
  COP_HEATING("A28","cop.heating","COP Heizleistung"),

  ;
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  private String tag = null;
  private String id = null;
  private String description = null;
  
  /**
   * ct.
   * @param tag der Name des Tag.
   * @param id die interne ID des Sensors.
   * @param description der sprechende Name des Sensors.
   */
  private Tag(String tag, String id, String description)
  {
    this.tag = tag;
    this.id = id;
    this.description = description;
  }
  
  /**
   * Liefert die interne ID des Sensors.
   * @return id die interne ID des Sensors.
   */
  public String getId()
  {
    return id;
  }
  
  /**
   * Liefert das Tag des Sensors.
   * @return tag das Tag des Sensors.
   */
  public String getTag()
  {
    return tag;
  }
  
  /**
   * Liefert den Beschreibungstext des Sensors.
   * @return description Beschreibungstext.
   */
  public String getDescription()
  {
    return i18n.tr(description);
  }
}



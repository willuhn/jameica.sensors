/**********************************************************************
 *
 * Copyright (c) 2021 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.wmbusmeters;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

/**
 * Jackson-Mapping fuer die JSON-Daten von wmbusmeters
 */
public class WmbusData
{
  /**
   * Medienbezeichnung.
   * Z.B. "cold water"
   */
  public String media;

  /**
   * Modellbezeichnung.
   */
  public String meter;
  
  /**
   * Die konfigurierte Bezeichnung des Zaehlers.
   */
  public String name;
  
  /**
   * Die Seriennummer des Zaehlers.
   */
  public String id;
  
  /**
   * Der Gesamt-Zaehlerstand in m³
   */
  public BigDecimal total_m3;

  /**
   * Unbekannt.
   */
  public BigDecimal target_m3;

  /**
   * Maximaler Durchfluss pro Stunde in m³.
   */
  public BigDecimal max_flow_m3h;

  /**
   * Fluss-Temperatur.
   * 127, wenn kein Wert uebertragen wurde.
   */
  public Integer flow_temperature_c;
  
  /**
   * Externe Temperatur.
   * 127, wenn kein Wert uebertragen wurde.
   */
  public Integer external_temperature_c;
  
  /**
   * Unbekannt.
   */
  public String current_status;
  
  /**
   * Unbekannt.
   */
  public String time_dry;

  /**
   * Unbekannt.
   */
  public String time_reversed;
  
  /**
   * Leckage-Zeit. Format unbekannt.
   */
  public String time_leaking;
  
  /**
   * Unbekannt.
   */
  public String time_bursting;
  
  /**
   * Empfangspegel.
   */
  public Integer rssi_dbm;

  /**
   * Zeitstempelt der Messung im Format.
   */
  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
  public Date timestamp;
  
  /**
   * Geraete-Bezeichnung.
   */
  public String device;
}

/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Controller-Bean fuer die Konfiguration.
 */
@Lifecycle(Type.REQUEST)
public class Config
{
  @Request
  private HttpServletRequest request = null;
  
  private List<Configurable> configs = null;
  
  private String status = null;
  
  /**
   * Liefert eine Liste der Configurables.
   * @return Liste der Configurables.
   */
  public synchronized List<Configurable> getConfigs()
  {
    if (this.configs != null)
      return this.configs;
    
    this.configs = new ArrayList<Configurable>();
    try
    {
      ClassFinder finder = Application.getPluginLoader().getManifest(Plugin.class).getClassLoader().getClassFinder();
      Class<Configurable>[] found = finder.findImplementors(Configurable.class);
      for (Class<Configurable> c:found)
      {
        try
        {
          this.configs.add(c.getDeclaredConstructor().newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load configurable " + c + " - skipping",e);
        }
      }
      
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("no configurables found");
    }
    return this.configs;
  }
  
  /**
   * Speichert die Konfigurationen.
   * @throws Exception
   */
  public void save() throws Exception
  {
    List<Configurable> configs = this.getConfigs();
    for (Configurable c:configs)
    {
      List<Parameter> parameters = c.getParameters();
      for (Parameter p:parameters)
        p.setValue(request.getParameter(p.getUuid()));
      c.setParameters(parameters);
    }
    this.status = "settings saved";
  }
  
  /**
   * Liefert den aktuellen Status-Text.
   * @return der Status-Text.
   */
  public String getStatus()
  {
    return this.status;
  }
}

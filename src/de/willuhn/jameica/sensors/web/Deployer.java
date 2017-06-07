/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web;

import java.io.File;

import org.eclipse.jetty.security.LoginService;

import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer;
import de.willuhn.jameica.webadmin.server.JameicaLoginService;

/**
 * Deployer fuer das Hibiscus-Webfrontend.
 */
public class Deployer extends AbstractWebAppDeployer
{
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getContext()
   */
  protected String getContext()
  {
    return "/sensors";
  }

  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getPath()
   */
  protected String getPath()
  {
    Manifest mf = Application.getPluginLoader().getManifest(Plugin.class);
    return mf.getPluginDir() + File.separator + "webapps" + getContext();
  }
  
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getSecurityRoles()
   */
  protected String[] getSecurityRoles()
  {
    return new String[]{"admin"};
  }
  
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getLoginService()
   */
  @Override
  protected LoginService getLoginService()
  {
    return de.willuhn.jameica.webadmin.Settings.getUseAuth() ? new JameicaLoginService() : null;
  }
}

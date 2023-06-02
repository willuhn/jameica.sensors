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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.DeviceRegistry;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.Scheduler;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Scheduler-Services.
 */
public class SchedulerImpl implements Scheduler
{
  private Timer timer = null;
  private Worker worker = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "scheduler service";
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
    return this.timer != null && this.worker != null;
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

    Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    int minutes = settings.getInt("scheduler.interval.minutes",5);
    Logger.info("scheduler interval: " + minutes + " minutes");

    this.timer = new Timer(getName(),true);
    this.worker = new Worker();

    Logger.info("starting scheduler worker thread");
    // Wir fangen erst nach 10 Sekunden mit dem ersten Durchlauf an. Dann
    // hat das System genug Zeit zu Ende zu starten.
    this.timer.schedule(this.worker,10 * 1000L, minutes * 60 * 1000L);
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

    if (this.worker != null)
    {
      try
      {
        Logger.info("stopping worker thread");
        this.worker.cancel();
      }
      catch (Exception e) {
        Logger.error("error while stopping worker thread",e);
      }
      finally
      {
        this.worker = null;
      }
    }

    if (this.timer != null)
    {
      try
      {
        Logger.info("stopping timer task");
        this.timer.cancel();
      }
      catch (Exception e) {
        Logger.error("error while stopping timer task",e);
      }
      finally
      {
        this.timer = null;
      }
    }
  }

  /**
   * Unser Worker.
   */
  private class Worker extends TimerTask
  {
    /**
     * @see java.util.TimerTask#run()
     */
    public void run()
    {
      try
      {
        List<Device> devices = DeviceRegistry.getDevices();
        for (Device d:devices)
        {
          String name = d.getName();

          if (!d.isEnabled())
          {
            Logger.debug("skipping device " + name + " - not configured or disabled");
            continue;
          }
          
          try
          {
            Measurement m = d.collect();
            if (m == null)
            {
              Logger.debug("skipping device " + name + " - returned no data");
              continue;
            }
            
            if (m.getDate() == null) m.setDate(new Date());
            
            Logger.info("collected data from device: " + name);
            Application.getMessagingFactory().sendMessage(new MeasureMessage(d,m));
          }
          catch (IOException e)
          {
            Logger.error("error while collecting data from device " + name,e);
          }
        }
      }
      catch (Exception e)
      {
        Logger.error("error while collecting device data, stopping scheduler",e);
        try
        {
          stop(true);
        }
        catch (Exception e2)
        {
          Logger.error("error while stopping scheduler",e2);
        }
      }
    }
  }
}

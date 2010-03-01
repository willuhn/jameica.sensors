/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/notifier/Mail.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/01 18:12:23 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify.notifier;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.sensors.notify.Rule;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Notifiers, der via eMail benachrichtigt.
 */
public class Mail implements Notifier
{
  /**
   * @see de.willuhn.jameica.sensors.notify.notifier.Notifier#notify(de.willuhn.jameica.sensors.devices.Sensor, de.willuhn.jameica.sensors.notify.Rule, java.util.Map)
   */
  public void notify(Sensor sensor, Rule rule) throws Exception
  {
    Map<String,String> params = rule.getParams();
    
    if (params == null) // erspart uns unnoetige NULL-Checks
      params = new HashMap<String,String>();
    
    Class<? extends Serializer> c = sensor.getSerializer();
    if (c == null)
      c = StringSerializer.class;
    Serializer serializer = c.newInstance();

    String value = serializer.format(sensor.getValue());
    

    ////////////////////////////////////////////////////////////////////////////
    // Authentifizierung
    Authenticator auth = null;
    final String user = params.get("smtp.username");
    final String pw   = params.get("smtp.password");
    if (user != null && pw != null && user.length() > 0 && pw.length() > 0)
    {
      auth = new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(user,pw);
        }
      };
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // SMTP-Host
    Properties props = System.getProperties();
    String host = params.get("smtp.host");
    props.put("mail.smtp.host",host != null && host.length() > 0 ? host : "localhost");
    //
    ////////////////////////////////////////////////////////////////////////////


    Session session = Session.getDefaultInstance(props,auth);
    MimeMessage mime = new MimeMessage(session);
    
    ////////////////////////////////////////////////////////////////////////////
    // Betreff
    String subject = params.get("mail.subject");
    if (subject == null || subject.length() == 0)
    {
      String name = Application.getPluginLoader().getManifest(Plugin.class).getName();
      subject = "[" + name + "] " + sensor.getName() + ": " + value;
    }
    mime.setSubject(subject);
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Absender
    String from = params.get("mail.sender");
    if (from != null && from.length() > 0)
      mime.setFrom(new InternetAddress(from));
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Empfaenger
    String recipients = params.get("mail.recipients");
    if (recipients == null || recipients.length() == 0)
      throw new Exception("no recipient(s) given. please add param 'mail.recipients' to you notify rule");
    
    String[] rl = recipients.split("[,; ]");
    for (String r:rl)
    {
      r = r.trim();
      if (r.length() == 0)
        continue;
      mime.addRecipient(RecipientType.TO,new InternetAddress(r));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Body
    String body = params.get("mail.text");
    if (body == null || body.length() == 0)
    {
      body = "Limit exceeded\n\n" +
             "Sensor name  : " + sensor.getName() + "\n" +
             "Sensor uuid  : " + sensor.getUuid() + "\n\n" +
             "Current Value: " + value + "\n" +
             "Limit        : " + serializer.format(serializer.unserialize(rule.getLimit()));
    }
    
    mime.setText(body);
    

    mime.setSentDate(new Date());
    Logger.info("sending mail [" + subject + "] to " + recipients);
    Transport.send(mime);
    Logger.info("message sent");
  }
}



/**********************************************************************
 * $Log: Mail.java,v $
 * Revision 1.1  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 17:08:18  willuhn
 * @N Mail-Benachrichtigung via javax.mail
 *
 **********************************************************************/
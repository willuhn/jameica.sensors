/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/notifier/Mail.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/03/01 23:51:07 $
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

import de.willuhn.logging.Logger;

/**
 * Implementierung eines Notifiers, der via eMail benachrichtigt.
 */
public class Mail implements Notifier
{
  /**
   * @see de.willuhn.jameica.sensors.notify.notifier.Notifier#outsideLimit(java.lang.String, java.lang.String, java.util.Map, boolean)
   */
  public void outsideLimit(String subject, String description, Map<String,String> params, boolean again) throws Exception
  {
    // Wir schicken die Mail nur beim ersten Mal
    if (again)
      return;
    
    send(subject,description,params);
  }

  /**
   * @see de.willuhn.jameica.sensors.notify.notifier.Notifier#insideLimit(java.lang.String, java.lang.String, java.util.Map)
   */
  public void insideLimit(String subject, String description, Map<String,String> params) throws Exception
  {
    send(subject,description,params);
  }
  
  /**
   * Sendet die Mail.
   * @param subject Betreff.
   * @param description Beschreibungstext.
   * @param params Zustellparameter.
   * @throws Exception
   */
  private void send(String subject, String description, Map<String,String> params) throws Exception
  {
    if (params == null) // erspart uns unnoetige NULL-Checks
      params = new HashMap<String,String>();

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
    mime.setSubject(subject);
    mime.setText(description);
    mime.setSentDate(new Date());
    
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

    Logger.info("sending mail [" + subject + "] to " + recipients);
    Transport.send(mime);
    Logger.info("message sent");
  }
}



/**********************************************************************
 * $Log: Mail.java,v $
 * Revision 1.2  2010/03/01 23:51:07  willuhn
 * @N Benachrichtigung, wenn Sensor zurueck im normalen Bereich ist
 * @N Merken des letzten Notify-Status, sodass nur beim ersten mal eine Mail gesendet wird
 *
 * Revision 1.1  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 17:08:18  willuhn
 * @N Mail-Benachrichtigung via javax.mail
 *
 **********************************************************************/
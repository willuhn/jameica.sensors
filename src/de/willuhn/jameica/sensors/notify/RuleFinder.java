/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/Attic/RuleFinder.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/01 13:16:12 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify;

import java.util.ArrayList;
import java.util.List;

/**
 * Durchsucht das Workverzeichnis nach Notify-Regeln in Form von XML-Dateien.
 */
public class RuleFinder
{
  /**
   * Liefert die gefundenen Regeln.
   * @return Liste der gefundenen Regeln.
   */
  public List<Rule> getRules()
  {
    List<Rule> rules = new ArrayList<Rule>();
    
    // TODO: Hier weiter
    
    return rules;
  }
}



/**********************************************************************
 * $Log: RuleFinder.java,v $
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/
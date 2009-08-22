import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/Attic/Test.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/08/22 00:03:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

public class Test
{
  private final static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
  
  public static void main(String[] args) throws Exception
  {
    new File("/tmp/install/test.rrd").delete();
    RrdDef def = new RrdDef("/tmp/install/test.rrd");
    
    Date start = new Date(Long.parseLong(calc("21.08.2009 17:05")) * 1000L);
    System.out.println(start);
    def.setStartTime(start);
    def.addDatasource("test",DsType.GAUGE,600,Double.NaN,Double.NaN);
    def.addDatasource("test2",DsType.GAUGE,600,Double.NaN,Double.NaN);
    def.addArchive(ConsolFun.AVERAGE,0.5,1,288); // letzte 24h (24h = 1440min -> 5min-Intervall -> 288 Werte
    def.addArchive(ConsolFun.AVERAGE,0.5,12,168); // letzte Woche (7 Tage = 168h), Stundenmittel (60min/5)
    def.addArchive(ConsolFun.AVERAGE,0.5,288,365); // letztes Jahr (365 Tage), Tagesmittel (24*60m/5)
    
    RrdDb db = new RrdDb(def);
    Sample s = db.createSample();
    s.setAndUpdate(calc("21.08.2009 17:10") + ":20");
    s.setAndUpdate(calc("21.08.2009 17:15") + ":20");
    s.setAndUpdate(calc("21.08.2009 17:20") + ":21");
    s.setAndUpdate(calc("21.08.2009 17:25") + ":23");
    s.setAndUpdate(calc("21.08.2009 17:30") + ":27");
    s.setAndUpdate(calc("21.08.2009 17:35") + ":30");
    s.setAndUpdate(calc("21.08.2009 17:40") + ":40");
    s.setAndUpdate(calc("21.08.2009 17:45") + ":20");
    s.setAndUpdate(calc("21.08.2009 17:50") + ":19");
    s.setAndUpdate(calc("21.08.2009 17:55") + ":18");
    s.setAndUpdate(calc("21.08.2009 18:00") + ":20");
    s.setAndUpdate(calc("21.08.2009 18:05") + ":21");
    s.setAndUpdate(calc("21.08.2009 18:10") + ":22");
    s.setAndUpdate(calc("21.08.2009 18:15") + ":23");
    s.setAndUpdate(calc("21.08.2009 18:20") + ":24");
    s.setAndUpdate(calc("21.08.2009 18:25") + ":25");
    s.setAndUpdate(calc("21.08.2009 18:30") + ":30");
    s.setAndUpdate(calc("21.08.2009 18:35") + ":33");
    s.setAndUpdate(calc("21.08.2009 18:40") + ":30");
    s.setAndUpdate(calc("21.08.2009 18:45") + ":50");
    s.setAndUpdate(calc("21.08.2009 18:50") + ":20");
    s.setAndUpdate(calc("21.08.2009 18:55") + ":22");
    s.setAndUpdate(calc("21.08.2009 19:00") + ":23");
    s.setAndUpdate(calc("21.08.2009 19:05") + ":24");
    s.setAndUpdate(calc("21.08.2009 19:10") + ":26");
    s.setAndUpdate(calc("21.08.2009 19:15") + ":27");
    s.setAndUpdate(calc("21.08.2009 19:20") + ":28");
    s.setAndUpdate(calc("21.08.2009 19:25") + ":30");
    s.setAndUpdate(calc("21.08.2009 19:30") + ":26");
    s.setAndUpdate(calc("21.08.2009 19:35") + ":24");
    s.setAndUpdate(calc("21.08.2009 19:40") + ":23");
    s.setAndUpdate(calc("21.08.2009 19:45") + ":21");
    db.close();

    RrdGraphDef gd = new RrdGraphDef();
    gd.setTimeSpan(Long.parseLong(calc("21.08.2009 17:10")),
                   Long.parseLong(calc("21.08.2009 20:00"))
        );
    gd.datasource("room","/tmp/install/test.rrd","test",ConsolFun.AVERAGE);
    gd.line("room",new Color(255,0,0),"Raum-Temperatur",2);
    gd.setFilename("/tmp/install/temp.png");
    gd.setImageFormat("PNG");
    gd.setMaxValue(0d);
    gd.setMaxValue(60d);
    gd.setTitle("Temperaturen");
    gd.setVerticalLabel("°C");
    
    
    RrdGraph gr = new RrdGraph(gd);
    BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB); // Die Groessenangabe wird irgendwie ignoriert
    gr.render(bi.getGraphics());
    
  }
  
  private static String calc(String date) throws Exception
  {
    Date d = df.parse(date);
    return String.valueOf(d.getTime() / 1000L);
  }
}


/**********************************************************************
 * $Log: Test.java,v $
 * Revision 1.3  2009/08/22 00:03:42  willuhn
 * @N Das Zeichnen der Charts funktioniert! ;)
 *
 * Revision 1.1  2009/08/21 17:27:37  willuhn
 * @N RRD-Service
 *
 **********************************************************************/

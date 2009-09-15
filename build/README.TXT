==== Kurz-Anleitung ====


1) System-Voraussetzungen

  - Betriebssystem: Linux oder Windows (32Bit)
  - Java: SUN Java 1.5 oder hoeher
  
2) Installation

  - Jameica 1.9 Nightly-Build herunterladen und entpacken
    http://www.willuhn.de/products/jameica/releases/nightly/jameica-1.9.0-nightly.zip
    
  - jameica.webadmin herunterladen und im Ordner "plugins" von Jameica entpacken
    http://www.willuhn.de/products/jameica/releases/nightly/jameica.webadmin-1.1-nightly.zip
    
  - jameica.webtools herunterladen und im Ordner "plugins" von Jameica entpacken
    http://www.willuhn.de/products/jameica/releases/nightly/jameica.webtools-1.1-nightly.zip
    
  - jameica.sensors herunterladen und im Ordner "plugins" von Jameica entpacken
    http://www.willuhn.de/products/jameica/releases/nightly/jameica.sensors-1.0-nightly.src.zip
    
3) Start-Script anpassen

   Fuer den Zugriff auf den seriellen Port (fuer die Waterkotte Ai1 WPCU) muss eine
   native Bibliothek geladen werden, die sich im Unterverzeichnis
   plugins/jameica.sensors/lib/rxtx befindet. Hierzu muss das Startscript von
   Jameica angepasst werden.
   
   Linux: Oeffne jameica.sh in einem Texteditor, aendere die Zeile
   
          java -Xmx256m $_JCONSOLE -jar jameica-linux.jar $@
          
          in
          
          java -Djava.library.path=/opt/jameica/plugins/jameica.sensors/lib/rxtx -Xmx256m $_JCONSOLE -jar jameica-linux.jar $@
          
          
   Windows: Oeffne jameica.bat in einem Texteditor, aendere die Zeile

          start javaw.exe -Xmx256m -jar jameica-win32.jar %1 %2 %3 %4 %5 %6 %7
          
          in
             
          start javaw.exe  -Djava.library.path=C:/Programme/jameica/plugins/jameica.sensors/lib/rxtx  -Xmx256m -jar jameica-win32.jar %1 %2 %3 %4 %5 %6 %7

          Hinweis: Keine Backslashes "\" sondern tatsaechlich Slash "/" verwenden!
   
   Passe den Pfad zum Unterverzeichnis "rxtx" ggf an, falls Jameica in einem
   anderen Verzeichnis installiert wurde.
          
4) Erster Starte

   - Starte jameica.bat bzw. jameica.sh
   - Vergib ein beliebiges Masterpasswort
   - Starte Jameica anschliessend erneut
   
   
5) Konfiguration

   - Starte Jameica
   - Oeffne einen Browser und gib folgende Adresse ein:
     https://localhost:8080/sensors
   - Melde dich mit dem Benutzernamen "admin" und dem Masterpasswort an.
   - Klicke auf den Link "Configuration"
   - Trage den Namen/Pfad des seriellen Ports (Parameter "Serieller Port")
     einen passenden Wert ein. Fuer Linux meist "/dev/ttyS0", für Windows meist "COM1".

6) Optional: Datenbank

   Falls die Messwerte ausserdem in einer Datenbank gespeichert werden sollen,
   lege einen Datenbankbenutzer sowie eine leere Datenbank an und trage die
   entsprechenden Parameter im Absatz "archive service" ein.
   

   Oeffne   
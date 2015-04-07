==== Kurz-Anleitung ====


1) System-Voraussetzungen

  - Betriebssystem: Linux oder Windows
  - Java: SUN Java 1.6 oder hoeher
  
2) Installation
  - Jameica herunterladen und entpacken
    http://willuhn.de/products/jameica/download.php
    
  - jameica.webadmin herunterladen und im Ordner "plugins" von Jameica entpacken
    http://willuhn.de/products/jameica/download_ext.php
    
  - jameica.sensors herunterladen und im Ordner "plugins" von Jameica entpacken
    http://willuhn.de/products/jameica/download_ext.php
    
3) Start-Script anpassen

   Fuer den Zugriff auf den seriellen Port (fuer die Waterkotte Ai1 WPCU) muss eine
   native Bibliothek geladen werden, die sich im Unterverzeichnis
   plugins/jameica.sensors/lib/rxtx/[lib32/lib64] befindet. Hierzu muss das Startscript von
   Jameica angepasst werden.
   
   Linux: Oeffne jameica.sh in einem Texteditor, aendere die Zeile
   
          java -Xmx256m $_JCONSOLE -jar jameica-linux.jar $@
          
          in
          
          java -Djava.library.path=/opt/jameica/plugins/jameica.sensors/lib/rxtx/lib64 -Xmx256m $_JCONSOLE -jar jameica-linux64.jar $@
          
          
   Windows: Oeffne jameica.bat in einem Texteditor, aendere die Zeile

          start javaw.exe -Xmx256m -jar jameica-win32.jar %1 %2 %3 %4 %5 %6 %7
          
          in
             
          start javaw.exe  -Djava.library.path=C:/Programme/jameica/plugins/jameica.sensors/lib/rxtx/lib32  -Xmx256m -jar jameica-win32.jar %1 %2 %3 %4 %5 %6 %7

          Hinweis: Keine Backslashes "\" sondern tatsaechlich Slash "/" verwenden!
   
   Passe den Pfad zum Unterverzeichnis "rxtx" ggf an, falls Jameica in einem
   anderen Verzeichnis installiert wurde.
          
4) Erster Starte

   - Starte jameica.bat bzw. jameica.sh (ggf. mit dem Parameter "-d" fuer Start ohne GUI)
   - Vergib ein beliebiges Masterpasswort
   - Starte Jameica anschliessend erneut
   
5) Konfiguration

   - Oeffne einen Browser und gib folgende Adresse ein:
     https://localhost:8080/sensors
   - Melde dich mit dem Benutzernamen "admin" und dem Masterpasswort an.
   - Klicke auf den Link "Configuration"
   - Trage den Namen/Pfad des seriellen Ports (Parameter "Serieller Port")
     einen passenden Wert ein. Fuer Linux meist "/dev/ttyS0", fuer Windows meist "COM1".

6) Optional: Datenbank

   Falls die Messwerte ausserdem in einer Datenbank gespeichert werden sollen,
   lege einen Datenbankbenutzer sowie eine leere Datenbank an und trage die
   entsprechenden Parameter im Absatz "archive service" ein.
   Achtung: Im Laufe der Zeit sammeln sich da eine ganze Menge Daten an.
   
7) Verwendung

   - Oeffne einen Browser und gib folgende Adresse ein:
     https://localhost:8080/sensors
   - Melde dich mit dem Benutzernamen "admin" und dem Masterpasswort an.
     
     Es werden die aktuellen Messwerte angezeigt. Sie werden im 5-Minutentakt
     aktualisiert. Mit einem Klick auf "charts" kann der grafische Verlauf
     angezeigt werden.
     
8) Fehler?

   - Falls das Auslesen der Messwerte der Heizung nicht funktioniert, pruefe
     ggf. aufgetretene Fehlermeldungen in der Lof-Datei "jameica.log". Sie
     befindet sich im Jameica-Benutzerverzeichnis. Siehe
     http://hibiscus.berlios.de/doku.php?id=support:backup#benutzerverzeichnis
     
9) Hinweise

   - Ich habe den seriellen Zugriff auf die Heizung ausschliesslich unter
     Linux getestet. Ich weiss daher nicht, ob es unter Windows tatsaechlich
     funktioniert.
        
# Scotland Yard Re-Imagined: Server & App
Willkommen beim Scotland Yard Re-Imagined Projekt! Dies ist eine moderne Neuinterpretation des klassischen Brettspiels Scotland Yard, aufgeteilt in eine Java-basierte Serveranwendung und eine Android-App, entwickelt mit Kotlin. Tauche ein in die spannende Verfolgungsjagd nach Mr. X – spiele online mit Freunden oder gegen clevere Bots!

## Funktionen
**Multiplayer-Erlebnis:** Spiele Scotland Yard online mit deinen Freunden.
**Intelligente Bots:** Keine Lust zu warten? Füge Bots hinzu, um fehlende Spieler zu ersetzen oder alleine zu üben.
**Cross-Plattform:** Der Java-Server läuft plattformunabhängig, und die Kotlin-App ist kompatibel mit allen Android-Geräten und Emulatoren.
## Installation & Start
Um das Spiel zum Laufen zu bringen, musst du zuerst den Server starten und anschließend die Android-App öffnen.

### 1. Server starten (Java)
**Voraussetzungen:**

Stelle sicher, dass Java (JRE oder JDK, Version 11 oder höher empfohlen) auf deinem System installiert ist.

**Server herunterladen:**

Lade die neueste Version der ScotlandYardServer.jar-Datei herunter.

**Server starten:**

Öffne ein Terminal oder eine Eingabeaufforderung und navigiere zu dem Verzeichnis, in dem du die .jar-Datei gespeichert hast. Führe dann den folgenden Befehl aus:

Bash

java -jar ScotlandYardServer.jar
Der Server wird gestartet und wartet auf Verbindungen von den Apps. Achte auf eventuelle Firewall-Abfragen und erlaube dem Server, Verbindungen anzunehmen.

### 2. App starten (Kotlin/Android)
**Voraussetzungen:**

Du benötigst ein Android-Gerät (mindestens Android-Version X, falls zutreffend) oder einen Android-Emulator (z.B. über Android Studio).

**App herunterladen:**

Lade die neueste ScotlandYardApp.apk-Datei von den [Releases](Link zu deinen GitHub Releases) herunter.

**App installieren:**

Auf dem Gerät: Übertrage die .apk-Datei auf dein Android-Handy und installiere sie. Möglicherweise musst du die Installation aus "unbekannten Quellen" zulassen.
Im Emulator: Ziehe die .apk-Datei einfach in das Emulatorfenster, um sie zu installieren.
**App starten:**

Öffne die Scotland Yard App auf deinem Gerät oder Emulator. Die App sollte sich automatisch mit dem laufenden Server verbinden.

## Spielanleitung
Sobald die App mit dem Server verbunden ist, kannst du:

Ein neues Spiel erstellen oder einem bestehenden Spiel beitreten.
Deine Rolle wählen (Detektiv oder Mr. X).
Bots hinzufügen, um die Spielerzahl aufzufüllen.
Spielen!
## Technologien
Server: Java
Client (App): Kotlin, Android

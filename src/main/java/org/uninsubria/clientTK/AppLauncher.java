package org.uninsubria.clientTK;

/**
 * Classe di bootstrap per bypassare i controlli dei moduli nativi di Java 11+.
 * Questa classe deve essere l'Entry Point (Main) configurato nell'IDE.
 */
public class AppLauncher {
    public static void main(String[] args) {
        // Delega l'avvio al vero client JavaFX
        MainClient.main(args);
    }
}
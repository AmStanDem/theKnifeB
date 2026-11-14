/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client;

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
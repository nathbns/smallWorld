import vuecontroleur.VueControleur;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Configuration du look and feel pour un meilleur rendu
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Utiliser le L&F par défaut
        }
        
        SwingUtilities.invokeLater(() -> {
            VueControleur vc = new VueControleur();
            vc.setVisible(true);
        });
    }
}

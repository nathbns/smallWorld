import vuecontroleur.VueControleur;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        
        SwingUtilities.invokeLater(() -> {
            VueControleur vc = new VueControleur();
            vc.setVisible(true);
        });
    }
}

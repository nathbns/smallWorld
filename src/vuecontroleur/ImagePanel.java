package vuecontroleur;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private Image imgBackground;
    private Image imgFront;
    private int nb = 3;
    private JTextArea nbUnites = new JTextArea(String.valueOf(nb));

    public void setBackground(Image _imgBackground) {
        imgBackground = _imgBackground;
    }

    public void setFront(Image _imgFront) {
        imgFront = _imgFront;
    }

    public void setNbUnites(int i){nb = i;}
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // cadre
        g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 1, 1);
                
        if (imgBackground != null) {

            g.drawImage(imgBackground, 2, 2, getWidth()-4, getHeight()-4, this);
        }

        if (imgFront != null) {
            g.drawImage(imgFront, 10, 10, (int) (getWidth()*0.5), (int) (getHeight()*0.5), this);
        }
        nbUnites.setLocation(getWidth()-25,getHeight()-25);
        nbUnites.setSize(25,25);
        nbUnites.setOpaque(false);
        add(nbUnites);

    }
}
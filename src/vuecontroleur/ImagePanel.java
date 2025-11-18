package vuecontroleur;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private Image imgBackground;
    private Image imgFront;
    private int nb = 0;
    private JTextArea nbUnites = new JTextArea(String.valueOf(nb));
    private Color borderColor = null;

    public void setBackground(Image _imgBackground) {
        imgBackground = _imgBackground;
    }

    public void setFront(Image _imgFront) {
        imgFront = _imgFront;
    }

    public void setNbUnites(int i){nb = i;}
    
    public void setBorderColor(Color color) {
        borderColor = color;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // cadre normal
        if (borderColor == null) {
            g.setColor(Color.BLACK);
            g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 1, 1);
        } else {
            // cadre coloré pour indiquer la sélection/disponibilité
            g.setColor(borderColor);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 1, 1);
        }
                
        if (imgBackground != null) {

            g.drawImage(imgBackground, 2, 2, getWidth()-4, getHeight()-4, this);
        }

        if (imgFront != null) {
            g.drawImage(imgFront, 10, 10, (int) (getWidth()*0.5), (int) (getHeight()*0.5), this);
        }

        if(nb > 1){
            nbUnites.setLocation(getWidth()-25,getHeight()-25);
            //nbUnites.setPreferredSize(new Dimension(20,20));
            //nbUnites.setSize(new Dimension(30,30));
            nbUnites.setOpaque(false);
            add(nbUnites);
        }

    }
}

package vuecontroleur;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private Image imgBackground;
    private Image imgFront;

    public void setBackground(Image _imgBackground) {
        imgBackground = _imgBackground;
    }

    public void setFront(Image _imgFront) {
        imgFront = _imgFront;
    }
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


    }
}

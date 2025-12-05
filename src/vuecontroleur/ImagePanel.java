package vuecontroleur;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private Image imgBackground;
    private Image imgFront;
    private int nb = 0;
    private Color borderColor = null;
    private Color fillColor = null;

    public void setBackground(Image _imgBackground) {
        imgBackground = _imgBackground;
    }

    public void setFront(Image _imgFront) {
        imgFront = _imgFront;
    }

    public void setNbUnites(int i) {
        nb = i;
    }
    
    public void setBorderColor(Color color) {
        borderColor = color;
    }

    public void setFillColor(Color color) {
        fillColor = color;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Dessiner le fond (terrain)
        if (imgBackground != null) {
            g.drawImage(imgBackground, 2, 2, getWidth()-4, getHeight()-4, this);
        }

        // Dessiner les unités (1 à 4 sprites selon le nombre)
        if (imgFront != null && nb > 0) {
            int spriteW = (int) (getWidth() * 0.4);
            int spriteH = (int) (getHeight() * 0.4);
            
            int nbAffiche = Math.min(nb, 4); // Max 4 sprites affichés
            
            // Positions pour 1, 2, 3 ou 4 sprites
            int[][] positions = getPositionsSprites(nbAffiche, spriteW, spriteH);
            
            for (int i = 0; i < nbAffiche; i++) {
                g.drawImage(imgFront, positions[i][0], positions[i][1], spriteW, spriteH, this);
            }
        }

        // Dessiner la bordure
        if (borderColor == null) {
            g.setColor(new Color(160, 140, 110)); // Bordure beige rétro
            g2d.setStroke(new BasicStroke(2));
            g.drawRect(1, 1, getWidth()-2, getHeight()-2);
        } else {
            // Bordure colorée bien visible pour sélection/disponibilité
            // D'abord remplir le fond
            g2d.setColor(fillColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Puis dessiner une bordure épaisse
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(6));
            g2d.drawRect(3, 3, getWidth()-6, getHeight()-6);
        }

        // Afficher le nombre seulement si > 4
        if (nb > 4) {
            String text = String.valueOf(nb);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            
            int x = getWidth() - textWidth - 8;
            int y = getHeight() - 8;
            
            // Fond pour le texte (style rétro)
            g2d.setColor(new Color(220, 210, 190, 220));
            g2d.fillRoundRect(x - 4, y - textHeight, textWidth + 8, textHeight + 4, 6, 6);
            
            // Texte brun rétro
            g2d.setColor(new Color(120, 80, 50));
            g2d.drawString(text, x, y);
        }
    }
    
    // Calcule les positions des sprites selon leur nombre
    private int[][] getPositionsSprites(int count, int spriteW, int spriteH) {
        int[][] positions = new int[count][2];
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        switch (count) {
            case 1:
                // Centré
                positions[0] = new int[]{centerX - spriteW/2, centerY - spriteH/2};
                break;
            case 2:
                // Côte à côte
                positions[0] = new int[]{centerX - spriteW - 2, centerY - spriteH/2};
                positions[1] = new int[]{centerX + 2, centerY - spriteH/2};
                break;
            case 3:
                // Triangle : 1 en haut, 2 en bas
                positions[0] = new int[]{centerX - spriteW/2, 5};
                positions[1] = new int[]{centerX - spriteW - 2, centerY};
                positions[2] = new int[]{centerX + 2, centerY};
                break;
            case 4:
                // Grille 2x2
                positions[0] = new int[]{centerX - spriteW - 2, 5};
                positions[1] = new int[]{centerX + 2, 5};
                positions[2] = new int[]{centerX - spriteW - 2, centerY};
                positions[3] = new int[]{centerX + 2, centerY};
                break;
        }
        
        return positions;
    }
}

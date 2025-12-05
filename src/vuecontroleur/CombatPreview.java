package vuecontroleur;

import modele.plateau.Case;

import javax.swing.*;
import java.awt.*;

public class CombatPreview extends JPanel {

    public int attaqueUnite;
    public int defenseUnite;
    public double attaquePourcent;

    public JPanel panel;

    private JLabel pourcentAttaque;
    private JLabel pourcentDefense;

    private JLabel textAttaque;
    private JLabel textDefense;
    private JLabel tiret;

    public Color colorAttaque = new Color(0,0,0);
    public Color colorDefense = new Color(0,0,0);

    public CombatPreview(){
        attaqueUnite = 0;
        defenseUnite = 0;
        panel = new JPanel(new FlowLayout());
        pourcentAttaque = new JLabel();
        pourcentDefense = new JLabel();
        textAttaque = new JLabel();
        textDefense = new JLabel();
        tiret = new JLabel();

        textAttaque.setFont(new Font("Arial",Font.BOLD,22));
        textDefense.setFont(new Font("Arial",Font.BOLD,22));
        tiret.setFont(new Font("Arial",Font.BOLD,22));

        // Textes vides
        pourcentAttaque.setText(" ");
        pourcentDefense.setText(" ");
        textAttaque.setText(" ");
        textDefense.setText(" ");
        tiret.setText(" ");

        // Respecter l'ordre pour le conserver dans la fenêtre
        panel.add(textAttaque);
        panel.add(pourcentAttaque);
        panel.add(tiret);
        panel.add(pourcentDefense);
        panel.add(textDefense);

    }

    // Calcule les pourcentages pour le combat, les met dans les JLabel et les affiche
    public void calculatePercents(Case c1, Case c2){
        int total = attaqueUnite + defenseUnite;

        if(c1.getBiome() == c1.getUnites().getTypePeuple().getTerrainFavori()){
            colorAttaque = new Color(0,100,0);
        }else if (c1.getBiome() == c1.getUnites().getTypePeuple().getTerrainDeteste()) {
            colorAttaque = new Color(100,0,0);
        }else{
            colorAttaque = new Color(0,0,0);
        }
        if(c2.getBiome() == c2.getUnites().getTypePeuple().getTerrainFavori()){
            colorDefense = new Color(0,100,0);
        }else if (c2.getBiome() == c2.getUnites().getTypePeuple().getTerrainDeteste()) {
            colorDefense = new Color(100,0,0);
        }else{
            colorDefense = new Color(0,0,0);
        }

        attaquePourcent = (double) attaqueUnite / total;
    }

    public void showPercents(){
        pourcentAttaque.setText(Math.round(attaquePourcent * 100) + " %");
        pourcentDefense.setText(Math.round((1 - attaquePourcent) * 100) + " %");

        //pourcentAttaque.setLocation(getWidth()-35,getHeight()-35); Inutile
        //pourcentDefense.setLocation(getWidth()-35,getHeight()-35);

        pourcentAttaque.setFont(new Font("Arial",Font.BOLD,22));
        pourcentDefense.setFont(new Font("Arial",Font.BOLD,22));

        pourcentAttaque.setForeground(colorAttaque);
        pourcentDefense.setForeground(colorDefense);

        textAttaque.setForeground(colorAttaque);
        textDefense.setForeground(colorDefense);
        textAttaque.setText("Attaque : ");
        textDefense.setText(" : Defense");
        tiret.setText(" - ");
    }

    public void hidePercents(){
        pourcentAttaque.setText(" ");
        pourcentDefense.setText(" ");
        textAttaque.setText(" ");
        textDefense.setText(" ");
        tiret.setText(" ");
    }

}

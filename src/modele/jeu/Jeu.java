package modele.jeu;

import modele.jeu.peuple.*;
import modele.plateau.Plateau;

import java.awt.geom.Ellipse2D;
import java.util.Random;

public class Jeu extends Thread{
    private Plateau plateau;
    private Joueur [] joueurs;
    private Joueur j1;
    private Joueur j2;
    private Joueur j3;
    private Joueur j4;
    protected Coup coupRecu;

    private static final int nbJoueurs = 4;

    public TypePeuple randomPeuple(){
        int pick = new Random().nextInt(TypePeuple.values().length);
        return TypePeuple.values()[pick];
    }

    public Jeu() {

        joueurs = new Joueur[4]; //  Á changer plus tarc avec nbJoueurs

        // Initialisation des joueurs
        for(int i = 0; i < nbJoueurs; i++){
            joueurs[i] = new Joueur(this,randomPeuple(),"Rouge"); // Couleur arbitraire pour l'instant
        }
        j1 = joueurs[0]; // Pour l'instant, sert uniquement pour jouer la partie

        // Taille du plateau
        if(nbJoueurs > 2){
            Plateau.SIZE_X = 7; Plateau.SIZE_Y = 7;
        }else{
            Plateau.SIZE_X = 6; Plateau.SIZE_Y = 6;
        }

        // Initialisation du plateau
        plateau = new Plateau();
        plateau.initialiser();

        // Initialisation des unites des joueurs
        initUnitesJoueurs(joueurs);

        // Initailisation des unites sur le plateau
        plateau.addJoueur(joueurs);


        start();

    }

    // Faire mieux apres mais au moins ça existe
    protected void initUnitesJoueurs(Joueur [] j){

        for(int i = 0; i < j.length; i++){
            if(j[i] == null){
                continue;
            }
            switch (j[i].getPeuple()){
                case ELFE:
                    for(int unites = 0; unites < 5; unites++){ // Nombre d'unites arbitraire pour le moment
                        j[i].ajouterUnite(new Elfe(plateau));
                    }
                    break;
                case GOBELIN:
                    for(int unites = 0; unites < 5; unites++){
                        j[i].ajouterUnite(new Gobelin(plateau));
                    }
                    break;
                case HUMAIN:
                    for(int unites = 0; unites < 5; unites++){
                        j[i].ajouterUnite(new Humain(plateau));
                    }
                    break;
                case NAIN:
                    for(int unites = 0; unites < 5; unites++){
                        j[i].ajouterUnite(new Nain(plateau));
                    }
                    break;
            }
        }

    }

    public Plateau getPlateau() {
        return plateau;
    }

    public void envoyerCoup(Coup c) {
        coupRecu = c;

        synchronized (this) {
            notify();
        }

    }


    public void appliquerCoup(Coup coup) {
        plateau.deplacerUnite(coup.dep, coup.arr);
    }

    public void run() {
        jouerPartie();
    }

    public void jouerPartie() {

        while(true) {
            Coup c = j1.getCoup();
            appliquerCoup(c);

        }

    }


}

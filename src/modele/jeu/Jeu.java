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

    public TypePeuple randomPeuple(){
        int pick = new Random().nextInt(TypePeuple.values().length);
        return TypePeuple.values()[pick];
    }

    public Jeu() {

        joueurs = new Joueur[4];

        // Initialisation des joueurs
        joueurs[0] = new Joueur(this,randomPeuple(),"Rouge"); // Couleur arbitraire pour l'instant
        joueurs[1] = new Joueur(this,randomPeuple(),"Bleu");
        joueurs[2] = new Joueur(this,randomPeuple(),"Jaune");
        joueurs[3] = new Joueur(this,randomPeuple(),"Vert");
        j1 = joueurs[0];

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

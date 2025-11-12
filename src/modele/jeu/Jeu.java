package modele.jeu;

import modele.jeu.peuple.Nain;
import modele.jeu.peuple.TypePeuple;
import modele.plateau.Plateau;

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
        joueurs[0] = new Joueur(this,randomPeuple(),"Rouge");
        joueurs[1] = new Joueur(this,randomPeuple(),"Bleu");
        joueurs[2] = null;
        joueurs[3] = null;
        j1 = joueurs[0];

        // Initialisation du plateau
        plateau = new Plateau();
        plateau.initialiser();

        // Initailisation des unites sur le plateau
        plateau.addJoueur(joueurs);


        start();

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

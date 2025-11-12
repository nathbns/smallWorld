package modele.jeu;

import modele.jeu.peuple.TypePeuple;
import modele.jeu.peuple.Unites;
import modele.plateau.Plateau;
import java.util.ArrayList;
import java.util.List;

public class Joueur {
    private Jeu jeu;
    private TypePeuple peuple;
    private int score;
    private List<Unites> unites;
    private String couleur; // Pour identifier visuellement le joueur

    public Joueur(Jeu _jeu, TypePeuple _peuple, String _couleur) {
        jeu = _jeu;
        peuple = _peuple;
        couleur = _couleur;
        score = 0;
        unites = new ArrayList<>();
    }

    public Joueur(Jeu _jeu) {
        this.jeu = _jeu;
        this.score = 0;
        this.unites = new ArrayList<>();
    }

    public Coup getCoup() {
        synchronized (jeu) {
            try {
                jeu.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return jeu.coupRecu;
    }

    public TypePeuple getPeuple() {
        return peuple;
    }

    public int getScore() {
        return score;
    }

    public void ajouterPoints(int points) {
        score += points;
    }

    public List<Unites> getUnites() {
        return unites;
    }

    public void ajouterUnite(Unites unite) {
        unites.add(unite);
    }

    public String getCouleur() {
        return couleur;
    }

    @Override
    public String toString() {
        return peuple.getNom() + " (" + couleur + ") - Score: " + score;
    }
}

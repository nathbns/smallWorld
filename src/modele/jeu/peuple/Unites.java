package modele.jeu.peuple;

import modele.plateau.Case;
import modele.plateau.Plateau;

/**
 * Entités amenées à bouger
 */
public abstract class Unites {

    protected Case c;
    protected Plateau plateau;
    protected modele.jeu.Joueur proprietaire;
    protected boolean aJoueCeTour; // Pour savoir si l'unité a déjà joué ce tour
    protected boolean aDeplaceOuAttaque; // Pour savoir si l'unité peut encore attaquer après un déplacement

    protected int nbUnit;

    public Unites(Plateau _plateau, int _nbUnit) {
        plateau = _plateau;
        aJoueCeTour = false;
        aDeplaceOuAttaque = false;
        nbUnit = _nbUnit;
    }

    public void quitterCase() {
        c.quitterLaCase();
    }
    
    public void allerSurCase(Case _c) {
        if (c != null) {
            quitterCase();
        }
        c = _c;
        plateau.arriverCase(c, this);
    }

    public Case getCase() {
        return c;
    }

    public void setProprietaire(modele.jeu.Joueur j) {
        proprietaire = j;
    }

    public modele.jeu.Joueur getProprietaire() {
        return proprietaire;
    }

    public boolean aJoueCeTour() {
        return aJoueCeTour;
    }

    public void marquerCommeJouee() {
        aJoueCeTour = true;
    }

    public void resetTour() {
        aJoueCeTour = false;
        aDeplaceOuAttaque = false;
    }

    public boolean aDeplaceOuAttaque() {
        return aDeplaceOuAttaque;
    }

    public void marquerDeplaceOuAttaque() {
        aDeplaceOuAttaque = true;
    }

    public int getNbUnit(){
        return nbUnit;
    }

    // Méthodes abstraites pour les capacités spéciales de chaque peuple
    public abstract int getPorteeAttaque();
    public abstract int getPorteeDeplacement();
    public abstract int getForceAttaque();
    public abstract int getForceDefense();
    public abstract TypePeuple getTypePeuple();

}

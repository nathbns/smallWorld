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
    protected boolean aAttaqueSansDeplacement; // Pour savoir si l'unité a attaqué sans se déplacer (ne peut plus bouger)

    protected int nbUnit; // Nombre d'unites superposees sur la case

    public Unites(Plateau _plateau, int _nbUnit) {
        plateau = _plateau;
        aJoueCeTour = false;
        aDeplaceOuAttaque = false;
        aAttaqueSansDeplacement = false;
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

    public boolean getJoueCeTour() {
        return aJoueCeTour;
    }

    public boolean getDepalceOuAttaque() {
        return aDeplaceOuAttaque;
    }

    public void setJoueCeTour(boolean b) {
        aJoueCeTour = b;
    }

    public void setDepalceOuAttaque(boolean b) {
        aDeplaceOuAttaque = b;
    }

    public void marquerCommeFinDeTour() {
        aDeplaceOuAttaque = true;
        aJoueCeTour = true;
    }

    public void resetTour() {
        aJoueCeTour = false;
        aDeplaceOuAttaque = false;
        aAttaqueSansDeplacement = false;
    }
    
    public boolean aAttaqueSansDeplacement() {
        return aAttaqueSansDeplacement;
    }
    
    public void marquerAttaqueSansDeplacement() {
        aAttaqueSansDeplacement = true;
        aJoueCeTour = true;
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

    public void setNbUnit(int nb){
        nbUnit = nb;
    }

    public int calculAttaqueTotale(){
        int force = getForceAttaque(); // Force de base de l'unité
        force += (nbUnit-1) * 3; // Bonus en fonction du nombre d'unités sur la case
        if(c.getBiome() == getTypePeuple().getTerrainFavori()){ // Bonus en fonction de la case sur laquelle se trouve l'unité
            force = (int) ((double) force * 1.5); // +50%
        } else if (c.getBiome() == getTypePeuple().getTerrainDeteste()) {
            force = Math.max((int) ((double) force * 0.66),1); // -33%
        }
        return force;
    }

    public int calculDefenseTotale(){
        int def = getForceAttaque(); // Defense de base de l'unité
        def += (nbUnit-1) * 3; // Bonus en fonction du nombre d'unités sur la case
        if(c.getBiome() == getTypePeuple().getTerrainFavori()){ // Bonus en fonction de la case sur laquelle se trouve l'unité
            def = (int) ((double) def * 1.5); // +50%
        } else if (c.getBiome() == getTypePeuple().getTerrainDeteste()) {
            def = Math.max((int) ((double) def * 0.66),1); // -33%
        }
        return def;
    }

    // Méthodes abstraites pour les capacités spéciales de chaque peuple
    public abstract int getPorteeAttaque();
    public abstract int getPorteeDeplacement();
    public abstract int getForceAttaque();
    public abstract int getForceDefense();
    public abstract TypePeuple getTypePeuple();
}

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

    protected int nbUnit; // Nombre d'unites superposees sur la case

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

    public void marquerCommeFinDeTour() {
        aDeplaceOuAttaque = true;
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

    public void setNbUnit(int nb){
        nbUnit = nb;
    }

    public int calculAttaqueTotale(){
        int force = getForceAttaque(); // Force de base de l'unité
        //System.out.println("Force : " + force);
        force += (nbUnit-1) * 3; // Bonus en fonction du nombre d'unités sur la case
        //System.out.println("Bonus : " + force);
        if(c.getBiome() == getTypePeuple().getTerrainFavori()){ // Bonus en fonction de la case sur laquelle se trouve l'unité
            force = (int) ((double) force * 1.5); // +50%
            //System.out.println("Terrain : " + force);
        } else if (c.getBiome() == getTypePeuple().getTerrainDeteste()) {
            force = Math.max((int) ((double) force * 0.66),1); // -33%
            //System.out.println("Terrain : " + force);
        }
        return force;
    }

    public int calculDefenseTotale(){
        int def = getForceAttaque(); // Defense de base de l'unité
        //System.out.println("Def : " + def);
        def += (nbUnit-1) * 3; // Bonus en fonction du nombre d'unités sur la case
        //System.out.println("Bonus : " + def);
        if(c.getBiome() == getTypePeuple().getTerrainFavori()){ // Bonus en fonction de la case sur laquelle se trouve l'unité
            def = (int) ((double) def * 1.5); // +50%
            //System.out.println("Terrain : " + def);
        } else if (c.getBiome() == getTypePeuple().getTerrainDeteste()) {
            def = Math.max((int) ((double) def * 0.66),1); // -33%
            //System.out.println("Terrain : " + def);
        }
        return def;
    }

    // Méthodes abstraites pour les capacités spéciales de chaque peuple
    public abstract int getPorteeAttaque();
    public abstract int getPorteeDeplacement();
    public abstract int getForceAttaque();
    public abstract int getForceDefense();
    public abstract TypePeuple getTypePeuple();
    // Peut etre plus pertinent si abstrait mais a voir plus tard
    //public abstract int calculAttaqueTotale();
    //public abstract int calculDefenseTotale();

}

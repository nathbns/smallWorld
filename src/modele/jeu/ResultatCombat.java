package modele.jeu;

import modele.jeu.peuple.Unites;

public class ResultatCombat {
    public Unites attaquant;
    public Unites defenseur;
    public int forceAttaquant;
    public int forceDefenseur;
    public boolean attaquantGagne;
    public String descriptionTerrain;
    
    public ResultatCombat(Unites att, Unites def, int forceAtt, int forceDef, boolean gagneAtt, String terrain) {
        attaquant = att;
        defenseur = def;
        forceAttaquant = forceAtt;
        forceDefenseur = forceDef;
        attaquantGagne = gagneAtt;
        descriptionTerrain = terrain;
    }
}


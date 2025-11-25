package modele.jeu;

import modele.jeu.peuple.Unites;

public class ResultatCombat {
    public Unites attaquant;
    public Unites defenseur;
    public int forceAttaquant;
    public int forceDefenseur;
    public boolean attaquantGagne;
    public String descriptionTerrainAttaquant;
    public String descriptionTerrainDefenseur;
    
    public ResultatCombat(Unites att, Unites def, int forceAtt, int forceDef, boolean gagneAtt, String terrain1, String terrain2) {
        attaquant = att;
        defenseur = def;
        forceAttaquant = attaquant.calculAttaqueTotale();
        forceDefenseur = defenseur.calculDefenseTotale();
        attaquantGagne = gagneAtt;
        descriptionTerrainAttaquant = terrain1;
        descriptionTerrainDefenseur = terrain2;
    }
}


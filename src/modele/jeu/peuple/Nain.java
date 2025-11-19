package modele.jeu.peuple;

import modele.plateau.Plateau;

public class Nain extends Unites {
    public Nain(Plateau _plateau, int _nbUnit) {
        super(_plateau, _nbUnit);
    }

    @Override
    public int getPorteeAttaque() {
        return 1; // Attaque au corps à corps
    }

    @Override
    public int getPorteeDeplacement() {
        return 1; // Déplacement lent
    }

    @Override
    public int getForceAttaque() {
        return 4; // Force élevée
    }

    @Override
    public int getForceDefense() {
        return 5; // Excellente défense (bonus en montagne)
    }

    @Override
    public TypePeuple getTypePeuple() {
        return TypePeuple.NAIN;
    }
}

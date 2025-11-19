package modele.jeu.peuple;

import modele.plateau.Plateau;

public class Humain extends Unites {
    public Humain(Plateau _plateau, int _nbUnit) {
        super(_plateau, _nbUnit);
    }

    @Override
    public int getPorteeAttaque() {
        return 1; // Attaque au corps à corps
    }

    @Override
    public int getPorteeDeplacement() {
        return 1; // Déplacement normal
    }

    @Override
    public int getForceAttaque() {
        return 3; // Force équilibrée
    }

    @Override
    public int getForceDefense() {
        return 3; // Défense équilibrée
    }

    @Override
    public TypePeuple getTypePeuple() {
        return TypePeuple.HUMAIN;
    }
}

package modele.jeu.peuple;

import modele.plateau.Plateau;

public class Gobelin extends Unites {
    public Gobelin(Plateau _plateau) {
        super(_plateau);
    }

    @Override
    public int getPorteeAttaque() {
        return 1; // Attaque au corps à corps
    }

    @Override
    public int getPorteeDeplacement() {
        return 2; // Déplacement rapide
    }

    @Override
    public int getForceAttaque() {
        return 2; // Force faible
    }

    @Override
    public int getForceDefense() {
        return 2; // Défense faible
    }

    @Override
    public TypePeuple getTypePeuple() {
        return TypePeuple.GOBELIN;
    }
}

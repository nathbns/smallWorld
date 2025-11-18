package modele.jeu.peuple;

import modele.plateau.Plateau;

public class Elfe extends Unites {
    public Elfe(Plateau _plateau) {
        super(_plateau);
    }

    @Override
    public int getPorteeAttaque() {
        return 2; // Les elfes peuvent attaquer à distance
    }

    @Override
    public int getPorteeDeplacement() {
        return 2; // Les elfes se déplacent plus loin
    }

    @Override
    public int getForceAttaque() {
        return 3; // Force moyenne
    }

    @Override
    public int getForceDefense() {
        return 2; // Défense faible
    }

    @Override
    public TypePeuple getTypePeuple() {
        return TypePeuple.ELFE;
    }
}

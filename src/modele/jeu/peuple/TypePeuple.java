package modele.jeu.peuple;

import modele.plateau.Biome;

import java.util.Random;


public enum TypePeuple {
    HUMAIN("Humain", Biome.PLAINE, 8),
    ELFE("Elfe", Biome.FORET, 8),
    NAIN("Nain", Biome.MONTAGNE, 8),
    GOBELIN("Gobelin", Biome.DESERT, 8);

    private String nom;
    private Biome terrainFavori;
    private int nombreUnitesInitial;

    TypePeuple(String nom, Biome terrainFavori, int nombreUnitesInitial) {
        this.nom = nom;
        this.terrainFavori = terrainFavori;
        this.nombreUnitesInitial = nombreUnitesInitial;
    }

    public TypePeuple randomPeuple(){
        int pick = new Random().nextInt(TypePeuple.values().length);
        return TypePeuple.values()[pick];
    }

    public String getNom() {
        return nom;
    }

    public Biome getTerrainFavori() {
        return terrainFavori;
    }

    public int getNombreUnitesInitial() {
        return nombreUnitesInitial;
    }

    @Override
    public String toString() {
        return nom;
    }
}


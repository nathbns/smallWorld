package modele.jeu.peuple;

import modele.plateau.Biome;

import java.util.Random;


public enum TypePeuple {
    HUMAIN("Humain", Biome.PLAINE, Biome.MONTAGNE, 8),
    ELFE("Elfe", Biome.FORET, Biome.DESERT, 8),
    NAIN("Nain", Biome.MONTAGNE, Biome.FORET, 8),
    GOBELIN("Gobelin", Biome.DESERT, Biome.PLAINE, 8);

    private String nom;
    private Biome terrainFavori;
    private Biome terrainDeteste;
    private int nombreUnitesInitial;

    TypePeuple(String nom, Biome terrainFavori, Biome terrainDeteste , int nombreUnitesInitial) {
        this.nom = nom;
        this.terrainFavori = terrainFavori;
        this.terrainDeteste = terrainDeteste;
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

    public Biome getTerrainDeteste() {
        return terrainDeteste;
    }

    public int getNombreUnitesInitial() {
        return nombreUnitesInitial;
    }

    @Override
    public String toString() {
        return nom;
    }
}


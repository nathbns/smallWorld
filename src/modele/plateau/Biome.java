package modele.plateau;

import java.util.Random;

public enum Biome {

    PLAINE,
    FORET,
    MONTAGNE,
    DESERT;

    public Biome randomBiome(){
        int pick = new Random().nextInt(Biome.values().length);
        return Biome.values()[pick];
    }

}

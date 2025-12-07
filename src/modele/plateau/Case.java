/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele.plateau;

import modele.jeu.peuple.Unites;

public class Case {

    protected Biome biome;
    protected Unites u;
    protected Plateau plateau;
    private int id;



    public void quitterLaCase() {
        u = null;
    }



    public Case(Plateau _plateau, int _id) {

        plateau = _plateau;
        id = _id;
    }

    public Case(Plateau _plateau, Biome b, int _id) {

        plateau = _plateau;
        biome = b;
        id = _id;
    }

    public void setBiome(Biome b){
        biome = b;
    }

    public Biome getBiome(){
        return biome;
    }

    public Unites getUnites() {
        return u;
    }

    public int getId() {
        return id;
    }

    public boolean getTerrain() {
        if (biome == null) {
            return false;
        } else {
            return true;
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele.plateau;

import modele.jeu.Unites;

public class Case {

    // TODO : ajouter le biome de la case
    protected Unites u;
    protected Plateau plateau;



    public void quitterLaCase() {
        u = null;
    }



    public Case(Plateau _plateau) {

        plateau = _plateau;
    }

    public Unites getUnites() {
        return u;
    }


   }

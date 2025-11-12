/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele.plateau;


import modele.jeu.Joueur;
import modele.jeu.peuple.*;

import java.awt.Point;
import java.util.HashMap;
import java.util.Observable;
import java.util.Random;


public class Plateau extends Observable {

    public static final int SIZE_X = 8;
    public static final int SIZE_Y = 8;


    private HashMap<Case, Point> map = new  HashMap<Case, Point>(); // permet de récupérer la position d'une case à partir de sa référence
    private Case[][] grilleCases = new Case[SIZE_X][SIZE_Y]; // permet de récupérer une case à partir de ses coordonnées

    public Plateau() {
        initPlateauVide();
    }

    public Case[][] getCases() {
        return grilleCases;
    }


    public Biome randomBiome(){
        int pick = new Random().nextInt(Biome.values().length);
        return Biome.values()[pick];
    }

    private void initPlateauVide() {

        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                grilleCases[x][y] = new Case(this,randomBiome());
                map.put(grilleCases[x][y], new Point(x, y));
            }

        }

    }

    public void initialiser() {


        Elfe c = new Elfe(this);
        c.allerSurCase(grilleCases[4][7]);

        setChanged();
        notifyObservers();

    }

    public void addJoueur(Joueur [] j) {

        for (int jo = 0; jo < 4; jo++){ // Boucle sur les joueurs

            if(j[jo] == null){ // Si moins de 4 joueurs
                continue;
            }

            // Choix du coin a utiliser
            int x = 0;
            int y = 0;
            switch (jo){
                case 0:
                    x = 0;
                    y = 0;
                    break;
                case 1:
                    x = SIZE_X - 1;
                    y = SIZE_Y - 1;
                    break;
                case 2:
                    x = SIZE_X - 1;
                    y = 0;
                    break;
                case 3:
                    x = 0;
                    y = SIZE_Y - 1;
            }

            for(int i = 0; i < j[jo].getUnites().toArray().length; i++){
                int [] pos;
                switch (j[jo].getPeuple()){
                    case HUMAIN:
                        Humain h = new Humain(this);
                        pos = findEmptyCaseAround(x,y);
                        h.allerSurCase(grilleCases[pos[0]][pos[1]]);
                        break;
                    case GOBELIN:
                        Gobelin g = new Gobelin(this);
                        pos = findEmptyCaseAround(x,y);
                        g.allerSurCase(grilleCases[pos[0]][pos[1]]);
                        break;
                    case NAIN:
                        Nain n = new Nain(this);
                        pos = findEmptyCaseAround(x,y);
                        n.allerSurCase(grilleCases[pos[0]][pos[1]]);
                        break;
                    case ELFE:
                        Elfe e = new Elfe(this);
                        pos = findEmptyCaseAround(x,y);
                        e.allerSurCase(grilleCases[pos[0]][pos[1]]);
                        break;
                }
            }
        }


        setChanged();
        notifyObservers();

    }

    private int [] findEmptyCaseAround(int x, int y){
        int xx = x; int yy = y;
        while(!(xx >= 0 && xx < SIZE_X && yy >= 0 && yy < SIZE_X && grilleCases[xx][yy].getUnites() == null)){
            xx = x; yy = y;
            xx = xx - 2 + new Random().nextInt(5);
            yy = yy - 2 + new Random().nextInt(5);
        }
        return new int[]{xx,yy};
    }

    public void arriverCase(Case c, Unites u) {

        c.u = u;

    }

    public void deplacerUnite(Case c1, Case c2) {
        if (c1.u != null) {

            c1.u.allerSurCase(c2);

        }



        setChanged();
        notifyObservers();

    }


    /** Indique si p est contenu dans la grille
     */
    private boolean contenuDansGrille(Point p) {
        return p.x >= 0 && p.x < SIZE_X && p.y >= 0 && p.y < SIZE_Y;
    }
    
    private Case caseALaPosition(Point p) {
        Case retour = null;
        
        if (contenuDansGrille(p)) {
            retour = grilleCases[p.x][p.y];
        }
        return retour;
    }


}

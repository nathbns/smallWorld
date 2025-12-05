/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele.plateau;

import modele.jeu.Joueur;
import modele.jeu.peuple.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Random;


public class Plateau extends Observable {

    public static int SIZE_X = 8;
    public static int SIZE_Y = 8;


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

//        c.allerSurCase(grilleCases[4][7]); // Unite pour exemple
//        Elfe c = new Elfe(this);

        setChanged();
        notifyObservers();

    }

    public void addJoueur(Joueur [] j) {

        for (int jo = 0; jo < j.length; jo++){ // Boucle sur les joueurs

            if(j[jo] == null){ // Si moins de joueurs
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
                        Humain h = (Humain) j[jo].getUnite(i);
                        h.setProprietaire(j[jo]);
                        pos = findEmptyCaseAround(x,y);
                        h.allerSurCase(grilleCases[pos[0]][pos[1]]);
                        break;
                    case GOBELIN:
                        Gobelin g = (Gobelin) j[jo].getUnite(i);
                        g.setProprietaire(j[jo]);
                        pos = findEmptyCaseAround(x,y);
                        g.allerSurCase(grilleCases[pos[0]][pos[1]]);
                        break;
                    case NAIN:
                        Nain n = (Nain) j[jo].getUnite(i);
                        n.setProprietaire(j[jo]);
                        pos = findEmptyCaseAround(x,y);
                        n.allerSurCase(grilleCases[pos[0]][pos[1]]);
                        break;
                    case ELFE:
                        Elfe e = (Elfe) j[jo].getUnite(i);
                        e.setProprietaire(j[jo]);
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

    /**
     * Calcule la distance réelle entre deux points.
     * Les déplacements en diagonale coûtent plus cher racine carre de 2
     * Un déplacement en ligne droite coûte 1, en diagonale ça coute plus
     */
    private double calculerDistance(int dx, int dy) {
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        
        // Distance avec diagonales plus coûteuses
        // Chaque pas diagonal compte comme √2, chaque pas droit compte comme 1
        int diagonales = Math.min(absDx, absDy);
        int lignesDroites = Math.abs(absDx - absDy);
        
        return diagonales * Math.sqrt(2) + lignesDroites;
    }

    /**
     * Calcule les cases accessibles pour un déplacement depuis une case donnée
     */
    public List<Case> getCasesAccessibles(Case caseDepart, Joueur joueur) {
        List<Case> casesAccessibles = new ArrayList<>();
        
        if (caseDepart == null || caseDepart.getUnites() == null) {
            return casesAccessibles;
        }
        
        Unites unite = caseDepart.getUnites();
        
        // Vérifier que l'unité appartient au joueur
        if (unite.getProprietaire() != joueur) {
            return casesAccessibles;
        }
        
        // Si l'unité a déjà déplacé ou attaqué, elle ne peut plus bouger
        if (unite.aDeplaceOuAttaque()) {
            return casesAccessibles;
        }
        
        Point posDepart = map.get(caseDepart);
        int portee = unite.getPorteeDeplacement();
        
        // Parcourir toutes les cases potentiellement dans la portée de déplacement
        // On élargit la zone de recherche pour couvrir les diagonales possibles
        for (int dx = -portee; dx <= portee; dx++) {
            for (int dy = -portee; dy <= portee; dy++) {
                if (dx == 0 && dy == 0) continue; // Pas la case de départ
                
                // Vérifier que la distance réelle est dans la portée
                double distance = calculerDistance(dx, dy);
                if (distance > portee) continue; // Trop loin en diagonale
                
                int newX = posDepart.x + dx;
                int newY = posDepart.y + dy;
                
                if (contenuDansGrille(new Point(newX, newY))) {
                    Case c = grilleCases[newX][newY];
                    
                    // Une case est accessible si elle est vide ou contient une unité ennemie
                    if (c.getUnites() == null || c.getUnites().getProprietaire() != joueur) {
                        casesAccessibles.add(c);
                    }
                }
            }
        }
        
        return casesAccessibles;
    }

    /**
     * Calcule les cases attaquables depuis une case donnée
    */
    public List<Case> getCasesAttaquables(Case caseDepart, Joueur joueur) {
        List<Case> casesAttaquables = new ArrayList<>();
        
        if (caseDepart == null || caseDepart.getUnites() == null) {
            return casesAttaquables;
        }
        
        Unites unite = caseDepart.getUnites();
        
        // Vérifier que l'unité appartient au joueur
        if (unite.getProprietaire() != joueur) {
            return casesAttaquables;
        }
        
        // Si l'unité a déjà joué ce tour, elle ne peut plus attaquer
        if (unite.aJoueCeTour()) {
            return casesAttaquables;
        }
        
        Point posDepart = map.get(caseDepart);
        int portee = unite.getPorteeAttaque();
        
        // Parcourir toutes les cases potentiellement dans la portée d'attaque
        for (int dx = -portee; dx <= portee; dx++) {
            for (int dy = -portee; dy <= portee; dy++) {
                if (dx == 0 && dy == 0) continue; // Pas la case de départ
                
                // Vérifier que la distance réelle est dans la portée
                // Les diagonales coûtent plus cher (√2 ≈ 1.41)
                double distance = calculerDistance(dx, dy);
                if (distance > portee) continue; // Trop loin en diagonale
                
                int newX = posDepart.x + dx;
                int newY = posDepart.y + dy;
                
                if (contenuDansGrille(new Point(newX, newY))) {
                    Case c = grilleCases[newX][newY];
                    
                    // Une case est attaquable si elle contient une unité ennemie
                    if (c.getUnites() != null && c.getUnites().getProprietaire() != joueur) {
                        casesAttaquables.add(c);
                    }
                }
            }
        }
        
        return casesAttaquables;
    }

    /**
     * Calcule les cases accessibles pour un allie
     */
    public List<Case> getCasesAlliees(Case caseDepart, Joueur joueur) {
        List<Case> casesAlliees = new ArrayList<>();

        if (caseDepart == null || caseDepart.getUnites() == null) {
            return casesAlliees;
        }

        Unites unite = caseDepart.getUnites();

        // Vérifier que l'unité appartient au joueur
        if (unite.getProprietaire() != joueur) {
            return casesAlliees;
        }

        // Si l'unité a déjà déplacé ou attaqué, elle ne peut plus bouger
        if (unite.aDeplaceOuAttaque()) {
            return casesAlliees;
        }

        Point posDepart = map.get(caseDepart);
        int portee = unite.getPorteeDeplacement();

        // Parcourir toutes les cases dans la portée de déplacement
        for (int dx = -portee; dx <= portee; dx++) {
            for (int dy = -portee; dy <= portee; dy++) {
                if (dx == 0 && dy == 0) continue; // Pas la case de départ

                int newX = posDepart.x + dx;
                int newY = posDepart.y + dy;

                if (contenuDansGrille(new Point(newX, newY))) {
                    Case c = grilleCases[newX][newY];

                    // Une case est accessible pour l'allie si elle ou contient une unité alliee
                    if (c.getUnites() != null && c.getUnites().getProprietaire() == joueur) {
                        casesAlliees.add(c);
                    }
                }
            }
        }

        return casesAlliees;
    }

    /**
     * Effectue une attaque entre deux cases
     * @return ResultatCombat avec les détails du combat
     */
    /*
    public modele.jeu.ResultatCombat attaquer2(Case caseAttaquant, Case caseDefenseur) {
        if (caseAttaquant == null || caseDefenseur == null) {
            return null;
        }
        
        Unites attaquant = caseAttaquant.getUnites();
        Unites defenseur = caseDefenseur.getUnites();
        
        if (attaquant == null || defenseur == null) {
            return null;
        }
        
        // Calcul aléatoire du combat
        Random rand = new Random();
        int diceAtt = rand.nextInt(6); // +0 à +5
        int diceDef = rand.nextInt(6); // +0 à +5
        int forceAtt = attaquant.getForceAttaque() + diceAtt;
        int forceDef = defenseur.getForceDefense() + diceDef;
        
        // Bonus de terrain pour le défenseur
        String descriptionTerrain = "";
        Biome biomeDefenseur = caseDefenseur.getBiome();
        if (biomeDefenseur == Biome.MONTAGNE && defenseur.getTypePeuple() == TypePeuple.NAIN) {
            forceDef += 2;
            descriptionTerrain = " (Bonus Montagne +2)";
        } else if (biomeDefenseur == Biome.FORET && defenseur.getTypePeuple() == TypePeuple.ELFE) {
            forceDef += 2;
            descriptionTerrain = " (Bonus Forêt +2)";
        }
        
        // L'attaquant gagne si sa force est supérieure
        boolean attaquantGagne = forceAtt > forceDef;
        
        // Créer le résultat avant de modifier le plateau
        modele.jeu.ResultatCombat resultat = new modele.jeu.ResultatCombat(
            attaquant, defenseur, forceAtt, forceDef, attaquantGagne, descriptionTerrain
        );
        
        if (attaquantGagne) {
            // Le défenseur est éliminé
            Joueur joueurDefenseur = defenseur.getProprietaire();
            defenseur.quitterCase(); // L'unité défenseur quitte sa case
            if (joueurDefenseur != null) {
                joueurDefenseur.retirerUnite(defenseur); // Retirer de la liste du joueur
            }
            // L'attaquant prend sa place
            attaquant.allerSurCase(caseDefenseur);
        }
        
        // Marquer l'unité comme ayant joué
        attaquant.marquerCommeJouee();
        
        setChanged();
        notifyObservers();
        
        return resultat;
    }
    */


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

    public Point getPosition(Case c) {
        return map.get(c);
    }


    public modele.jeu.ResultatCombat attaquer(Case caseAttaquant, Case caseDefenseur) {
        if (caseAttaquant == null || caseDefenseur == null) {
            return null;
        }

        Unites attaquant = caseAttaquant.getUnites();
        Unites defenseur = caseDefenseur.getUnites();

        if (attaquant == null || defenseur == null) {
            return null;
        }

        // Calcul aléatoire du combat
        int forceAtt = attaquant.calculAttaqueTotale();
        int forceDef = defenseur.calculDefenseTotale();

        // Bonus de terrain pour le défenseur
        String descriptionTerrain2 = "";
        Biome biomeDefenseur = caseDefenseur.getBiome();
        if (biomeDefenseur == defenseur.getTypePeuple().getTerrainFavori()) {
            descriptionTerrain2 = " (Bonus Terrain +50%)";
        } else if (biomeDefenseur == defenseur.getTypePeuple().getTerrainDeteste()) {
            descriptionTerrain2 = " (Bonus Terrain -33%)";
        }

        // Bonus de terrain pour l'attaquant
        String descriptionTerrain1 = "";
        Biome biomeAttaquant = caseAttaquant.getBiome();
        if (biomeAttaquant == attaquant.getTypePeuple().getTerrainFavori()) {
            descriptionTerrain1 = " (Bonus Terrain +50%)";
        } else if (biomeAttaquant == attaquant.getTypePeuple().getTerrainDeteste()) {
            descriptionTerrain1 = " (Bonus Terrain -33%)";
        }


        Random rand = new Random();

        // Calcul des probabilites
        int forceTotal = forceAtt + forceDef;
        double probaGagne = (float) forceAtt / forceTotal; // Probabilite que l'attaquand gagne en pourcentage/100
        double probaRand = rand.nextDouble(); // Nombre aleatoire qui determine le resultat
        System.out.println("Proba choisie aléatoirement : " + probaRand);
        boolean attaquantGagne = probaGagne > probaRand; // Resultat aléatoire

        // Créer le résultat avant de modifier le plateau
        modele.jeu.ResultatCombat resultat = new modele.jeu.ResultatCombat(
                attaquant, defenseur, forceAtt, forceDef, attaquantGagne, descriptionTerrain1, descriptionTerrain2
        );

        if (attaquantGagne) {
            // Le défenseur perd une unité
            if(defenseur.getNbUnit() > 1){
                defenseur.setNbUnit(defenseur.getNbUnit()-1);
            }else{ // Le defenseur n'a plus qu'une unité
                Joueur joueurDefenseur = defenseur.getProprietaire();
                defenseur.quitterCase(); // L'unité défenseur quitte sa case
                if (joueurDefenseur != null) {
                    joueurDefenseur.retirerUnite(defenseur); // Retirer de la liste du joueur
                }
                // L'attaquant prend sa place
                attaquant.allerSurCase(caseDefenseur);
            }
        }else{
            // L'attaquant perd une unité
            if(attaquant.getNbUnit() > 1){
                attaquant.setNbUnit(attaquant.getNbUnit()-1);
            }else{  // L'attaquant n'a plus qu'une unité
                Joueur joueurAttaquant = attaquant.getProprietaire();
                attaquant.quitterCase(); // L'unité attaquante quitte sa case
                if (joueurAttaquant != null) {
                    joueurAttaquant.retirerUnite(attaquant); // Retirer de la liste du joueur
                }
            }
        }

        // Marquer l'unité comme ayant joué
        attaquant.marquerCommeJouee();

        setChanged();
        notifyObservers();

        return resultat;
    }


}

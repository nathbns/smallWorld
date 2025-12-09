package modele.plateau;

import modele.jeu.Joueur;
import modele.jeu.peuple.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.Set;


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
                grilleCases[x][y] = new Case(this,randomBiome(),y + x*SIZE_X);
                map.put(grilleCases[x][y], new Point(x, y));
            }

        }
    }

    public void initialiser() {
        setChanged();
        notifyObservers();
    }

    public void setUniteSurCase(Unites unit, int x, int y){
        unit.allerSurCase(grilleCases[x][y]);
    }

    public void addJoueur(Joueur [] j) {

        for (int jo = 0; jo < j.length; jo++){ 

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

    public void changeBiomeFromPos(String st, int x, int y){
        switch (st){
            case "FORET" -> grilleCases[x][y].setBiome(Biome.FORET);
            case "DESERT" -> grilleCases[x][y].setBiome(Biome.DESERT);
            case "PLAINE" -> grilleCases[x][y].setBiome(Biome.PLAINE);
            case "MONTAGNE" -> grilleCases[x][y].setBiome(Biome.MONTAGNE);
            default -> throw new IllegalStateException("Unexpected value");
        }
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

    // forcer un notif pour le snapshot
    public void refresh(){
        setChanged();
        notifyObservers();
    }

    // Calcule les cases accessibles pour un déplacement depuis une case donnée
    // Règle : déplacement de N cases sur les 4 cases adjacentes, pas de diagonale
    // Ne peut pas passer par une case avec une unité du même type
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
        
        // Si l'unité a déjà joué ce tour, elle ne peut plus bouger
        if (unite.aJoueCeTour()) {
            return casesAccessibles;
        }
        
        // Si l'unité a attaqué sans se déplacer, elle ne peut plus bouger
        if (unite.aAttaqueSansDeplacement()) {
            return casesAccessibles;
        }
        
        Point posDepart = map.get(caseDepart);
        int mouvement = unite.getPorteeDeplacement(); // Nombre de cases de mouvement
        
        // Set pour éviter les doublons
        Set<Point> casesVisitees = new HashSet<>();
        
        // Exploration récursive depuis la case de départ
        explorerCasesRecursif(posDepart, mouvement, joueur, unite.getTypePeuple(), 
                              casesAccessibles, casesVisitees);
        
        return casesAccessibles;
    }
    
    // Methode récursive qui explore les cases accessibles
    private void explorerCasesRecursif(Point position, int mouvementRestant, Joueur joueur, TypePeuple typePeuple, List<Case> resultat, Set<Point> visites) {
        // Si plus de mouvement disponible, on s'arrête
        if (mouvementRestant < 0) {
            return;
        }
        
        // Éviter les cycles : si on a déjà visité cette position, on s'arrête
        if (visites.contains(position)) {
            return;
        }
        
        // Vérifier que la position est dans la grille
        if (!contenuDansGrille(position)) {
            return;
        }
        
        Case caseActuelle = grilleCases[position.x][position.y];
        
        // Si la case contient une unité du même type (alliée) et ce n'est pas la case de départ, on ne peut pas passer
        if (!visites.isEmpty() && caseActuelle.getUnites() != null && 
            caseActuelle.getUnites().getTypePeuple() == typePeuple) {
            return;
        }
        
        // Ajouter cette case aux résultats si elle est accessible (vide ou ennemie)
        if (caseActuelle.getUnites() == null || 
            caseActuelle.getUnites().getProprietaire() != joueur) {
            if (!resultat.contains(caseActuelle)) {
                resultat.add(caseActuelle);
            }
        }
        
        // Si plus de mouvement, on ne continue pas l'exploration
        if (mouvementRestant == 0) {
            return;
        }
        
        // Marquer comme visitée
        visites.add(new Point(position.x, position.y));
        
        // Seulement les 4 directions adjacentes (pas de diagonale)
        int[][] directions = {
            {0, 1},   // Droite
            {1, 0},   // Bas
            {0, -1},  // Gauche
            {-1, 0}   // Haut
        };
        
        // Explorer récursivement les 4 cases adjacentes
        for (int[] dir : directions) {
            int newX = position.x + dir[0];
            int newY = position.y + dir[1];
            Point nouvellePosition = new Point(newX, newY);
            
            // Continuer l'exploration avec un mouvement de moins
            explorerCasesRecursif(nouvellePosition, mouvementRestant - 1, joueur, 
                                 typePeuple, resultat, visites);
        }
        
        // Retirer de la visite pour permettre d'autres chemins vers cette case
        visites.remove(position);
    }

    // Calcule les cases attaquables depuis une case donnée
    // Règle : attaque seulement les unités adjacentes (1 case, pas de diagonale)
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
        
        // Seulement les 4 cases adjacentes (pas de diagonale)
        int[][] directions = {
            {0, 1},   // Droite
            {1, 0},   // Bas
            {0, -1},  // Gauche
            {-1, 0}   // Haut
        };
        
        // Parcourir les 4 cases adjacentes
        for (int[] dir : directions) {
            int newX = posDepart.x + dir[0];
            int newY = posDepart.y + dir[1];
            
            if (contenuDansGrille(new Point(newX, newY))) {
                Case c = grilleCases[newX][newY];
                
                // Une case est attaquable si elle contient une unité ennemie
                if (c.getUnites() != null && c.getUnites().getProprietaire() != joueur) {
                    casesAttaquables.add(c);
                }
            }
        }
        
        return casesAttaquables;
    }

    // Calcule les cases accessibles pour un allié (superposition)
    // Règle : déplacement de N cases sur les 4 cases adjacentes, vers une unité alliée
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

        // Si l'unité a déjà joué ce tour, elle ne peut plus bouger
        if (unite.aJoueCeTour()) {
            return casesAlliees;
        }
        
        // Si l'unité a attaqué sans se déplacer, elle ne peut plus bouger
        if (unite.aAttaqueSansDeplacement()) {
            return casesAlliees;
        }

        Point posDepart = map.get(caseDepart);
        int mouvement = unite.getPorteeDeplacement(); // Nombre de cases de mouvement
        
        // Set pour éviter les doublons
        Set<Point> casesVisitees = new HashSet<>();
        
        // Exploration récursive depuis la case de départ (cherche les unités alliées)
        explorerCasesAllieesRecursif(posDepart, mouvement, joueur, unite.getTypePeuple(), 
                                     casesAlliees, casesVisitees);
        
        return casesAlliees;
    }
    
    // Méthode récursive qui explore les cases avec des unités alliées
    private void explorerCasesAllieesRecursif(Point position, int mouvementRestant, Joueur joueur, 
                                              TypePeuple typePeuple, List<Case> resultat, Set<Point> visites) {
        // Si plus de mouvement disponible, on s'arrête
        if (mouvementRestant < 0) {
            return;
        }
        
        // Éviter les cycles : si on a déjà visité cette position, on s'arrête
        if (visites.contains(position)) {
            return;
        }
        
        // Vérifier que la position est dans la grille
        if (!contenuDansGrille(position)) {
            return;
        }
        
        Case caseActuelle = grilleCases[position.x][position.y];
        
        // Si la case contient une unité alliée (même joueur et même type), on l'ajoute
        if (!visites.isEmpty() && caseActuelle.getUnites() != null && 
            caseActuelle.getUnites().getProprietaire() == joueur &&
            caseActuelle.getUnites().getTypePeuple() == typePeuple) {
            if (!resultat.contains(caseActuelle)) {
                resultat.add(caseActuelle);
            }
        }
        
        // Si la case contient une unité ennemie ou d'un autre type, on ne peut pas passer
        if (!visites.isEmpty() && caseActuelle.getUnites() != null && 
            (caseActuelle.getUnites().getProprietaire() != joueur ||
             caseActuelle.getUnites().getTypePeuple() != typePeuple)) {
            return;
        }
        
        // Si plus de mouvement, on ne continue pas l'exploration
        if (mouvementRestant == 0) {
            return;
        }
        
        // Marquer comme visitée
        visites.add(new Point(position.x, position.y));
        
        // Seulement les 4 directions adjacentes (pas de diagonale)
        int[][] directions = {
            {0, 1},   // Droite
            {1, 0},   // Bas
            {0, -1},  // Gauche
            {-1, 0}   // Haut
        };
        
        // Explorer récursivement les 4 cases adjacentes
        for (int[] dir : directions) {
            int newX = position.x + dir[0];
            int newY = position.y + dir[1];
            Point nouvellePosition = new Point(newX, newY);
            
            // Continuer l'exploration avec un mouvement de moins
            explorerCasesAllieesRecursif(nouvellePosition, mouvementRestant - 1, joueur, 
                                        typePeuple, resultat, visites);
        }
        
        // Retirer de la visite pour permettre d'autres chemins vers cette case
        visites.remove(position);
    }

    // Indique si p est contenu dans la grille
    private boolean contenuDansGrille(Point p) {
        return p.x >= 0 && p.x < SIZE_X && p.y >= 0 && p.y < SIZE_Y;
    }

    public Point getPosition(Case c) {
        return map.get(c);
    }

    public Case getPosition(int x, int y) {
        return grilleCases[x][y];
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

        setChanged();
        notifyObservers();

        return resultat;
    }


}

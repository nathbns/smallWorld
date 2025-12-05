package modele.ia;

import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.jeu.Joueur;
import modele.jeu.peuple.TypePeuple;
import modele.jeu.peuple.Unites;
import modele.plateau.Biome;
import modele.plateau.Case;
import modele.plateau.Plateau;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Recherche Arbre Monte Carlo complète (sélection / expansion / simulation / rétropropagation)
 * Conçue pour choisir un seul coup pour le joueur
 */
public class RechercheArbreMonteCarlo {

    private static final int NB_ITERATIONS_MAX = 350;
    private static final int PROFONDEUR_ROLLOUT_MAX = 6;
    private static final double EXPLORATION = Math.sqrt(2);
    private static final Map<TypePeuple, Statistiques> STATS = new EnumMap<>(TypePeuple.class);

    static {
        STATS.put(TypePeuple.ELFE, new Statistiques(2, 2, 3, 2, Biome.FORET, Biome.DESERT));
        STATS.put(TypePeuple.HUMAIN, new Statistiques(1, 1, 3, 3, Biome.PLAINE, Biome.MONTAGNE));
        STATS.put(TypePeuple.NAIN, new Statistiques(1, 1, 4, 5, Biome.MONTAGNE, Biome.FORET));
        STATS.put(TypePeuple.GOBELIN, new Statistiques(1, 2, 2, 2, Biome.DESERT, Biome.PLAINE));
    }

    public Coup choisirMeilleurCoup(Jeu jeu){
        if(jeu == null || jeu.hasEnded()){
            return null;
        }
        EtatMC etatRacine = EtatMC.depuisJeu(jeu);
        List<ActionMC> actionsRacine = etatRacine.listerActions();
        if(actionsRacine.isEmpty()){
            return null;
        }

        Random hasard = new Random();
        Noeud racine = new Noeud(null, etatRacine, null);

        for(int iter = 0; iter < NB_ITERATIONS_MAX; iter++){
            Noeud noeud = selection(racine);
            Noeud enfant = expansion(noeud, hasard);
            double recompense = simulation(enfant.etat, etatRacine.joueurRacine, hasard);
            retropropager(enfant, recompense);
        }

        Noeud meilleur = null;
        int visitesMax = -1;
        for(Noeud enfant : racine.enfants){
            if(enfant.visites > visitesMax){
                visitesMax = enfant.visites;
                meilleur = enfant;
            }
        }
        if(meilleur == null || meilleur.actionDepuisParent == null){
            return null;
        }

        Case[][] cases = jeu.getPlateau().getCases();
        ActionMC a = meilleur.actionDepuisParent;
        return new Coup(cases[a.depX][a.depY], cases[a.arrX][a.arrY]);
    }

    // Sélection
    private Noeud selection(Noeud noeud){
        while(noeud.estEntierementDeveloppe() && !noeud.estTerminal()){
            noeud = meilleurEnfantUcb(noeud);
        }
        return noeud;
    }

    // Expansion
    private Noeud expansion(Noeud noeud, Random hasard){
        if(noeud.estTerminal()){
            return noeud;
        }
        if(noeud.actionsNonExplorees.isEmpty()){
            return noeud;
        }
        ActionMC action = noeud.actionsNonExplorees.remove(hasard.nextInt(noeud.actionsNonExplorees.size()));
        EtatMC etatSuivant = noeud.etat.appliquer(action, hasard);
        Noeud enfant = new Noeud(noeud, etatSuivant, action);
        noeud.enfants.add(enfant);
        return enfant;
    }

    // Simulation
    private double simulation(EtatMC etat, int joueurRacine, Random hasard){
        EtatMC sim = etat;
        for(int profondeur = 0; profondeur < PROFONDEUR_ROLLOUT_MAX && !sim.estTerminal(); profondeur++){
            List<ActionMC> actions = sim.listerActions();
            if(actions.isEmpty()){
                sim = sim.passerTour();
                continue;
            }
            ActionMC action = actions.get(hasard.nextInt(actions.size()));
            sim = sim.appliquer(action, hasard);
        }
        return sim.evaluer(joueurRacine);
    }

    // Rétropropagation
    private void retropropager(Noeud noeud, double valeur){
        Noeud courant = noeud;
        while(courant != null){
            courant.visites += 1;
            courant.valeurTotale += valeur;
            courant = courant.parent;
        }
    }

    private Noeud meilleurEnfantUcb(Noeud noeud){
        Noeud meilleur = null;
        double meilleurScore = Double.NEGATIVE_INFINITY;
        double logParent = Math.log(Math.max(1, noeud.visites));
        for(Noeud enfant : noeud.enfants){
            if(enfant.visites == 0){
                return enfant; // priorité exploration
            }
            double moyenne = enfant.valeurTotale / enfant.visites;
            double ucb = moyenne + EXPLORATION * Math.sqrt(logParent / enfant.visites);
            if(ucb > meilleurScore){
                meilleurScore = ucb;
                meilleur = enfant;
            }
        }
        return meilleur != null ? meilleur : noeud.enfants.get(0);
    }

    // structures
    private static class Noeud{
        final Noeud parent;
        final EtatMC etat;
        final ActionMC actionDepuisParent;
        final List<Noeud> enfants = new ArrayList<>();
        final List<ActionMC> actionsNonExplorees;
        int visites = 0;
        double valeurTotale = 0;

        Noeud(Noeud parent, EtatMC etat, ActionMC actionDepuisParent){
            this.parent = parent;
            this.etat = etat;
            this.actionDepuisParent = actionDepuisParent;
            this.actionsNonExplorees = etat.listerActions();
        }

        boolean estTerminal(){
            return etat.estTerminal();
        }

        boolean estEntierementDeveloppe(){
            return actionsNonExplorees.isEmpty() ? (!enfants.isEmpty() || etat.estTerminal()) : false;
        }
    }

    private static class ActionMC{
        final int depX, depY, arrX, arrY;
        final TypeAction type;

        ActionMC(int depX, int depY, int arrX, int arrY, TypeAction type){
            this.depX = depX;
            this.depY = depY;
            this.arrX = arrX;
            this.arrY = arrY;
            this.type = type;
        }
    }

    private enum TypeAction { DEPLACEMENT, ATTAQUE, SUPERPOSITION }

    private static class UniteMC{
        final TypePeuple type;
        final int proprietaire;
        int nb;
        boolean aJoue;
        boolean aDeplaceOuAttaque;

        UniteMC(TypePeuple type, int proprietaire, int nb, boolean aJoue, boolean aDeplaceOuAttaque){
            this.type = type;
            this.proprietaire = proprietaire;
            this.nb = nb;
            this.aJoue = aJoue;
            this.aDeplaceOuAttaque = aDeplaceOuAttaque;
        }

        UniteMC copie(){
            return new UniteMC(type, proprietaire, nb, aJoue, aDeplaceOuAttaque);
        }
    }

    private static class CaseMC{
        final Biome biome;
        UniteMC unite;

        CaseMC(Biome biome){
            this.biome = biome;
        }

        CaseMC copie(){
            CaseMC c = new CaseMC(biome);
            if(unite != null){
                c.unite = unite.copie();
            }
            return c;
        }
    }

    private static class EtatMC{
        final CaseMC[][] grille;
        final int largeur;
        final int hauteur;
        final int nbJoueurs;
        final int[] scores;
        final int joueurCourant;
        final int nbToursMax;
        final int tourActuel;
        final int joueurRacine;

        EtatMC(CaseMC[][] grille, int largeur, int hauteur, int nbJoueurs, int[] scores, int joueurCourant, int nbToursMax, int tourActuel, int joueurRacine){
            this.grille = grille;
            this.largeur = largeur;
            this.hauteur = hauteur;
            this.nbJoueurs = nbJoueurs;
            this.scores = scores;
            this.joueurCourant = joueurCourant;
            this.nbToursMax = nbToursMax;
            this.tourActuel = tourActuel;
            this.joueurRacine = joueurRacine;
        }

        static EtatMC depuisJeu(Jeu jeu){
            Plateau plateau = jeu.getPlateau();
            Case[][] cases = plateau.getCases();
            int width = Plateau.SIZE_X;
            int height = Plateau.SIZE_Y;
            CaseMC[][] grille = new CaseMC[width][height];

            Joueur[] joueurs = jeu.getJoueurs();

            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    Case c = cases[x][y];
                    CaseMC cell = new CaseMC(c.getBiome());
                    Unites u = c.getUnites();
                    if(u != null){
                        int ownerIdx = indexOfJoueur(joueurs, u.getProprietaire());
                        cell.unite = new UniteMC(u.getTypePeuple(), ownerIdx, u.getNbUnit(), u.aJoueCeTour(), u.aDeplaceOuAttaque());
                    }
                    grille[x][y] = cell;
                }
            }

            int[] scores = new int[joueurs.length];
            for(int i = 0; i < joueurs.length; i++){
                scores[i] = joueurs[i].getScore();
            }

            return new EtatMC(grille, width, height, joueurs.length, scores, jeu.getIndexJoueurCourant(), jeu.getNbToursMax(), jeu.getTourActuel(), jeu.getIndexJoueurCourant());
        }

        EtatMC passerTour(){
            int prochainJoueur = (joueurCourant + 1) % nbJoueurs;
            int prochainTour = tourActuel + (prochainJoueur == 0 ? 1 : 0);
            return new EtatMC(grille, largeur, hauteur, nbJoueurs, scores.clone(), prochainJoueur, nbToursMax, prochainTour, joueurRacine);
        }

        List<ActionMC> listerActions(){
            List<ActionMC> actions = new ArrayList<>();

            for(int x = 0; x < largeur; x++){
                for(int y = 0; y < hauteur; y++){
                    CaseMC cell = grille[x][y];
                    if(cell.unite == null || cell.unite.proprietaire != joueurCourant){
                        continue;
                    }
                    UniteMC unite = cell.unite;
                    Statistiques stats = STATS.get(unite.type);
                    if(stats == null){
                        continue;
                    }

                    // Déplacements et superpositions
                    if(!unite.aDeplaceOuAttaque){
                        for(int dx = -stats.porteeDep; dx <= stats.porteeDep; dx++){
                            for(int dy = -stats.porteeDep; dy <= stats.porteeDep; dy++){
                                if(dx == 0 && dy == 0){
                                    continue;
                                }
                                if(distance(dx, dy) > stats.porteeDep){
                                    continue;
                                }
                                int nx = x + dx;
                                int ny = y + dy;
                                if(!dansGrille(nx, ny)){
                                    continue;
                                }
                                CaseMC dest = grille[nx][ny];
                                if(dest.unite == null){
                                    actions.add(new ActionMC(x, y, nx, ny, TypeAction.DEPLACEMENT));
                                }else if(dest.unite.proprietaire == joueurCourant){
                                    actions.add(new ActionMC(x, y, nx, ny, TypeAction.SUPERPOSITION));
                                }
                            }
                        }
                    }

                    // Attaques
                    if(!unite.aJoue){
                        for(int dx = -stats.porteeAtt; dx <= stats.porteeAtt; dx++){
                            for(int dy = -stats.porteeAtt; dy <= stats.porteeAtt; dy++){
                                if(dx == 0 && dy == 0){
                                    continue;
                                }
                                if(distance(dx, dy) > stats.porteeAtt){
                                    continue;
                                }
                                int nx = x + dx;
                                int ny = y + dy;
                                if(!dansGrille(nx, ny)){
                                    continue;
                                }
                                CaseMC dest = grille[nx][ny];
                                if(dest.unite != null && dest.unite.proprietaire != joueurCourant){
                                    actions.add(new ActionMC(x, y, nx, ny, TypeAction.ATTAQUE));
                                }
                            }
                        }
                    }
                }
            }

            return actions;
        }

        EtatMC appliquer(ActionMC action, Random hasard){
            CaseMC[][] nouvelleGrille = new CaseMC[largeur][hauteur];
            for(int x = 0; x < largeur; x++){
                for(int y = 0; y < hauteur; y++){
                    nouvelleGrille[x][y] = grille[x][y].copie();
                }
            }

            CaseMC origine = nouvelleGrille[action.depX][action.depY];
            CaseMC arrivee = nouvelleGrille[action.arrX][action.arrY];
            if(origine.unite == null){
                return passerTour();
            }

            UniteMC unite = origine.unite;
            Statistiques stats = STATS.get(unite.type);

            switch (action.type){
                case DEPLACEMENT:
                    arrivee.unite = unite.copie();
                    arrivee.unite.aDeplaceOuAttaque = true;
                    origine.unite = null;
                    break;
                case SUPERPOSITION:
                    if(unite.nb > 1){
                        unite.nb -= 1;
                        if(arrivee.unite != null){
                            arrivee.unite.nb += 1;
                        }
                        arrivee.unite.aDeplaceOuAttaque = true;
                        arrivee.unite.aJoue = true;
                    }else{
                        if(arrivee.unite != null){
                            arrivee.unite.nb += unite.nb;
                            arrivee.unite.aDeplaceOuAttaque = true;
                            arrivee.unite.aJoue = true;
                        }
                        origine.unite = null;
                    }
                    break;
                case ATTAQUE:
                    if(arrivee.unite == null){
                        break;
                    }
                    int forceAtt = calculForce(unite, stats, origine.biome, true);
                    Statistiques statsDef = STATS.get(arrivee.unite.type);
                    int forceDef = calculForce(arrivee.unite, statsDef, arrivee.biome, false);
                    int total = Math.max(forceAtt + forceDef, 1);
                    double probaGagne = (double) forceAtt / total;
                    boolean attaquantGagne = hasard.nextDouble() < probaGagne;

                    if(attaquantGagne){
                        if(arrivee.unite.nb > 1){
                            arrivee.unite.nb -= 1;
                        }else{
                            arrivee.unite = unite.copie();
                            origine.unite = null;
                        }
                    }else{
                        if(unite.nb > 1){
                            unite.nb -= 1;
                        }else{
                            origine.unite = null;
                        }
                    }
                    if(origine.unite != null){
                        origine.unite.aJoue = true;
                    }else if(arrivee.unite != null && arrivee.unite.proprietaire == joueurCourant){
                        arrivee.unite.aJoue = true;
                    }
                    break;
            }

            int prochainJoueur = (joueurCourant + 1) % nbJoueurs;
            int prochainTour = tourActuel + (prochainJoueur == 0 ? 1 : 0);
            return new EtatMC(nouvelleGrille, largeur, hauteur, nbJoueurs, scores.clone(), prochainJoueur, nbToursMax, prochainTour, joueurRacine);
        }

        double evaluer(int joueur){
            double scoreMoi = scores[joueur];
            double scoreOppMax = 0;

            for(int x = 0; x < largeur; x++){
                for(int y = 0; y < hauteur; y++){
                    CaseMC cell = grille[x][y];
                    if(cell.unite == null){
                        continue;
                    }
                    Statistiques stats = STATS.get(cell.unite.type);
                    int ajout = 1 + Math.max(cell.unite.nb - 1, 0);
                    if(stats != null && cell.biome == stats.favori){
                        ajout += 2;
                    }
                    if(cell.unite.proprietaire == joueur){
                        scoreMoi += ajout;
                    }else{
                        scoreOppMax = Math.max(scoreOppMax, scores[cell.unite.proprietaire] + ajout);
                    }
                }
            }
            return scoreMoi - scoreOppMax * 0.8;
        }

        boolean estTerminal(){
            return tourActuel >= nbToursMax || listerActions().isEmpty();
        }

        private boolean dansGrille(int x, int y){
            return x >= 0 && x < largeur && y >= 0 && y < hauteur;
        }

        private static double distance(int dx, int dy){
            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);
            int diagonales = Math.min(absDx, absDy);
            int lignes = Math.abs(absDx - absDy);
            return diagonales * Math.sqrt(2) + lignes;
        }

        private static int calculForce(UniteMC unite, Statistiques stats, Biome biome, boolean attaque){
            if(stats == null){
                return 1;
            }
            int force = attaque ? stats.forceAtt : stats.forceDef;
            force += (unite.nb - 1) * 3;
            if(biome == stats.favori){
                force = (int) Math.round(force * 1.5);
            }else if(biome == stats.deteste){
                force = Math.max((int) Math.round(force * 0.66), 1);
            }
            return Math.max(force, 1);
        }

        private static int indexOfJoueur(Joueur[] joueurs, Joueur j){
            for(int i = 0; i < joueurs.length; i++){
                if(joueurs[i] == j){
                    return i;
                }
            }
            return -1;
        }
    }

    private static class Statistiques{
        final int porteeAtt;
        final int porteeDep;
        final int forceAtt;
        final int forceDef;
        final Biome favori;
        final Biome deteste;

        Statistiques(int porteeAtt, int porteeDep, int forceAtt, int forceDef, Biome favori, Biome deteste){
            this.porteeAtt = porteeAtt;
            this.porteeDep = porteeDep;
            this.forceAtt = forceAtt;
            this.forceDef = forceDef;
            this.favori = favori;
            this.deteste = deteste;
        }
    }
}


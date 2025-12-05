package modele.jeu;

import modele.jeu.peuple.*;
import modele.plateau.Case;
import modele.plateau.Plateau;

import java.util.List;
import java.util.Random;

public class Jeu extends Thread{
    private Plateau plateau;
    private Joueur [] joueurs;
    private int indexJoueurCourant;
    private int nbToursMax = 6; // Nombre de tours prédéfini
    private int tourActuel = 0;
    protected Coup coupRecu;
    private ResultatCombat dernierResultatCombat;
    private boolean hasEnded;

    private int nbJoueurs;
    private static final String[] COULEURS = {"Rouge", "Bleu", "Jaune", "Vert"};

    public TypePeuple randomPeuple(){
        int pick = new Random().nextInt(TypePeuple.values().length);
        return TypePeuple.values()[pick];
    }

    // Nombre de joueurs et peuples choisis
    public Jeu(int nombreJoueurs, TypePeuple[] peuplesChoisis) {
        this.nbJoueurs = nombreJoueurs;
        joueurs = new Joueur[nombreJoueurs];

        // Initialisation des joueurs avec leurs peuples choisis
        for (int i = 0; i < nombreJoueurs; i++) {
            joueurs[i] = new Joueur(this, peuplesChoisis[i], COULEURS[i]);
        }
        
        indexJoueurCourant = 0;

        // Taille du plateau selon le nombre de joueurs
        if (nbJoueurs > 2) {
            Plateau.SIZE_X = 7;
            Plateau.SIZE_Y = 7;
        } else {
            Plateau.SIZE_X = 6;
            Plateau.SIZE_Y = 6;
        }

        // Initialisation du plateau
        plateau = new Plateau();
        plateau.initialiser();

        // Initialisation des unités des joueurs
        initUnitesJoueurs(joueurs);

        // Initialisation des unités sur le plateau
        plateau.addJoueur(joueurs);

        start();
    }

    // Constructeur par défaut (partie à 4 joueurs avec peuples par défaut)
    public Jeu() {
        this(4, new TypePeuple[]{TypePeuple.ELFE, TypePeuple.NAIN, TypePeuple.HUMAIN, TypePeuple.GOBELIN});
    }

    // Faire mieux après mais au moins ça existe
    protected void initUnitesJoueurs(Joueur [] j){

        Random rand = new Random();
        int maxUnitParCase = 4; // Nombre d'unites maximum; sur une case
        int nbMaxUnitSurPlateau = 5; // Nombre d'unites maximum sur le plateau

        for(int i = 0; i < j.length; i++){
            if(j[i] == null){
                continue;
            }
            int nbUnitRestant = j[i].getPeuple().getNombreUnitesInitial(); // Nombre d'unites a placer sur le plateau
            switch (j[i].getPeuple()){
                case ELFE:
                    for(int unites = 0; unites < nbMaxUnitSurPlateau; unites++){ // Nombre d'unites arbitraire pour le moment
                        j[i].ajouterUnite(new Elfe(plateau,1));
                        nbUnitRestant--;
                    }
                    break;
                case GOBELIN:
                    for(int unites = 0; unites < nbMaxUnitSurPlateau; unites++){
                        j[i].ajouterUnite(new Gobelin(plateau,1));
                        nbUnitRestant--;
                    }
                    break;
                case HUMAIN:
                    for(int unites = 0; unites < nbMaxUnitSurPlateau; unites++){
                        j[i].ajouterUnite(new Humain(plateau,1));
                        nbUnitRestant--;
                    }
                    break;
                case NAIN:
                    for(int unites = 0; unites < nbMaxUnitSurPlateau; unites++){
                        j[i].ajouterUnite(new Nain(plateau,1));
                        nbUnitRestant--;
                    }
                    break;
            }
            while (nbUnitRestant > 0){
                int idUnit = rand.nextInt(nbMaxUnitSurPlateau);
                if(j[i].getUnite(idUnit).getNbUnit() < maxUnitParCase){
                    j[i].getUnite(idUnit).setNbUnit(j[i].getUnite(idUnit).getNbUnit()+1);
                    nbUnitRestant--;
                }
            }
        }

    }

    public Plateau getPlateau() {
        return plateau;
    }

    public Joueur getJoueurCourant() {
        return joueurs[indexJoueurCourant];
    }

    public int getTourActuel() {
        return tourActuel;
    }

    public int getNbToursMax() {
        return nbToursMax;
    }

    public boolean hasEnded(){ return hasEnded;}

    public Joueur[] getJoueurs() {
        return joueurs;
    }

    public void envoyerCoup(Coup c) {
        coupRecu = c;

        synchronized (this) {
            notify();
        }

    }


    public ResultatCombat appliquerCoup(Coup coup) {
        // Le coup peut être un déplacement ou une attaque
        Case dep = coup.dep;
        Case arr = coup.arr;
        
        if (dep == null || arr == null) {
            return null;
        }
        
        Unites unite = dep.getUnites();
        if (unite == null) {
            return null;
        }
        
        // Vérifier que c'est bien l'unité du joueur courant
        if (unite.getProprietaire() != getJoueurCourant()) {
            return null;
        }
        
        ResultatCombat resultatCombat = null;
        
        // Vérifier si c'est une attaque (case d'arrivée contient une unité ennemie)
        Unites uniteArr = arr.getUnites();
        if (uniteArr != null && uniteArr.getProprietaire() != getJoueurCourant()) {
            // C'est une attaque
            List<Case> casesAttaquables = plateau.getCasesAttaquables(dep, getJoueurCourant());
            if (casesAttaquables.contains(arr)) {
                resultatCombat = plateau.attaquer(dep, arr);
                if (resultatCombat != null && resultatCombat.attaquantGagne) {
                    // L'attaquant gagne 1 point
                    getJoueurCourant().ajouterPoints(1);
                    System.out.println(getJoueurCourant().getCouleur() + " a gagné un combat ! Points: " + getJoueurCourant().getScore());
                }
            }
        } else if (uniteArr != null && uniteArr.getProprietaire() == getJoueurCourant()) {
            // C'est une superposition
            List<Case> casesAlliees = plateau.getCasesAlliees(dep, getJoueurCourant());
            if (casesAlliees.contains(arr)) {
                if(unite.getNbUnit() > 1){ // Deplacement d'une unite vers l'autre case
                    unite.setNbUnit(unite.getNbUnit() - 1);
                    arr.getUnites().setNbUnit(arr.getUnites().getNbUnit() + 1);
                    unite.marquerCommeFinDeTour();
                }else{ // La seule unite se deplace vers l'autre unite
                    Joueur joueur = unite.getProprietaire();
                    unite.quitterCase();
                    joueur.retirerUnite(unite);
                    arr.getUnites().setNbUnit(arr.getUnites().getNbUnit() + 1);
                }
            }

        } else {
            // C'est un déplacement
            List<Case> casesAccessibles = plateau.getCasesAccessibles(dep, getJoueurCourant());
            if (casesAccessibles.contains(arr)) {
                plateau.deplacerUnite(dep, arr);
                unite.marquerDeplaceOuAttaque();
            }
        }
        
        // Sauvegarder le résultat du combat pour l'affichage
        dernierResultatCombat = resultatCombat;
        
        // NE PAS passer au joueur suivant automatiquement
        // Le joueur doit cliquer sur "Passer le tour" pour finir son tour
        
        return resultatCombat;
    }

    public ResultatCombat getDernierResultatCombat() {
        ResultatCombat resultat = dernierResultatCombat;
        dernierResultatCombat = null; // Réinitialiser après lecture
        return resultat;
    }

    public void passerAuJoueurSuivant() {
        // Réinitialiser toutes les unités du joueur courant
        for (Unites u : joueurs[indexJoueurCourant].getUnites()) {
            u.resetTour();
        }
        
        // Calculer les points pour les cases occupées
        int pointsTour = calculerPointsTour(joueurs[indexJoueurCourant]);
        joueurs[indexJoueurCourant].ajouterPoints(pointsTour);
        System.out.println(joueurs[indexJoueurCourant].getCouleur() + " gagne " + pointsTour + " points pour ce tour. Total: " + joueurs[indexJoueurCourant].getScore());
        
        indexJoueurCourant = (indexJoueurCourant + 1) % joueurs.length;
        
        // Si on revient au premier joueur, un tour complet est passé
        if (indexJoueurCourant == 0) {
            tourActuel++;
            System.out.println("--- Tour " + tourActuel + " / " + nbToursMax + " ---");
            
            // Vérifier si la partie est terminée
            if (tourActuel >= nbToursMax) {
                finPartie();
            }
        }
    }

    private int calculerPointsTour(Joueur joueur) {
        int points = 0;
        
        // Parcourir toutes les cases du plateau
        for (int x = 0; x < Plateau.SIZE_X; x++) {
            for (int y = 0; y < Plateau.SIZE_Y; y++) {
                Case c = plateau.getCases()[x][y];
                Unites u = c.getUnites();
                
                // Si la case contient au moins une unité du joueur
                if (u != null && u.getProprietaire() == joueur && u.getTypePeuple().getTerrainFavori() == u.getCase().getBiome()) {
                    points+= 2; // 1 point pour la case occupée si le terrain est favori
                }
            }
        }
        
        return points;
    }

    private void finPartie() {
        System.out.println("\n===== FIN DE LA PARTIE =====");
        
        // Trouver le gagnant
        Joueur gagnant = joueurs[0];
        for (int i = 1; i < joueurs.length; i++) {
            if (joueurs[i].getScore() > gagnant.getScore()) {
                gagnant = joueurs[i];
            }
        }
        
        // Afficher les scores
        System.out.println("\nScores finaux:");
        for (Joueur j : joueurs) {
            System.out.println(j.toString());
        }
        
        System.out.println("\nLe gagnant est : " + gagnant.toString() + " !");

        // Désactiver les actions
        hasEnded = !hasEnded;

        // Optionnel : arrêter le jeu
        // System.exit(0);
    }

    public void run() {
        jouerPartie();
    }

    public void jouerPartie() {
        System.out.println("=== Début de la partie ===");
        System.out.println("Nombre de tours: " + nbToursMax);
        
        while(tourActuel < nbToursMax) {
            Coup c = joueurs[indexJoueurCourant].getCoup();
            appliquerCoup(c);
        }

    }


}

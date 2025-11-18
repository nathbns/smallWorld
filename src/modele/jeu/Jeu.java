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
    private int nbToursMax = 10; // Nombre de tours prédéfini
    private int tourActuel = 0;
    protected Coup coupRecu;
    private ResultatCombat dernierResultatCombat;

    private static final int nbJoueurs = 4;

    public TypePeuple randomPeuple(){
        int pick = new Random().nextInt(TypePeuple.values().length);
        return TypePeuple.values()[pick];
    }

    public Jeu() {

        joueurs = new Joueur[4]; //  À changer plus tard avec nbJoueurs

        // Initialisation des joueurs avec des couleurs et peuples correspondants
        // Rouge = Elfes, Bleu = Nains, Jaune = Humains, Vert = Gobelins
        joueurs[0] = new Joueur(this, TypePeuple.ELFE, "Rouge");
        joueurs[1] = new Joueur(this, TypePeuple.NAIN, "Bleu");
        joueurs[2] = new Joueur(this, TypePeuple.HUMAIN, "Jaune");
        joueurs[3] = new Joueur(this, TypePeuple.GOBELIN, "Vert");
        
        indexJoueurCourant = 0;

        // Taille du plateau
        if(nbJoueurs > 2){
            Plateau.SIZE_X = 7; Plateau.SIZE_Y = 7;
        }else{
            Plateau.SIZE_X = 6; Plateau.SIZE_Y = 6;
        }

        // Initialisation du plateau
        plateau = new Plateau();
        plateau.initialiser();

        // Initialisation des unites des joueurs
        initUnitesJoueurs(joueurs);

        // Initialisation des unites sur le plateau
        plateau.addJoueur(joueurs);


        start();

    }

    // Faire mieux après mais au moins ça existe
    protected void initUnitesJoueurs(Joueur [] j){

        for(int i = 0; i < j.length; i++){
            if(j[i] == null){
                continue;
            }
            switch (j[i].getPeuple()){
                case ELFE:
                    for(int unites = 0; unites < 5; unites++){ // Nombre d'unites arbitraire pour le moment
                        j[i].ajouterUnite(new Elfe(plateau));
                    }
                    break;
                case GOBELIN:
                    for(int unites = 0; unites < 5; unites++){
                        j[i].ajouterUnite(new Gobelin(plateau));
                    }
                    break;
                case HUMAIN:
                    for(int unites = 0; unites < 5; unites++){
                        j[i].ajouterUnite(new Humain(plateau));
                    }
                    break;
                case NAIN:
                    for(int unites = 0; unites < 5; unites++){
                        j[i].ajouterUnite(new Nain(plateau));
                    }
                    break;
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
        
        indexJoueurCourant = (indexJoueurCourant + 1) % nbJoueurs;
        
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
        TypePeuple peuplePreference = joueur.getPeuple();
        
        // Parcourir toutes les cases du plateau
        for (int x = 0; x < Plateau.SIZE_X; x++) {
            for (int y = 0; y < Plateau.SIZE_Y; y++) {
                Case c = plateau.getCases()[x][y];
                Unites u = c.getUnites();
                
                // Si la case contient au moins une unité du joueur
                if (u != null && u.getProprietaire() == joueur) {
                    points++; // 1 point pour la case occupée
                    
                    // Bonus si le type d'unité correspond au peuple préféré
                    if (u.getTypePeuple() == peuplePreference) {
                        // Pas de bonus supplémentaire selon le PDF, juste 1 point par case
                    }
                }
            }
        }
        
        return points;
    }

    private void finPartie() {
        System.out.println("\n===== FIN DE LA PARTIE =====");
        
        // Trouver le gagnant
        Joueur gagnant = joueurs[0];
        for (int i = 1; i < nbJoueurs; i++) {
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

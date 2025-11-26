package vuecontroleur;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;


import modele.jeu.peuple.*;
import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.jeu.ResultatCombat;
import modele.plateau.Case;
import modele.plateau.Plateau;


/** Cette classe a deux fonctions :
 *  (1) Vue : proposer une représentation graphique de l'application (cases graphiques, etc.)
 *  (2) Controleur : écouter les évènements clavier et déclencher le traitement adapté sur le modèle (clic position départ -> position arrivée pièce))
 *
 */
public class VueControleur extends JFrame implements Observer {
    private Plateau plateau; // référence sur une classe de modèle : permet d'accéder aux données du modèle pour le rafraichissement, permet de communiquer les actions clavier (ou souris)
    private Jeu jeu;
    private final int sizeX; // taille de la grille affichée
    private final int sizeY;
    private static final int pxCase = 100; // nombre de pixel par case
    // icones affichées dans la grille
    private Image icoElfes;
    private Image icoHumain;
    private Image icoGobelin;
    private Image icoNain;
    private Image icoDesert;
    private Image icoPlaine;
    private Image icoForet;
    private Image icoMontagne;

    private JComponent grilleIP;
    private Case caseClic1; // mémorisation des cases cliquées
    private Case caseClic2;
    
    private List<Case> casesAccessibles;
    private List<Case> casesAttaquables;
    private List<Case> casesSuperposables;
    
    private JLabel labelJoueurCourant;
    private JLabel labelTour;
    private JButton btnPasserTour;

    private CombatPreview combatPreview;


    private ImagePanel[][] tabIP; // cases graphique (au moment du rafraichissement, chaque case va être associée à une icône background et front, suivant ce qui est présent dans le modèle)


    public VueControleur(Jeu _jeu) {
        jeu = _jeu;
        plateau = jeu.getPlateau();
        sizeX = plateau.SIZE_X;
        sizeY = plateau.SIZE_Y;

        chargerLesIcones();
        placerLesComposantsGraphiques();

        plateau.addObserver(this);

        mettreAJourAffichage();

    }


    private void chargerLesIcones() {
        //icoElfes = new ImageIcon("./data/res/cat.png").getImage();
        //icoDesert = new ImageIcon("./data/res/desert.png").getImage();

        icoElfes = new ImageIcon("./data/units/unit_red.png").getImage();
        icoNain = new ImageIcon("./data/units/unit_blue.png").getImage();
        icoHumain = new ImageIcon("./data/units/unit_yellow.png").getImage();
        icoGobelin = new ImageIcon("./data/units/unit_green.png").getImage();
        icoDesert = new ImageIcon("./data/terrain/desert.png").getImage();
        icoForet = new ImageIcon("./data/terrain/forest.png").getImage();
        icoMontagne = new ImageIcon("./data/terrain/moutain.png").getImage();
        icoPlaine = new ImageIcon("./data/terrain/plain.png").getImage();


    }



    private void placerLesComposantsGraphiques() {
        setTitle("Smallworld");
        setResizable(true);
        setLayout(new BorderLayout());
        
        // Panel du haut pour les informations
        JPanel panelInfo = new JPanel(new FlowLayout());
        labelJoueurCourant = new JLabel("Joueur: " + jeu.getJoueurCourant().getCouleur());
        labelTour = new JLabel("Tour: " + jeu.getTourActuel() + "/" + jeu.getNbToursMax());
        btnPasserTour = new JButton("Passer le tour");
        
        btnPasserTour.addActionListener(e -> {
            if(jeu.hasEnded()){
                return;
            }
            jeu.passerAuJoueurSuivant();
            caseClic1 = null;
            caseClic2 = null;
            casesAccessibles = null;
            casesAttaquables = null;
            mettreAJourAffichage();
        });
        
        panelInfo.add(labelJoueurCourant);
        panelInfo.add(labelTour);
        panelInfo.add(btnPasserTour);
        
        add(panelInfo, BorderLayout.NORTH);

        combatPreview = new CombatPreview(); // Element qui permet de determiles les probas avant le combat

        add(combatPreview.panel, BorderLayout.SOUTH);
        
        setSize(sizeX * pxCase, sizeY * pxCase + 120);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // permet de terminer l'application à la fermeture de la fenêtre

        grilleIP = new JPanel(new GridLayout(sizeY, sizeX)); // grilleJLabels va contenir les cases graphiques et les positionner sous la forme d'une grille


        tabIP = new ImagePanel[sizeX][sizeY];

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                ImagePanel iP = new ImagePanel();

                tabIP[x][y] = iP; // on conserve les cases graphiques dans tabJLabel pour avoir un accès pratique à celles-ci (voir mettreAJourAffichage() )

                final int xx = x; // permet de compiler la classe anonyme ci-dessous
                final int yy = y;
                // écouteur de clics
                iP.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Aucune action si le jeu est terminé
                        if(jeu.hasEnded()){
                            return;
                        }

                        // Logique de jeu
                        if (caseClic1 == null) {
                            // Premier clic : sélectionner une unité
                            caseClic1 = plateau.getCases()[xx][yy];
                            
                            Unites unite = caseClic1.getUnites();
                            if (unite != null && unite.getProprietaire() == jeu.getJoueurCourant()) {
                                // Afficher les cases accessibles et attaquables
                                if (!unite.aDeplaceOuAttaque()) {
                                    casesAccessibles = plateau.getCasesAccessibles(caseClic1, jeu.getJoueurCourant());
                                    casesSuperposables = plateau.getCasesAlliees(caseClic1, jeu.getJoueurCourant());
                                    combatPreview.attaqueUnite = caseClic1.getUnites().calculAttaqueTotale();
                                } else {
                                    casesAccessibles = null;
                                    casesSuperposables = null;
                                }
                                
                                if (!unite.aJoueCeTour()) {
                                    casesAttaquables = plateau.getCasesAttaquables(caseClic1, jeu.getJoueurCourant());
                                    combatPreview.attaqueUnite = caseClic1.getUnites().calculAttaqueTotale();
                                } else {
                                    casesAttaquables = null;
                                }
                                
                                mettreAJourAffichage();
                            } else {
                                // Pas d'unité ou unité adverse : réinitialiser
                                caseClic1 = null;
                                combatPreview.attaqueUnite = 0;
                            }
                        } else {
                            // Deuxième clic : déplacer ou attaquer
                            caseClic2 = plateau.getCases()[xx][yy];
                            
                            // Vérifier si on clique sur la même case (désélection)
                            if (caseClic1 == caseClic2) {
                                caseClic1 = null;
                                caseClic2 = null;
                                casesAccessibles = null;
                                casesAttaquables = null;
                                casesSuperposables = null;
                                combatPreview.attaqueUnite = 0;
                                mettreAJourAffichage();
                                return;
                            }
                            
                            // Vérifier si le clic est valide (déplacement ou attaque)
                            boolean coupValide = false;
                            if (casesAccessibles != null && casesAccessibles.contains(caseClic2)) {
                                coupValide = true;
                                combatPreview.attaqueUnite = caseClic1.getUnites().calculAttaqueTotale();
                            } else if (casesAttaquables != null && casesAttaquables.contains(caseClic2)) {
                                coupValide = true;
                                combatPreview.attaqueUnite = caseClic1.getUnites().calculAttaqueTotale();
                            } else if (casesSuperposables != null && casesSuperposables.contains(caseClic2)) {
                                coupValide = true;
                                combatPreview.attaqueUnite = caseClic1.getUnites().calculAttaqueTotale();
                            }
                            
                            if (coupValide) {
                                Coup coup = new Coup(caseClic1, caseClic2);
                                jeu.envoyerCoup(coup);
                                combatPreview.attaqueUnite = 0;
                                
                                // Attendre un peu pour voir le résultat avant d'afficher le dialogue
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {}
                            }
                            
                            // Réinitialiser la sélection
                            caseClic1 = null;
                            caseClic2 = null;
                            casesAccessibles = null;
                            casesAttaquables = null;
                            casesSuperposables = null;
                            mettreAJourAffichage();
                        }

                    }
                });

                iP.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        Case caseSurvolee = plateau.getCases()[xx][yy];
                        if(caseClic1 != null && caseSurvolee.getUnites() != null && caseSurvolee.getUnites().getProprietaire() != caseClic1.getUnites().getProprietaire()){
                                combatPreview.defenseUnite = caseSurvolee.getUnites().calculDefenseTotale();
                                System.out.println(combatPreview.attaqueUnite + " contre " + combatPreview.defenseUnite);
                                combatPreview.calculatePercents(caseClic1,caseSurvolee);
                                mettreAJourAffichage();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        combatPreview.defenseUnite = 0;
                        mettreAJourAffichage();
                    }


                });



                grilleIP.add(iP);
            }
        }
        add(grilleIP, BorderLayout.CENTER);
    }

    
    /**
     * Il y a une grille du côté du modèle ( jeu.getGrille() ) et une grille du côté de la vue (tabIP)
     */
    private void mettreAJourAffichage() {

        // Mettre à jour les labels avec couleur du joueur
        labelJoueurCourant.setText("Tour de : " + jeu.getJoueurCourant().getCouleur() + " (" + jeu.getJoueurCourant().getPeuple().getNom() + ") - Points: " + jeu.getJoueurCourant().getScore());
        labelJoueurCourant.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Colorer le texte selon le joueur
        switch(jeu.getJoueurCourant().getCouleur()) {
            case "Rouge":
                labelJoueurCourant.setForeground(new Color(200, 0, 0));
                break;
            case "Bleu":
                labelJoueurCourant.setForeground(new Color(0, 0, 200));
                break;
            case "Jaune":
                labelJoueurCourant.setForeground(new Color(200, 150, 0));
                break;
            case "Vert":
                labelJoueurCourant.setForeground(new Color(0, 150, 0));
                break;
        }
        
        labelTour.setText("Tour: " + jeu.getTourActuel() + "/" + jeu.getNbToursMax());
        labelTour.setFont(new Font("Arial", Font.BOLD, 14));

        // Affichage prédiction combat
        if(combatPreview.defenseUnite != 0){ // S'affiche uniquement si 2 unités sont renseignées dans le champ
            combatPreview.showPercents();
        }else{
            combatPreview.hidePercents();
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                switch (plateau.getCases()[x][y].getBiome()){
                    case PLAINE:
                        tabIP[x][y].setBackground(icoPlaine);
                        break;
                    case MONTAGNE:
                        tabIP[x][y].setBackground(icoMontagne);
                        break;
                    case DESERT:
                        tabIP[x][y].setBackground(icoDesert);
                        break;
                    case FORET:
                        tabIP[x][y].setBackground(icoForet);
                        break;
                }

                tabIP[x][y].setFront(null);
                
                // Réinitialiser la bordure
                tabIP[x][y].setBorderColor(null);

                Case c = plateau.getCases()[x][y];

                if(c.getUnites() != null){
                    tabIP[x][y].setNbUnites(c.getUnites().getNbUnit()); //get NB
                }else{
                    tabIP[x][y].setNbUnites(0);
                }

                if (c != null) {

                    Unites u = c.getUnites();

                    // Moche mais juste pour tester si ça marche
                    if (u instanceof Elfe) {
                        tabIP[x][y].setFront(icoElfes);
                    }
                    if (u instanceof Humain) {
                        tabIP[x][y].setFront(icoHumain);
                    }
                    if (u instanceof Nain) {
                        tabIP[x][y].setFront(icoNain);
                    }
                    if (u instanceof Gobelin) {
                        tabIP[x][y].setFront(icoGobelin);
                    }
                }

                // Afficher les cases sélectionnées et disponibles
                if (caseClic1 != null && c == caseClic1) {
                    tabIP[x][y].setBorderColor(Color.YELLOW); // Case sélectionnée
                    tabIP[x][y].setFillColor(new Color(255, 255, 0, 70));
                }

                if (casesAccessibles != null && casesAccessibles.contains(c)) {
                    tabIP[x][y].setBorderColor(Color.GREEN); // Cases accessibles pour déplacement
                    tabIP[x][y].setFillColor(new Color(0, 255, 0, 70));
                }

                if (casesAttaquables != null && casesAttaquables.contains(c)) {
                    tabIP[x][y].setBorderColor(Color.RED); // Cases attaquables
                    tabIP[x][y].setFillColor(new Color(255, 0, 0, 70));
                }

                if (casesSuperposables != null && casesSuperposables.contains(c)) {
                    tabIP[x][y].setBorderColor(Color.BLUE); // Cases alliees
                    tabIP[x][y].setFillColor(new Color(0, 0, 120, 10));
                }

            }
        }
        grilleIP.repaint();


    }

    @Override
    public void update(Observable o, Object arg) {

        SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mettreAJourAffichage();
                        
                        // Vérifier s'il y a un résultat de combat à afficher dans le terminal
                        ResultatCombat resultat = jeu.getDernierResultatCombat();
                        if (resultat != null) {
                            afficherResultatCombatTerminal(resultat);
                        }
                    }
                }); 

    }

    private void afficherResultatCombatTerminal(ResultatCombat resultat) {
        String attaquantNom = resultat.attaquant.getTypePeuple().getNom();
        String attaquantJoueur = resultat.attaquant.getProprietaire().getCouleur();
        String defenseurNom = resultat.defenseur.getTypePeuple().getNom();
        String defenseurJoueur = resultat.defenseur.getProprietaire().getCouleur();
        
        String gagnant = resultat.attaquantGagne ? attaquantJoueur : defenseurJoueur;
        
        System.out.println("\n═══════════════════════════════════");
        System.out.println("      RÉSULTAT DU COMBAT");
        System.out.println("═══════════════════════════════════");
        System.out.println("⚔️  ATTAQUANT: " + attaquantNom + " (" + attaquantJoueur + ")");
        System.out.println("    Force d'attaque: " + resultat.forceAttaquant + resultat.descriptionTerrainDefenseur);
        System.out.println();
        System.out.println("🛡️  DÉFENSEUR: " + defenseurNom + " (" + defenseurJoueur + ")");
        System.out.println("    Force de défense: " + resultat.forceDefenseur + resultat.descriptionTerrainDefenseur);
        System.out.println();
        System.out.println("    Proba de gagner pour l'attaque: " + (double) resultat.forceAttaquant / (resultat.forceAttaquant + resultat.forceDefenseur));
        System.out.println();
        System.out.println("───────────────────────────────────");
        if (resultat.attaquantGagne) {
            System.out.println("VICTOIRE ! " + gagnant + " remporte le combat !");
        } else {
            System.out.println("DÉFAITE... " + gagnant + " remporte le combat !");
        }
        System.out.println("═══════════════════════════════════\n");
    }
}

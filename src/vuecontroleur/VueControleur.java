package vuecontroleur;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import modele.ia.RechercheArbreMonteCarlo;
import modele.jeu.peuple.*;
import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.jeu.Joueur;
import modele.jeu.ResultatCombat;
import modele.plateau.Biome;
import modele.plateau.Case;
import modele.plateau.Plateau;

public class VueControleur extends JFrame implements Observer {
    
    // couleur retro blanc
    private static final Color RETRO_BG = new Color(245, 240, 225);        // Crème
    private static final Color RETRO_BG_DARK = new Color(220, 210, 190);   // Crème foncé
    private static final Color RETRO_TEXT = new Color(45, 40, 35);         // Brun foncé
    private static final Color RETRO_TEXT_DIM = new Color(120, 110, 95);   // Brun clair
    private static final Color RETRO_ACCENT = new Color(180, 80, 50);      // Rouge-brun rétro
    private static final Color RETRO_BORDER = new Color(160, 140, 110);    // Bordure beige
    
    // les etats du menu
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    
    // font de type comme dans le terminal pour l'effet retro
    private Font terminalFont;
    
    // les etats du jeu
    private Plateau plateau;
    private Jeu jeu;
    private int sizeX;
    private int sizeY;
    private static final int pxCase = 100;
    
    private Image icoElfes, icoHumain, icoGobelin, icoNain;
    private Image icoDesert, icoPlaine, icoForet, icoMontagne;

    private JComponent grilleIP;
    private Case caseClic1, caseClic2;
    private List<Case> casesAccessibles, casesAttaquables, casesSuperposables;
    
    private JLabel labelJoueurCourant, labelTour;
    private JLabel labelTerrainFavori;
    private JButton btnPasserTour;
    private JButton btnCoupPrev, btnCoupNext;
    private boolean reviewMode = false;
    private int reviewIndex = -1;
    private boolean finAnnoncee = false;
    private CombatPreview combatPreview;
    private ImagePanel[][] tabIP;
    private RechercheArbreMonteCarlo mcts = new RechercheArbreMonteCarlo();
    private Case lastDep, lastArr;
    private javax.swing.Timer lastMoveTimer;

    private String formatBiome(Biome biome){
        switch (biome){
            case PLAINE: return "Plaine";
            case MONTAGNE: return "Montagne";
            case FORET: return "Forêt";
            case DESERT: return "Désert";
            default: return biome != null ? biome.name() : "";
        }
    }
    
    public VueControleur() {
        setTitle("SmallWorld par Nathan && Leonard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setBackground(RETRO_BG);
        
        terminalFont = new Font("Monospaced", Font.BOLD, 14);
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(RETRO_BG);
        
        mainContainer.add(creerEcranTerminal(), "MENU");
        
        setContentPane(mainContainer);
        cardLayout.show(mainContainer, "MENU");
        
        // Focus pour le clavier
        setFocusable(true);
        requestFocus();
    }
    
    // Menu
    
    private JPanel creerEcranTerminal() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(RETRO_BG);
        
        JPanel contenu = new JPanel();
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBackground(RETRO_BG);
        contenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(RETRO_BORDER, 3),
            BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));
        
        // Titre
        JLabel titre = new JLabel("SMALL WORLD");
        titre.setFont(new Font("Monospaced", Font.BOLD, 42));
        titre.setForeground(RETRO_TEXT);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenu.add(titre);
        
        contenu.add(Box.createVerticalStrut(50));
        
        // Bouton 2 joueurs
        JButton btn2 = creerBoutonMenu("Partie à 2 joueurs");
        btn2.addActionListener(e -> lancerPartieRapide(2));
        contenu.add(btn2);
        
        contenu.add(Box.createVerticalStrut(20));
        
        // Bouton 4 joueurs
        JButton btn4 = creerBoutonMenu("Partie à 4 joueurs");
        btn4.addActionListener(e -> lancerPartieRapide(4));
        contenu.add(btn4);
        
        contenu.add(Box.createVerticalStrut(40));
        
        // Info
        JLabel info = new JLabel("Les peuples sont attribués aléatoirement");
        info.setFont(new Font("Monospaced", Font.PLAIN, 12));
        info.setForeground(RETRO_TEXT_DIM);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenu.add(info);
        
        panel.add(contenu);
        return panel;
    }
    
    private JButton creerBoutonMenu(String texte) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Monospaced", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(300, 50));
        btn.setMaximumSize(new Dimension(300, 50));
        btn.setBackground(RETRO_BG_DARK);
        btn.setForeground(RETRO_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(RETRO_BORDER, 2));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(RETRO_ACCENT);
                btn.setForeground(RETRO_ACCENT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(RETRO_BG_DARK);
                btn.setForeground(RETRO_TEXT);
            }
        });
        
        return btn;
    }
    
    private void lancerPartieRapide(int nbJoueurs) {
        // Mélanger les peuples et en prendre le nombre nécessaire
        List<TypePeuple> peuplesMelanges = new ArrayList<>(Arrays.asList(TypePeuple.values()));
        Collections.shuffle(peuplesMelanges);
        
        TypePeuple[] peuplesChoisis = new TypePeuple[nbJoueurs];
        for (int i = 0; i < nbJoueurs; i++) {
            peuplesChoisis[i] = peuplesMelanges.get(i);
        }

        // Choix du nombre d'IA
        boolean[] joueursIA = demanderConfigurationIA(nbJoueurs);
        
        // Lancer la partie
        jeu = new Jeu(nbJoueurs, peuplesChoisis, joueursIA);
        plateau = jeu.getPlateau();
        sizeX = Plateau.SIZE_X;
        sizeY = Plateau.SIZE_Y;
        
        chargerLesIcones();
        
        JPanel ecranJeu = creerEcranJeuOriginal();
        mainContainer.add(ecranJeu, "JEU");
        cardLayout.show(mainContainer, "JEU");
        
        plateau.addObserver(this);
        mettreAJourAffichage();

        // Si le premier joueur est une IA, la faire jouer immédiatement
        jouerTourIA();
        
        setSize(sizeX * pxCase, sizeY * pxCase + 120);
        setLocationRelativeTo(null);
    }
    
    // jeu

    private void chargerLesIcones() {
        // Charger les sprites des unités
        icoHumain = new ImageIcon("./data/units/sprites/humain.png").getImage();
        icoGobelin = new ImageIcon("./data/units/sprites/gobelin.png").getImage();
        icoElfes = new ImageIcon("./data/units/sprites/elfe.png").getImage();
        icoNain = new ImageIcon("./data/units/sprites/nain.png").getImage();
        
        // Charger les terrains
        icoDesert = new ImageIcon("./data/terrain/desert.png").getImage();
        icoForet = new ImageIcon("./data/terrain/forest.png").getImage();
        icoMontagne = new ImageIcon("./data/terrain/moutain.png").getImage();
        icoPlaine = new ImageIcon("./data/terrain/plain.png").getImage();
    }
    
    private JPanel creerEcranJeuOriginal() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(RETRO_BG);
        
        // panel info (en haut)
        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setBackground(RETRO_BG_DARK);
        panelInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, RETRO_BORDER),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        labelJoueurCourant = new JLabel();
        labelJoueurCourant.setFont(terminalFont);
        labelJoueurCourant.setForeground(RETRO_TEXT);
        
        JPanel panelDroite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        panelDroite.setBackground(RETRO_BG_DARK);
        
        labelTour = new JLabel();
        labelTour.setFont(terminalFont);
        labelTour.setForeground(RETRO_TEXT_DIM);

        btnCoupPrev = creerBoutonRetroJeu("←");
        btnCoupPrev.setPreferredSize(new Dimension(40, 30));
        btnCoupPrev.setEnabled(false);
        btnCoupPrev.addActionListener(e -> montrerCoup(-1));

        btnCoupNext = creerBoutonRetroJeu("→");
        btnCoupNext.setPreferredSize(new Dimension(40, 30));
        btnCoupNext.setEnabled(false);
        btnCoupNext.addActionListener(e -> montrerCoup(1));
        
        btnPasserTour = creerBoutonRetroJeu("[ FIN TOUR ]");
        // Fin de tour automatique : un coup par tour
        btnPasserTour.setEnabled(false);
        
        panelDroite.add(labelTour);
        panelDroite.add(btnCoupPrev);
        panelDroite.add(btnCoupNext);
        panelDroite.add(btnPasserTour);
        
        JPanel panelGauche = new JPanel();
        panelGauche.setLayout(new BoxLayout(panelGauche, BoxLayout.Y_AXIS));
        panelGauche.setBackground(RETRO_BG_DARK);

        labelTerrainFavori = new JLabel();
        labelTerrainFavori.setFont(terminalFont);
        labelTerrainFavori.setForeground(RETRO_TEXT_DIM);

        panelGauche.add(labelJoueurCourant);
        panelGauche.add(labelTerrainFavori);

        panelInfo.add(panelGauche, BorderLayout.WEST);
        panelInfo.add(panelDroite, BorderLayout.EAST);
        
        panel.add(panelInfo, BorderLayout.NORTH);

        // preview combat info (en bas)
        combatPreview = new CombatPreview();
        combatPreview.panel.setBackground(RETRO_BG_DARK);
        combatPreview.panel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, RETRO_BORDER));
        panel.add(combatPreview.panel, BorderLayout.SOUTH);

        // grille du jeu
        grilleIP = new JPanel(new GridLayout(sizeY, sizeX, 2, 2));
        grilleIP.setBackground(RETRO_BORDER);
        grilleIP.setBorder(BorderFactory.createLineBorder(RETRO_BORDER, 2));

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
                                jeu.appliquerCoup(coup);
                                combatPreview.attaqueUnite = 0;
                                marquerDernierCoup(caseClic1, caseClic2);
                                jeu.passerAuJoueurSuivant(); // un coup par tour
                            }

                            // Réinitialiser la sélection
                            caseClic1 = null;
                            caseClic2 = null;
                            casesAccessibles = null;
                            casesAttaquables = null;
                            casesSuperposables = null;
                            mettreAJourAffichage();
                            jouerTourIA();
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
                                combatPreview.calculerPourcentages(caseClic1,caseSurvolee);
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
        
        panel.add(grilleIP, BorderLayout.CENTER);
        
        return panel;
    }
    
    // bouton retro pour le jeu
    private JButton creerBoutonRetroJeu(String texte) {
        JButton btn = new JButton(texte) {
            private boolean hover = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                
                g2d.setColor(hover ? RETRO_ACCENT : RETRO_BG);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(RETRO_BORDER);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(1, 1, getWidth()-2, getHeight()-2);
                
                g2d.setColor(hover ? RETRO_BG : RETRO_TEXT);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
            
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
        };
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(120, 30));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Demande combien d'IA doivent être utilisées.
     * Les IA sont assignées aux derniers joueurs pour laisser le joueur humain commencer.
     */
    private boolean[] demanderConfigurationIA(int nbJoueurs){
        String message = "Combien d'IA MCTS souhaitez-vous ? (0 à " + nbJoueurs + ")";
        String input = JOptionPane.showInputDialog(this, message, "IA MCTS", JOptionPane.QUESTION_MESSAGE);
        int nbIA = 0;
        try{
            nbIA = Integer.parseInt(input);
            if(nbIA < 0) nbIA = 0;
            if(nbIA > nbJoueurs) nbIA = nbJoueurs;
        }catch(Exception e){
            nbIA = 0;
        }

        boolean[] ia = new boolean[nbJoueurs];
        // On place les IA à la fin pour que le premier joueur soit humain par défaut
        for(int i = 0; i < nbIA; i++){
            ia[nbJoueurs - 1 - i] = true;
        }
        return ia;
    }
    
    private void mettreAJourAffichage() {
        if (jeu == null) return;

        String couleur = jeu.getJoueurCourant().getCouleur();
        String peuple = jeu.getJoueurCourant().getPeuple().getNom().toUpperCase();
        int score = jeu.getJoueurCourant().getScore();
        
        labelJoueurCourant.setText("> " + peuple + " - PTS: " + score);
        labelJoueurCourant.setFont(terminalFont);
        labelJoueurCourant.setForeground(getCouleurRetro(couleur));

        Biome favori = jeu.getJoueurCourant().getPeuple().getTerrainFavori();
        labelTerrainFavori.setText("Terrain favori : " + formatBiome(favori));
        
        if(reviewMode && jeu.getHistorique().size() > 0){
            labelTour.setText("Revue " + (reviewIndex+1) + "/" + jeu.getHistorique().size());
        }else{
            labelTour.setText("TOUR " + jeu.getTourActuel() + "/" + jeu.getNbToursMax());
        }
        labelTour.setFont(terminalFont);

        // Détection de fin de partie (même si aucune notification Observable n'est envoyée)
        if(jeu.hasEnded() && !finAnnoncee){
            finAnnoncee = true;
            btnCoupPrev.setEnabled(true);
            btnCoupNext.setEnabled(true);
            afficherFinPartie();
        }

        if(combatPreview.defenseUnite != 0){
            combatPreview.afficherPourcentages();
        }else{
            combatPreview.masquerPourcentages();
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
                tabIP[x][y].setBorderColor(null);

                Case c = plateau.getCases()[x][y];

                if(c.getUnites() != null){
                    tabIP[x][y].setNbUnites(c.getUnites().getNbUnit());
                }else{
                    tabIP[x][y].setNbUnites(0);
                }

                if (c != null) {
                    Unites u = c.getUnites();

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

                // Couleurs bien visibles pour les surbrillances
                if (!reviewMode && caseClic1 != null && c == caseClic1) {
                    tabIP[x][y].setBorderColor(new Color(255, 180, 0));      // Orange vif
                    tabIP[x][y].setFillColor(new Color(255, 200, 50, 120));
                }

                if (!reviewMode && casesAccessibles != null && casesAccessibles.contains(c)) {
                    tabIP[x][y].setBorderColor(new Color(50, 200, 50));      // Vert vif
                    tabIP[x][y].setFillColor(new Color(100, 255, 100, 100));
                }

                if (!reviewMode && casesAttaquables != null && casesAttaquables.contains(c)) {
                    tabIP[x][y].setBorderColor(new Color(220, 50, 50));      // Rouge vif
                    tabIP[x][y].setFillColor(new Color(255, 80, 80, 110));
                }

                if (!reviewMode && casesSuperposables != null && casesSuperposables.contains(c)) {
                    tabIP[x][y].setBorderColor(new Color(80, 80, 220));      // Bleu vif
                    tabIP[x][y].setFillColor(new Color(120, 120, 255, 100));
                }

                // Surbrillance du dernier coup joué
                if (lastDep != null && c == lastDep) {
                    tabIP[x][y].setBorderColor(new Color(255, 200, 50));
                    tabIP[x][y].setFillColor(new Color(255, 230, 150, 140));
                }
                if (lastArr != null && c == lastArr) {
                    tabIP[x][y].setBorderColor(new Color(80, 180, 255));
                    tabIP[x][y].setFillColor(new Color(150, 220, 255, 160));
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
                        if(jeu.hasEnded() && !finAnnoncee){
                            finAnnoncee = true;
                            afficherFinPartie();
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
    
    /**
     * Surbrillance courte pour montrer le coup joué.
     */
    private void marquerDernierCoup(Case dep, Case arr){
        lastDep = dep;
        lastArr = arr;
        if(lastMoveTimer != null){
            lastMoveTimer.stop();
        }
        lastMoveTimer = new javax.swing.Timer(800, evt -> {
            lastDep = null;
            lastArr = null;
            mettreAJourAffichage();
        });
        lastMoveTimer.setRepeats(false);
        lastMoveTimer.start();
    }

    /**
     * Active le mode revue une fois la partie terminée.
     */
    private void activerModeRevue(){
        List<Jeu.HistoriqueCoup> hist = jeu.getHistorique();
        if(hist == null || hist.isEmpty()){
            return;
        }
        reviewMode = true;
        reviewIndex = Math.max(0, Math.min(hist.size()-1, reviewIndex == -1 ? 0 : reviewIndex));
        btnCoupPrev.setEnabled(true);
        btnCoupNext.setEnabled(true);
        if(lastMoveTimer != null){
            lastMoveTimer.stop();
        }
        jeu.appliquerSnapshot(reviewIndex+1);
        mettreAJourAffichage();
    }

    /**
     * Affiche les infos de fin de partie (winner + scores).
     */
    private void afficherFinPartie(){
        Joueur gagnant = jeu.getGagnantFinal();
        StringBuilder sb = new StringBuilder();
        sb.append("Partie terminée.\n\nScores :\n");
        for(Joueur j : jeu.getJoueurs()){
            sb.append(j.toString()).append("\n");
        }
        if(gagnant != null){
            sb.append("\nGagnant : ").append(gagnant.toString());
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Fin de partie", JOptionPane.INFORMATION_MESSAGE);
        activerModeRevue();
    }

    /**
     * Navigation dans l'historique des coups (flèches).
     */
    private void montrerCoup(int delta){
        if(jeu == null || jeu.getHistorique() == null || jeu.getHistorique().isEmpty()){
            return;
        }
        reviewMode = true;
        List<Jeu.HistoriqueCoup> hist = jeu.getHistorique();
        reviewIndex = Math.max(0, Math.min(hist.size()-1, (reviewIndex == -1 ? 0 : reviewIndex) + delta));
        Jeu.HistoriqueCoup hc = hist.get(reviewIndex);
        // Appliquer l'état du plateau correspondant à ce coup
        jeu.appliquerSnapshot(reviewIndex+1); // snapshot après ce coup
        if(lastMoveTimer != null){
            lastMoveTimer.stop();
        }
        lastDep = plateau.getCases()[hc.depX][hc.depY];
        lastArr = plateau.getCases()[hc.arrX][hc.arrY];
        mettreAJourAffichage();
    }

    /**
     * Déclenche le tour automatique si le joueur courant est une IA.
     */
    private void jouerTourIA(){
        if(jeu == null || plateau == null || !jeu.estJoueurCourantIA() || jeu.hasEnded()){
            return;
        }

        // Thread séparé pour ne pas bloquer l'EDT pendant la recherche
        new Thread(() -> {
            try {
                Thread.sleep(150); // petite pause visuelle
            } catch (InterruptedException ignored) {}

            Coup coupIA = mcts.choisirMeilleurCoup(jeu);

            if(coupIA != null){
                jeu.appliquerCoup(coupIA);
                marquerDernierCoup(coupIA.getDep(), coupIA.getArr());
            }

            jeu.passerAuJoueurSuivant();

            SwingUtilities.invokeLater(() -> {
                caseClic1 = null;
                caseClic2 = null;
                casesAccessibles = null;
                casesAttaquables = null;
                casesSuperposables = null;
                combatPreview.attaqueUnite = 0;
                combatPreview.defenseUnite = 0;
                mettreAJourAffichage();
                // Enchaîner si plusieurs IA consécutives
                jouerTourIA();
            });
        }).start();
    }


    private Color getCouleurRetro(String couleur) {
        switch (couleur) {
            case "Rouge": return new Color(180, 60, 60);
            case "Bleu": return new Color(60, 100, 160);
            case "Jaune": return new Color(180, 140, 40);
            case "Vert": return new Color(60, 130, 60);
            default: return RETRO_TEXT;
        }
    }
}

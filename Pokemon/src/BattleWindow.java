import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class BattleWindow extends JFrame implements ActionListener {
    
    private PKMN p1;
    private PKMN p2;
    private Random random;

    private JTextArea battleLog;
    private JLabel p1StatsLabel;
    private JLabel p2StatsLabel;
    private JLabel p1SpriteLabel; 
    private JLabel p2SpriteLabel; 
    private JButton tackleButton, skill1Button, skill2Button, skill3Button, skill4Button, healthPotionButton, manaPotionButton, protectButton;
    private JPanel actionPanel;
    
    // MODIFIED Helper function: Loads the icon image and uses it as the sprite.
    private JLabel createSpriteLabel(String pokemonName, boolean isPlayer) {
        // ASSUMPTION: The single image is named <pokemonname>_icon.png
        // This reuses the file defined in App.java's POKEMON_DATA
        String iconPath = "images/" + pokemonName.toLowerCase() + "_icon.png"; 
        JLabel label = new JLabel();
        
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource(iconPath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                if (!isPlayer) {
                    // Only flip the opponent's sprite (P2)
                    icon = flipImage(icon); 
                }
                label.setIcon(icon);
            } else {
                label.setText(pokemonName); 
                System.err.println("Couldn't find image file: " + iconPath);
            }
        } catch (Exception e) {
            label.setText(pokemonName);
            System.err.println("Error loading image: " + e.getMessage());
        }
        return label;
    }

    // Helper function to flip the image (UNLESS you use animated GIFs, then read the previous note)
    private ImageIcon flipImage(ImageIcon icon) {
        Image image = icon.getImage();
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        
        java.awt.image.BufferedImage flippedImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = flippedImage.createGraphics();
        
        g2d.drawImage(image, width, 0, -width, height, null);
        g2d.dispose();
        
        return new ImageIcon(flippedImage);
    }

    // CONSTRUCTOR
    public BattleWindow(PKMN playerPokemon, PKMN opponentPokemon) {
        
        this.p1 = playerPokemon;
        this.p2 = opponentPokemon;
        this.random = new Random();

        battleLog = new JTextArea(15, 50);
        PKMN.battleLog = this.battleLog;
        PKMN.appInstance = this;
        
        this.setTitle("PKMN Battle Simulator (GUI)");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(10, 10));

        // Status and Sprite Panel
        JPanel statusAndSpritePanel = new JPanel(new BorderLayout()); 
        
        // --- Sprite Display ---
        JPanel spritePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        p1SpriteLabel = createSpriteLabel(p1.name, true);
        p2SpriteLabel = createSpriteLabel(p2.name, false);
        spritePanel.add(p1SpriteLabel);
        spritePanel.add(p2SpriteLabel);
        
        // --- Stats Display ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        p1StatsLabel = new JLabel();
        p2StatsLabel = new JLabel();
        p1StatsLabel.setBorder(BorderFactory.createTitledBorder("Player 1: " + p1.name));
        p2StatsLabel.setBorder(BorderFactory.createTitledBorder("Player 2: " + p2.name));
        statsPanel.add(p1StatsLabel);
        statsPanel.add(p2StatsLabel);

        statusAndSpritePanel.add(spritePanel, BorderLayout.NORTH); 
        statusAndSpritePanel.add(statsPanel, BorderLayout.SOUTH);  
        
        this.add(statusAndSpritePanel, BorderLayout.NORTH);

        // Battle Log Area (Center)
        battleLog.setEditable(false);
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(battleLog);
        this.add(scrollPane, BorderLayout.CENTER); 

        // Action Panel (Bottom)
        actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(3, 3, 5, 5)); 

        tackleButton = new JButton("Tackle (Stamina +20)");
        
        skill1Button = new JButton("Skill 1 (Cost: " + p1.getSkill1Cost() + ")");
        skill2Button = new JButton("Skill 2 (Cost: " + p1.getSkill2Cost() + ")");
        skill3Button = new JButton("Skill 3 (Cost: " + p1.getSkill3Cost() + ")");
        skill4Button = new JButton("Skill 4 (Cost: " + p1.getSkill4Cost() + ") [50% Miss]");
        
        healthPotionButton = new JButton("Health Potion (+120 HP)"); 
        manaPotionButton = new JButton("Mana Potion (+60 Stam)");
        protectButton = new JButton("Protect (Block 1 Turn)");

        actionPanel.add(tackleButton);
        actionPanel.add(skill1Button);
        actionPanel.add(skill2Button);
        actionPanel.add(skill3Button);
        actionPanel.add(skill4Button);
        actionPanel.add(healthPotionButton);
        actionPanel.add(manaPotionButton);
        actionPanel.add(protectButton); 
        actionPanel.add(new JLabel("")); 

        tackleButton.addActionListener(this);
        skill1Button.addActionListener(this);
        skill2Button.addActionListener(this);
        skill3Button.addActionListener(this);
        skill4Button.addActionListener(this);
        healthPotionButton.addActionListener(this);
        manaPotionButton.addActionListener(this);
        protectButton.addActionListener(this);

        this.add(actionPanel, BorderLayout.SOUTH);

        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);

        appendLog("--- PKMN Battle Commences! ---");
        appendLog(p1.name + " vs. " + p2.name + "! What will " + p1.name + " do?");
        updateStatsDisplay();
    }
    
    private void appendLog(String message) {
        battleLog.append(message + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    // Public method called by PKMN to refresh the stats display
    public void updateStatsDisplay() {
        String p1ProtectionStatus = p1.getIsProtected() ? " **(PROTECTED)**" : "";
        p1StatsLabel.setText(
            "<html>Health: **" + p1.health + "** / " + p1.getMaxHealth() + "<br>" +
            "Stamina: **" + p1.stamina + "** / " + p1.getMaxStamina() + "<br>" +
            "Type: " + p1.type + " | Status: " + p1.status + " (D:" + p1.duration + ")" + p1ProtectionStatus + "<br>" +
            "Potions: HP **(" + (p1.getMaxHpUses() - p1.getHealthPotionUses()) + "/" + p1.getMaxHpUses() + ")** | Stam **(" + (p1.getMaxStaminaUses() - p1.getStaminaPotionUses()) + "/" + p1.getMaxStaminaUses() + ")**</html>"
        );
        
        String p2ProtectionStatus = p2.getIsProtected() ? " **(PROTECTED)**" : "";
        p2StatsLabel.setText(
            "<html>Health: **" + p2.health + "** / " + p2.getMaxHealth() + "<br>" +
            "Stamina: **" + p2.stamina + "** / " + p2.getMaxStamina() + "<br>" +
            "Type: " + p2.type + " | Status: " + p2.status + " (D:" + p2.duration + ")" + p2ProtectionStatus + "<br>" +
            "Potions: HP **(" + (p2.getMaxHpUses() - p2.getHealthPotionUses()) + "/" + p2.getMaxHpUses() + ")** | Stam **(" + (p2.getMaxStaminaUses() - p2.getStaminaPotionUses()) + "/" + p2.getMaxStaminaUses() + ")**</html>"
        );

        skill1Button.setText("Skill 1 (Cost: " + p1.getSkill1Cost() + ")");
        skill2Button.setText("Skill 2 (Cost: " + p1.getSkill2Cost() + ")");
        skill3Button.setText("Skill 3 (Cost: " + p1.getSkill3Cost() + ")");
        skill4Button.setText("Skill 4 (Cost: " + p1.getSkill4Cost() + ") [50% Miss]");

        healthPotionButton.setText("Health Potion (+120 HP) [" + (p1.getMaxHpUses() - p1.getHealthPotionUses()) + " left]");
        manaPotionButton.setText("Mana Potion (+60 Stam) [" + (p1.getMaxStaminaUses() - p1.getStaminaPotionUses()) + " left]");
        
        healthPotionButton.setEnabled(p1.getHealthPotionUses() < p1.getMaxHpUses());
        manaPotionButton.setEnabled(p1.getStaminaPotionUses() < p1.getMaxStaminaUses());
        
        if (p1.getTurnsSinceProtected() <= 1) {
            protectButton.setText("Protect (Cannot use consecutively)");
            protectButton.setEnabled(false);
        } else {
            protectButton.setText("Protect (Block 1 Turn)");
            protectButton.setEnabled(true);
        }
    }
    
    private void checkWinCondition() {
        if (p1.health <= 0) {
            appendLog("\n**" + p1.name + " fainted! " + p2.name + " wins the battle!**");
            disableButtons();
        } else if (p2.health <= 0) {
            appendLog("\n**" + p2.name + " fainted! " + p1.name + " wins the battle!**");
            disableButtons();
        }
    }
    
    private void disableButtons() {
        Component[] components = actionPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                ((JButton) component).setEnabled(false);
            }
        }
    }
    
    private void performAITurn() {
        if (p2.health <= 0) return;
        
        p2.prepareForTurn();
        
        boolean aiMoveWasSuccessful = false;
        
        if (p2.getTurnsSinceProtected() >= 1 && p2.health < p2.getMaxHealth() && p1.stamina > 80 && random.nextInt(100) < 20) {
             aiMoveWasSuccessful = p2.protect(); 
        } 
        
        if (!aiMoveWasSuccessful) {
            if (p2.health < 100 && p2.getHealthPotionUses() < p2.getMaxHpUses()) { 
                aiMoveWasSuccessful = p2.useHealthPotion();
            } else if (p2.stamina < 50 && p2.getStaminaPotionUses() < p2.getMaxStaminaUses()) { 
                aiMoveWasSuccessful = p2.useManaPotion();
            } else if (p2.stamina < 70) { 
                 aiMoveWasSuccessful = p2.tackle(p1);
            } else {
                int choice = random.nextInt(3) + 1; 
                switch (choice) {
                    case 1:
                        aiMoveWasSuccessful = p2.skill1(p1);
                        break;
                    case 2:
                        aiMoveWasSuccessful = p2.skill2(p1);
                        break;
                    case 3:
                        aiMoveWasSuccessful = p2.skill3(p1);
                        break;
                }
            }
        }
        
        checkWinCondition();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (p1.health <= 0 || p2.health <= 0) return;

        p1.prepareForTurn();
        
        boolean moveWasSuccessful = false;

        if (e.getSource() == tackleButton) {
            moveWasSuccessful = p1.tackle(p2);
        } else if (e.getSource() == skill1Button) {
            moveWasSuccessful = p1.skill1(p2);
        } else if (e.getSource() == skill2Button) {
            moveWasSuccessful = p1.skill2(p2);
        } else if (e.getSource() == skill3Button) {
            moveWasSuccessful = p1.skill3(p2);
        } else if (e.getSource() == skill4Button) {
            moveWasSuccessful = p1.skill4(p2);
        } else if (e.getSource() == healthPotionButton) {
            moveWasSuccessful = p1.useHealthPotion();
        } else if (e.getSource() == manaPotionButton) {
            moveWasSuccessful = p1.useManaPotion();
        } else if (e.getSource() == protectButton) {
            moveWasSuccessful = p1.protect(); 
        } else {
            return;
        }
        
        if (moveWasSuccessful && p2.health > 0 && p1.health > 0) {
            performAITurn();
        }
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class App extends JFrame implements ActionListener {

    private JLabel instructionLabel;

    // POKEMON_DATA: Storing Name, Type, Description, and Base Name
    private final String[][] POKEMON_DATA = {
        {"Charizard", "Fire", "Balanced", "charizard"}, // base name
        {"Ninetales", "Fire", "High Stamina/Smart", "ninetales"},
        {"Arcanine", "Fire", "High Health/Strong Tackle", "arcanine"},
        {"Blastoise", "Water", "Balanced", "blastoise"},
        {"Lapras", "Water", "High Health/Tank", "lapras"},
        {"Starmie", "Water", "High Stamina/Smart", "starmie"},
        {"Venusaur", "Grass", "Balanced", "venusaur"},
        {"Leafeon", "Grass", "High Stamina/Smart", "leafeon"},
        {"Meganium", "Grass", "High Health/Tank", "meganium"}
    };
    
    // CONSTRUCTOR
    public App() {
        this.setTitle("PKMN Selection");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(10, 10));

        instructionLabel = new JLabel("Choose your PKMN:", SwingConstants.CENTER);
        this.add(instructionLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 3, 10, 10));

        for (String[] data : POKEMON_DATA) {
            String name = data[0];
            String type = data[1];
            String description = data[2];
            
            // CONSTRUCT THE PATH: "images/" + baseName + "_icon.png"
            String iconPath = "images/" + data[3].toLowerCase() + "_icon.png"; 
            
            String buttonText = String.format("%s: %s (%s)", type, name, description);
            JButton button = new JButton(buttonText);
            
            try {
                // IMPORTANT: Looks for the file in the classpath (usually the bin/ or out/ folder)
                java.net.URL imgURL = getClass().getClassLoader().getResource(iconPath);
                
                if (imgURL != null) {
                    ImageIcon icon = new ImageIcon(imgURL);
                    button.setIcon(icon);
                    button.setHorizontalTextPosition(SwingConstants.CENTER);
                    button.setVerticalTextPosition(SwingConstants.BOTTOM);
                } else {
                    // This is the error message you are currently seeing
                    System.err.println("!!! Couldn't find image file: " + iconPath + " (Check file name casing and build path)");
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }

            button.addActionListener(this);
            buttonPanel.add(button);
        }
        
        this.add(buttonPanel, BorderLayout.CENTER);

        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
    
    private PKMN createOpponent(String playerPokemonName) {
        String opponentName;
        String opponentType;
        Random rand = new Random();
        
        String[] allFire = {"Charizard", "Ninetales", "Arcanine"};
        String[] allWater = {"Blastoise", "Lapras", "Starmie"};
        String[] allGrass = {"Venusaur", "Leafeon", "Meganium"};
        
        String[] allPokemonNames = new String[allFire.length + allWater.length + allGrass.length];
        System.arraycopy(allFire, 0, allPokemonNames, 0, allFire.length);
        System.arraycopy(allWater, 0, allPokemonNames, allFire.length, allWater.length);
        System.arraycopy(allGrass, 0, allPokemonNames, allFire.length + allWater.length, allGrass.length);
        
        do {
            int randomIndex = rand.nextInt(allPokemonNames.length);
            opponentName = allPokemonNames[randomIndex];
        } while (opponentName.equals(playerPokemonName));

        if (isPokemonInList(opponentName, allFire)) {
            opponentType = "Fire";
        } else if (isPokemonInList(opponentName, allWater)) {
            opponentType = "Water";
        } else { 
            opponentType = "Grass";
        }
        
        return new PKMN(opponentName, opponentType, "normal");
    }

    private boolean isPokemonInList(String name, String[] list) {
        for (String p : list) {
            if (p.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            String selection = ((JButton) e.getSource()).getText();
            String playerType;
            PKMN playerPokemon;
            
            String[] parts = selection.split(": ");
            String typePart = parts[0]; 
            String playerPokemonName = parts[1].split(" ")[0]; 
            
            if (typePart.equals("Fire")) {
                playerType = "Fire";
            } else if (typePart.equals("Water")) {
                playerType = "Water";
            } else { 
                playerType = "Grass";
            }
            
            playerPokemon = new PKMN(playerPokemonName, playerType, "normal");
            PKMN opponentPokemon = createOpponent(playerPokemonName);

            this.dispose();
            SwingUtilities.invokeLater(() -> new BattleWindow(playerPokemon, opponentPokemon));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}
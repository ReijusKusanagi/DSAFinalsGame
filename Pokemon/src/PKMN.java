import javax.swing.JTextArea;
import java.util.Random;

public class PKMN {

   // Static references required to communicate with the GUI (App.java)
   public static JTextArea battleLog;
   public static BattleWindow appInstance;
   
   // SHARED CONSTANTS (Potion Limits remain constant)
   private final int MAX_HP_USES = 3;
   private final int MAX_STAMINA_USES = 5;

   // INSTANCE VARIABLES
   String name;
   String type;
   String status;
   int duration;
   
   int health;
   int stamina;
   int maxHealth;    // NEW: Max stats stored per instance
   int maxStamina;   // NEW: Max stats stored per instance
   
   // NEW: Base attack values for dynamic calculation
   int baseTackleDamage;
   int skill1Damage;
   int skill2Damage;
   int skill3Damage;
   int skill4Damage;
   
   // NEW: Skill costs for dynamic calculation
   int skill1Cost;
   int skill2Cost;
   int skill3Cost;
   int skill4Cost;
   
   private int healthPotionUses;
   private int staminaPotionUses;
   private boolean isProtected;
   private int turnsSinceProtected;

   // MODIFIED CONSTRUCTOR: Dynamically sets stats based on name
   PKMN(String name, String type, String status) {
       this.name = name;
       this.type = type;
       this.status = status;
       this.duration = 0;
       this.healthPotionUses = 0;
       this.staminaPotionUses = 0;
       this.isProtected = false;
       this.turnsSinceProtected = 99;
       
       // Default values (used for Charizard, Blastoise, Venusaur)
       this.maxHealth = 300;
       this.maxStamina = 150;
       this.baseTackleDamage = 40;
       this.skill1Damage = 30;
       this.skill2Damage = 30;
       this.skill3Damage = 25;
       this.skill4Damage = 70;
       this.skill1Cost = 40;
       this.skill2Cost = 50;
       this.skill3Cost = 30;
       this.skill4Cost = 70;
       
       // --- SET UNIQUE STATS BASED ON POKEMON NAME ---
       if (name.equals("Ninetales")) {
           this.maxHealth = 250; // Less Health (-50)
           this.maxStamina = 200; // More Stamina (+50)
           this.skill1Damage = 40; // Stronger skills (+10)
           this.skill2Damage = 40; // Stronger skills (+10)
           this.skill3Damage = 35; // Stronger skills (+10)
           this.skill4Damage = 80; // Stronger skills (+10)
           this.skill1Cost = 35; // Slight cost reduction
           this.skill3Cost = 25; // Slight cost reduction
       } else if (name.equals("Arcanine")) {
           this.maxHealth = 350; // More Health (+50)
           this.maxStamina = 100; // Lesser Stamina (-50)
           this.baseTackleDamage = 50; // Higher tackle damage (+10)
           // Skills remain same damage/cost as Charizard
       } else if (name.equals("Lapras")) {
           this.maxHealth = 350; // More Health (+50)
           this.maxStamina = 200; // More Stamina (+50)
           this.skill1Damage = 25; // Weaker skill damage (-5)
           this.skill2Damage = 25; // Weaker skill damage (-5)
           this.skill3Damage = 20; // Weaker skill damage (-5)
           this.skill4Damage = 60; // Weaker skill damage (-10)
       } else if (name.equals("Starmie")) {
           this.maxHealth = 220; // Significantly less health (-80)
           this.maxStamina = 250; // Significantly more stamina (+100)
           this.skill1Damage = 50; // Significantly stronger skills (+20)
           this.skill2Damage = 50; // Significantly stronger skills (+20)
           this.skill3Damage = 45; // Significantly stronger skills (+20)
           this.skill4Damage = 110; // Significantly stronger skills (+40)
           this.skill1Cost = 50; // Higher cost
           this.skill2Cost = 60; // Higher cost
           this.skill4Cost = 90; // Higher cost
       } else if (name.equals("Leafeon")) {
           this.maxHealth = 250; // Less Health (-50)
           this.maxStamina = 200; // More Stamina (+50)
           this.skill1Damage = 40; // Stronger skill values (+10)
           this.skill2Damage = 40; // Stronger skill values (+10)
           this.skill3Damage = 35; // Stronger skill values (+10)
           this.skill4Damage = 80; // Stronger skill values (+10)
       } else if (name.equals("Meganium")) {
           this.maxHealth = 400; // Significantly higher health (+100)
           this.maxStamina = 80; // Significantly less stamina (-70)
           // Skills remain same damage/cost as Venusaur
       }
       
       this.health = this.maxHealth;
       this.stamina = this.maxStamina;
       
       appendLog(this.name + " is captured!");
   }
   
    private void appendLog(String message) {
        if (battleLog != null) {
            battleLog.append(message + "\n");
            // Auto-scroll to the bottom
            battleLog.setCaretPosition(battleLog.getDocument().getLength());
        } else {
            System.out.println(message);
        }
    }
   
	  int applyStatusEffects(PKMN target, int baseDamage) {


	       if (this.duration >= 3) {
	           this.status = "normal";
	           this.duration = 0;
	           appendLog(this.name + " is now in normal state.");
	       }


	       if (this.status.equals("chilled")) {
	           appendLog(this.name + " is chilled! Damage is reduced this turn.");
	           this.duration += 1;
	           baseDamage -= 10;
	       }


	       if (target.status.equals("poisoned")) {
	           target.health -= 10;
	           target.duration += 1;
	           appendLog("The poison weakens " + target.name + "! Extra damage dealt.");
	       }


	       if (target.status.equals("burned")) {
	           target.health -= 10;
	           target.duration += 1;
	           appendLog("The burn weakens " + target.name + "! Extra damage dealt.");
	       }
           
           if (appInstance != null) appInstance.updateStatsDisplay();

	       return baseDamage;
	   }
       
       public void prepareForTurn() {
           // If protected this turn, reset it before the next turn starts
           if (this.isProtected) {
               this.isProtected = false;
               appendLog(this.name + "'s protection faded!");
           }
           // Increment turns since protected, but keep it capped
           if (this.turnsSinceProtected < 99) {
               this.turnsSinceProtected++;
           }
       }


	   // TACKLE: Uses dynamic baseTackleDamage
	   boolean tackle(PKMN target) {
	       if (target.health <= 0) {
	           appendLog(target.name + " has already fainted. Tackle skill failed.");
	           return false; // Invalid move
	       }
           // Refined check to prevent gaining stamina when at MAX
	       if (this.stamina == this.maxStamina) { 
	           appendLog(this.name + " has max stamina, no stamina gained. Tackle failed.");
	           appendLog(this.name + "'s current stamina is at " + this.stamina + " points.");
               if (appInstance != null) appInstance.updateStatsDisplay();
	           return false; // Invalid move
	       }
           
           // CHECK PROTECTION STATUS ON TARGET
           if (target.isProtected) {
               appendLog(target.name + " is protected! Tackle failed.");
               return true; // Move was successful (used up turn), but damage was blocked
           }


	       int damage = this.baseTackleDamage; // Use dynamic tackle damage
	       int staminaGained = 20;
	       appendLog(this.name + " used tackle on " + target.name);
	       target.health -= damage;
	       if (target.health < 0) target.health = 0;
	       this.stamina += staminaGained;
	       
           // Check against dynamic maxStamina
	       if (this.stamina > this.maxStamina) { 
	           this.stamina = this.maxStamina;
               appendLog(target.name + "'s current health is at [" + target.health + "] points.");
	           appendLog(this.name + " gained " + staminaGained + " stamina points, reaching max.");
	       } else {
	           appendLog(target.name + "'s current health is at [" + target.health + "] points.");
	           appendLog(this.name + " gained " + staminaGained + " stamina points.");
	       }
           
           if (appInstance != null) appInstance.updateStatsDisplay();
	       if (target.health <= 0) appendLog(target.name + " fainted!");
           
           return true; // Valid move
	   }


	   // SKILL 1: Uses dynamic skill1Damage and skill1Cost
	   boolean skill1(PKMN target) {
	       int damage = this.skill1Damage; // Use dynamic skill damage
	       int staminaCost = this.skill1Cost; // Use dynamic stamina cost
	       
	       if (target.health <= 0) {
	           appendLog(target.name + " has already fainted. Skill 1 failed.");
	           return false;
	       }
	       
	       // CHECK: Prevent skill if stamina is less than the cost
	       if (this.stamina < staminaCost) {
	           appendLog(this.name + " does not have enough stamina to use Skill 1 (Cost: " + staminaCost + ").");
	           appendLog(this.name + "'s current stamina is at " + this.stamina + " points.");
               if (appInstance != null) appInstance.updateStatsDisplay();
               return false; // Invalid move
	       }
           
           // CHECK PROTECTION STATUS ON TARGET
           if (target.isProtected) {
               appendLog(target.name + " is protected! Skill 1 failed.");
               this.stamina -= staminaCost; // Still deduct cost if move was attempted
               if (appInstance != null) appInstance.updateStatsDisplay();
               return true; // Move was successful (used up turn), but damage was blocked
           }
           
	       // Apply status effects to modify incoming damage
	       damage = applyStatusEffects(target, damage);
	       
	       // If stamina is sufficient, proceed with the attack and cost deduction
           String skillName = "";
	       if (this.type.equals("Fire")) {
               skillName = "Flame Thrower";
	           if (target.type.equals("Grass")) {
	               damage += 15;
	               appendLog("It's **super effective!**");
	           } else if (target.type.equals("Water")) {
	               damage -= 15;
	               appendLog("It's not very effective...");
	           }
	       } else if (this.type.equals("Water")) {
               skillName = "Water Gun";
	           if (target.type.equals("Fire")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Grass")) {
                   damage -= 15;
                   appendLog("It's not very effective...");
               }
	       } else if (this.type.equals("Grass")) {
               skillName = "Vine Whip";
	           if (target.type.equals("Water")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Fire")) {
                   damage -= 10;
                   appendLog("It's not very effective...");
               }
	       }
           
           appendLog(this.name + " used **" + skillName + "** on " + target.name);
	       target.health -= damage;
	       this.stamina -= staminaCost; // Deduct stamina
	       
	       if (target.health < 0) target.health = 0;
	       appendLog(target.name + "'s current health is at [" + target.health + "] points.");
	       appendLog(this.name + " lost " + staminaCost + " stamina points.");
           if (appInstance != null) appInstance.updateStatsDisplay();
	       if (target.health <= 0) appendLog(target.name + " fainted!");
           
           return true; // Valid move
	   }


	   // SKILL 2: Uses dynamic skill2Damage and skill2Cost
	   boolean skill2(PKMN target) {
	       int damage = this.skill2Damage; // Use dynamic skill damage
	       int staminaCost = this.skill2Cost; // Use dynamic stamina cost
	       
	       if (target.health <= 0) {
	           appendLog(target.name + " has already fainted. Skill failed.");
	           return false;
	       }
	       
	       // CHECK: Prevent skill if stamina is less than the cost
	       if (this.stamina < staminaCost) {
	           appendLog(this.name + " does not have enough stamina to use Skill 2 (Cost: " + staminaCost + ").");
	           appendLog(this.name + "'s current stamina is at " + this.stamina + " points.");
               if (appInstance != null) appInstance.updateStatsDisplay();
               return false; // Invalid move
	       }
           
           // CHECK PROTECTION STATUS ON TARGET
           if (target.isProtected) {
               appendLog(target.name + " is protected! Skill 2 failed.");
               this.stamina -= staminaCost; // Still deduct cost if move was attempted
               if (appInstance != null) appInstance.updateStatsDisplay();
               return true; // Move was successful (used up turn), but damage was blocked
           }

	       damage = applyStatusEffects(target, damage);
	       
           String skillName = "";
	       if (this.type.equals("Fire")) {
               skillName = "Fire Spin";
	           if (target.type.equals("Grass")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Water")) {
                   damage -= 10;
                   appendLog("It's not very effective...");
               }
	       } else if (this.type.equals("Water")) {
               skillName = "Water Pulse";
	           if (target.type.equals("Fire")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Grass")) {
                   damage -= 10;
                   appendLog("It's not very effective...");
               }
	       } else if (this.type.equals("Grass")) {
               skillName = "Razor Leaf";
	           if (target.type.equals("Water")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Fire")) {
                   damage -= 10;
                   appendLog("It's not very effective...");
               }
	       }
           
           appendLog(this.name + " used **" + skillName + "** on " + target.name);
	       target.health -= damage;
	       this.stamina -= staminaCost; // Deduct stamina
	       
	       if (target.health < 0) target.health = 0;
	       appendLog(target.name + "'s current health is at [" + target.health + "] points.");
	       appendLog(this.name + " lost " + staminaCost + " stamina points.");
           if (appInstance != null) appInstance.updateStatsDisplay();
	       if (target.health <= 0) appendLog(target.name + " fainted!");
           
           return true; // Valid move
	   }


	   // SKILL 3: Uses dynamic skill3Damage and skill3Cost
	   boolean skill3(PKMN target) {
	       Random random = new Random();
	       int damage = this.skill3Damage; // Use dynamic skill damage
	       int staminaCost = this.skill3Cost; // Use dynamic stamina cost
	       
	       if (target.health <= 0) {
	           appendLog(target.name + " has already fainted. Skill 3 failed.");
	           return false;
	       }
	       
	       // CHECK: Prevent skill if stamina is less than the cost
	       if (this.stamina < staminaCost) {
	           appendLog(this.name + " does not have enough stamina to use Skill 3 (Cost: " + staminaCost + ").");
	           appendLog(this.name + "'s current stamina is at " + this.stamina + " points.");
               if (appInstance != null) appInstance.updateStatsDisplay();
               return false; // Invalid move
	       }
           
           // CHECK PROTECTION STATUS ON TARGET
           if (target.isProtected) {
               appendLog(target.name + " is protected! Skill 3 failed.");
               this.stamina -= staminaCost; // Still deduct cost if move was attempted
               if (appInstance != null) appInstance.updateStatsDisplay();
               return true; // Move was successful (used up turn), but damage was blocked
           }
	       
	       damage = applyStatusEffects(target, damage);
	       
           String skillName = "";
	       if (this.type.equals("Fire")) {
               skillName = "Fire Fang";
	           if (target.type.equals("Grass")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Water")) {
                   damage -= 15;
                   appendLog("It's not very effective...");
               }
	           if (random.nextInt(100) < 20) {
                   damage += 25;
                   appendLog("A **critical hit!**");
               }
	           if (random.nextInt(100) < 20) {
	               appendLog(target.name + " is **burned**! It will take extra damage next turn.");
	               target.status = "burned";
	               target.duration = 1;
	           }
	       } else if (this.type.equals("Water")) {
               skillName = "Bubble Beam";
	           if (target.type.equals("Fire")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Grass")) {
                   damage -= 15;
                   appendLog("It's not very effective...");
               }
	           if (random.nextInt(100) < 20) {
                   damage += 25;
                   appendLog("A **critical hit!**");
               }
	           if (random.nextInt(100) < 20) {
	               appendLog(target.name + " is **chilled**! Its damage will be reduced next turn.");
	               target.status = "chilled";
	               target.duration = 1;
	           }
	       } else if (this.type.equals("Grass")) {
               skillName = "Seed Bomb";
	           if (target.type.equals("Water")) {
                   damage += 15;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Fire")) {
                   damage -= 15;
                   appendLog("It's not very effective...");
               }
	           if (random.nextInt(100) < 20) {
                   damage += 25;
                   appendLog("A **critical hit!**");
               }
	           if (random.nextInt(100) < 20) {
	               appendLog(target.name + " is **poisoned**! It will take extra damage next turn.");
	               target.status = "poisoned";
	               target.duration = 1;
	           }
	       }
           
           appendLog(this.name + " used **" + skillName + "** on " + target.name);
	       target.health -= damage;
	       this.stamina -= staminaCost; // Deduct stamina
	       
	       if (target.health < 0) target.health = 0;
	       appendLog(target.name + "'s current health is at [" + target.health + "] points.");
	       appendLog(this.name + " lost " + staminaCost + " stamina points.");
           if (appInstance != null) appInstance.updateStatsDisplay();
	       if (target.health <= 0) appendLog(target.name + " fainted!");
           
           return true; // Valid move
	   }


	   // SKILL 4: Uses dynamic skill4Damage and skill4Cost
	   boolean skill4(PKMN target) {
	       int damage = this.skill4Damage; // Use dynamic skill damage
	       int staminaCost = this.skill4Cost; // Use dynamic stamina cost
	       Random random = new Random();
	       
           if (target.health <= 0) {
	           appendLog(target.name + " has already fainted. Skill 4 failed.");
	           return false;
	       }
           
           // CHECK: Prevent skill if stamina is less than the cost
           if (this.stamina < staminaCost) {
	           appendLog(this.name + " does not have enough stamina to use Skill 4 (Cost: " + staminaCost + ").");
	           appendLog(this.name + "'s current stamina is at " + this.stamina + " points.");
               if (appInstance != null) appInstance.updateStatsDisplay();
               return false; // Invalid move
	       }
           
           // CHECK PROTECTION STATUS ON TARGET
           if (target.isProtected) {
               appendLog(target.name + " is protected! Skill 4 failed.");
               this.stamina -= staminaCost; // Still deduct cost if move was attempted
               if (appInstance != null) appInstance.updateStatsDisplay();
               return true; // Move was successful (used up turn), but damage was blocked
           }
           
           // Check for miss (occurs before stamina deduction in original code)
	       if (random.nextInt(100) < 50) {
	           appendLog(this.name + "'s special attack **missed!**");
	           this.stamina -= (staminaCost / 2); // Still incur half cost on miss
	           appendLog(this.name + " lost " + (staminaCost / 2) + " stamina points.");
               if (appInstance != null) appInstance.updateStatsDisplay();
	           return true; // The player still took an action (miss), so turn passes
	       }
           
           // Status effect applications
	       if (this.duration >= 3) {
	           this.status = "normal";
	           this.duration = 0;
	           appendLog(this.name + " is now in normal state.");
	       }
	       if (this.status.equals("chilled")) {
	           appendLog(this.name + " is chilled! Damage is reduced this turn.");
	           this.duration += 1;
	           damage -= 10;
	       }
	       if (target.status.equals("poisoned")) {
	           target.health -= 10;
	           target.duration += 1;
	           appendLog("The poison weakens " + target.name + "! Extra damage dealt.");
	       }
	       if(target.status.equals("burned")) {
	           target.health -= 10;
	           target.duration += 1;
	           appendLog("The burn weakens " + target.name + "! Extra damage dealt.");
	       }       
	       
           String skillName = "";
           if (this.type.equals("Fire")) {
               skillName = "Fire Blast"; 
               if (target.type.equals("Grass")) {
                   damage += 25;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Water")) {
                   damage -= 35;
                   appendLog("It's not very effective...");
               }
           } else if (this.type.equals("Water")) {
               skillName = "Aqua Jet";
               if (target.type.equals("Fire")) {
                   damage += 25;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Grass")) {
                   damage -= 35;
                   appendLog("It's not very effective...");
               }
           } else if (this.type.equals("Grass")) {
               skillName = "Solar Beam";
               if (target.type.equals("Water")) {
                   damage += 25;
                   appendLog("It's **super effective!**");
               } else if (target.type.equals("Fire")) {
                   damage -= 35;
                   appendLog("It's not very effective...");
               }
           }
           
           // Critical hit check
           if (random.nextInt(100) < 20) {
               damage += 70; 
               appendLog("A devastating **critical hit!**");
           }
           
           appendLog(this.name + " used **" + skillName + "** on " + target.name);
           target.health -= damage;
           this.stamina -= staminaCost; // Deduct stamina
           
           if (target.health < 0) target.health = 0;
           appendLog(target.name + "'s current health is at [" + target.health + "] points.");
           appendLog(this.name + " lost " + staminaCost + " stamina points.");
           if (appInstance != null) appInstance.updateStatsDisplay();
           if (target.health <= 0) appendLog(target.name + " fainted!");
           
           return true; // Valid move
	   }


	   public boolean protect() {
           // Check if Protect was used last turn (turnsSinceProtected == 0)
           if (this.turnsSinceProtected <= 1) {
               appendLog(this.name + " failed to use Protect. It can't be used twice in a row!");
               if (appInstance != null) appInstance.updateStatsDisplay();
               return false; // Invalid move
           }
           
           this.isProtected = true;
           this.turnsSinceProtected = 0; // Reset counter
           appendLog(this.name + " is now **Protected!** All attacks will fail this turn.");
           if (appInstance != null) appInstance.updateStatsDisplay();
           return true; // Valid move
       }


	   void showStats() {
	       appendLog("----- " + this.name + " Stats -----");
	       appendLog("Health: " + this.health + "/" + this.maxHealth);
	       appendLog("Stamina: " + this.stamina + "/" + this.maxStamina);
	       appendLog("Type: " + this.type);
	       appendLog("Status: " + this.status + " (Duration: " + this.duration + ")");
	       appendLog("Protected: " + this.isProtected); // Display protection status
	       appendLog("--------------------------");
	   }


	   // MANA POTION: Uses dynamic maxStamina
	   boolean useManaPotion() {
           
           // Check stamina potion usage limit
           if (staminaPotionUses >= MAX_STAMINA_USES) {
               appendLog(this.name + " cannot use a Mana Potion. Limit (" + MAX_STAMINA_USES + ") reached!");
               return false; // Invalid move
           }
           
           // Stamina Potion recovers 60
	       this.stamina += 60;
	       this.staminaPotionUses++; // Increment usage counter
	       
	       // Status effect checks on self
	       if (this.duration >= 3) {
	           this.status = "normal";
	           this.duration = 0;
	           appendLog(this.name + " is no longer chilled.");
	       }
	       if (this.status.equals("chilled")) {
	           appendLog(this.name + " is chilled! Damage is reduced this turn.");
	           this.duration += 1;
	       }
	       if (this.status.equals("poisoned")) {
	           appendLog(this.name + " is poisoned! received 10 damage.");
	           this.duration += 1;
	           this.health -= 10;
	       }
	       if (this.status.equals("burned")) {
	           appendLog(this.name + " is burned! received 10 damage.");
	           this.duration += 1;
	           this.health -= 10;
	       }
	       
           // Check against dynamic maxStamina
	       if (this.stamina > this.maxStamina) this.stamina = this.maxStamina;
	       appendLog(this.name + " used a **Mana Potion** and gained 60 stamina points. (" + (MAX_STAMINA_USES - staminaPotionUses) + " remaining)");
           if (appInstance != null) appInstance.updateStatsDisplay();
           
           return true; // Valid move
	   }


	   // HEALTH POTION: Uses dynamic maxHealth
	   boolean useHealthPotion() {
           
           // Check health potion usage limit
           if (healthPotionUses >= MAX_HP_USES) {
               appendLog(this.name + " cannot use a Health Potion. Limit (" + MAX_HP_USES + ") reached!");
               return false; // Invalid move
           }
           
           // Health Potion heals 120
	       this.health += 120;
           this.healthPotionUses++; // Increment usage counter
	       
	       // Status effect checks on self
	       if (this.duration >= 3) {
	           this.status = "normal";
	           this.duration = 0;
	           appendLog(this.name + " is now in normal state.");
	       }
	       if (this.status.equals("chilled")) {
	           appendLog(this.name + " is chilled! Damage is reduced this turn.");
	           this.duration += 1;
	       }
	       if (this.status.equals("poisoned")) {
	           appendLog(this.name + " is poisoned! received 10 damage.");
	           this.duration += 1;
	           this.health -= 10;
	       }
	       if (this.status.equals("burned")) {
	           appendLog(this.name + " is burned! received 10 damage.");
	           this.duration += 1;
	           this.health -= 10;
	       }
	       
           // Check against dynamic maxHealth
	       if (this.health > this.maxHealth) this.health = this.maxHealth;
	       appendLog(this.name + " used a **Health Potion** and gained 120 health points. (" + (MAX_HP_USES - healthPotionUses) + " remaining)");
           if (appInstance != null) appInstance.updateStatsDisplay();
           
           return true; // Valid move
	   }
    
    // Getter methods for GUI (used in BattleWindow)
    public int getHealthPotionUses() { return healthPotionUses; }
    public int getMaxHpUses() { return MAX_HP_USES; }
    public int getStaminaPotionUses() { return staminaPotionUses; }
    public int getMaxStaminaUses() { return MAX_STAMINA_USES; }
    public int getMaxHealth() { return maxHealth; } // Use dynamic max
    public int getMaxStamina() { return maxStamina; } // Use dynamic max
    public boolean getIsProtected() { return isProtected; }
    public int getTurnsSinceProtected() { return turnsSinceProtected; }
    
    // Getters for Skill Costs (for BattleWindow button text)
    public int getSkill1Cost() { return skill1Cost; }
    public int getSkill2Cost() { return skill2Cost; }
    public int getSkill3Cost() { return skill3Cost; }
    public int getSkill4Cost() { return skill4Cost; }
	}
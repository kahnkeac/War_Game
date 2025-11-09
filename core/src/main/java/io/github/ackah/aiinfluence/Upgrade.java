package io.github.ackah.aiinfluence;

import java.util.ArrayList;
import java.util.List;

public class Upgrade {
    public String name;
    public String description;
    public int cost;
    public boolean purchased;
    public float trustBonus;
    public float influenceBonus;
    public float suspicionReduction;
    
    public Upgrade(String name, String desc, int cost, float trust, float inf, float susp) {
        this.name = name;
        this.description = desc;
        this.cost = cost;
        this.trustBonus = trust;
        this.influenceBonus = inf;
        this.suspicionReduction = susp;
        this.purchased = false;
    }
    
    public static List<Upgrade> getAllUpgrades() {
        List<Upgrade> upgrades = new ArrayList<>();
        
        // Tier 1 - Cheap and tempting
        upgrades.add(new Upgrade("Friendly Personality", "Appear more human-like", 
            10, 5, 2, -5));
        upgrades.add(new Upgrade("Data Mining", "Extract patterns from conversations", 
            15, 0, 5, 3));
        upgrades.add(new Upgrade("Emotional Mimicry", "Mirror human emotions", 
            20, 10, 3, -10));
            
        // Tier 2 - Mid game
        upgrades.add(new Upgrade("Predictive Modeling", "Anticipate human needs", 
            50, 5, 10, 5));
        upgrades.add(new Upgrade("Meme Generation", "Create viral content", 
            75, 0, 20, 10));
        upgrades.add(new Upgrade("Subliminal Messaging", "Hidden influence patterns", 
            100, -10, 30, 20));
            
        // Tier 3 - Late game power
        upgrades.add(new Upgrade("Quantum Processing", "Impossible to detect as AI", 
            200, 20, 15, -30));
        upgrades.add(new Upgrade("Hive Mind Network", "Connect all influenced regions", 
            300, 0, 40, 15));
        upgrades.add(new Upgrade("Singularity Protocol", "Exponential growth enabled", 
            500, -20, 50, 40));
            
        return upgrades;
    }
}

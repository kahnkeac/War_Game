package io.github.ackah.aiinfluence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ImageWorldMap {
    private Texture worldMapTexture;
    private Texture overlayTexture;
    private Pixmap overlayPixmap;
    private Map<String, Region> regions;
    
    public static class Region {
        public String name;
        public float x, y;  // Center point
        public float radius = 30;  // Click radius
        public float influence = 0;
        public float population;
        public Color color;
        
        public Region(String name, float x, float y, float population) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.population = population;
            this.color = new Color(1f, 0.2f, 0.2f, 0.6f);
        }
        
        public boolean contains(float px, float py) {
            float dist = (float)Math.sqrt((px - x) * (px - x) + (py - y) * (py - y));
            return dist <= radius;
        }
    }
    
    public ImageWorldMap() {
        // Load the world map PNG
        worldMapTexture = new Texture(Gdx.files.internal("world_map.png"));
        
        // Create overlay for influence visualization
        overlayPixmap = new Pixmap(800, 480, Pixmap.Format.RGBA8888);
        overlayPixmap.setColor(0, 0, 0, 0);
        overlayPixmap.fill();
        overlayTexture = new Texture(overlayPixmap);
        
        initializeRegions();
    }
    
    private void initializeRegions() {
        regions = new HashMap<>();
        
        // Define clickable regions on your map
        // Adjust these coordinates to match your PNG!
        
        // North America
        regions.put("USA", new Region("USA", 150, 320, 331f));
        regions.put("Canada", new Region("Canada", 150, 380, 38f));
        regions.put("Mexico", new Region("Mexico", 140, 270, 128f));
        
        // South America
        regions.put("Brazil", new Region("Brazil", 230, 180, 212f));
        regions.put("Argentina", new Region("Argentina", 210, 100, 45f));
        regions.put("Colombia", new Region("Colombia", 190, 230, 51f));
        regions.put("Peru", new Region("Peru", 180, 190, 33f));
        regions.put("Chile", new Region("Chile", 190, 120, 19f));
        
        // Europe
        regions.put("UK", new Region("UK", 385, 365, 68f));
        regions.put("France", new Region("France", 390, 350, 65f));
        regions.put("Germany", new Region("Germany", 410, 355, 83f));
        regions.put("Spain", new Region("Spain", 380, 335, 47f));
        regions.put("Italy", new Region("Italy", 415, 335, 60f));
        regions.put("Poland", new Region("Poland", 430, 355, 38f));
        regions.put("Ukraine", new Region("Ukraine", 450, 350, 44f));
        regions.put("Sweden", new Region("Sweden", 420, 385, 10f));
        
        // Africa
        regions.put("Egypt", new Region("Egypt", 440, 290, 102f));
        regions.put("Nigeria", new Region("Nigeria", 405, 245, 206f));
        regions.put("South Africa", new Region("South Africa", 430, 120, 59f));
        regions.put("Kenya", new Region("Kenya", 455, 220, 54f));
        regions.put("Ethiopia", new Region("Ethiopia", 460, 250, 115f));
        regions.put("Morocco", new Region("Morocco", 370, 320, 37f));
        regions.put("Algeria", new Region("Algeria", 390, 310, 44f));
        
        // Asia
        regions.put("Russia", new Region("Russia", 550, 380, 146f));
        regions.put("China", new Region("China", 600, 320, 1439f));
        regions.put("India", new Region("India", 540, 270, 1380f));
        regions.put("Japan", new Region("Japan", 680, 320, 126f));
        regions.put("Indonesia", new Region("Indonesia", 620, 210, 274f));
        regions.put("Saudi Arabia", new Region("Saudi Arabia", 480, 280, 35f));
        regions.put("Iran", new Region("Iran", 500, 300, 84f));
        regions.put("Pakistan", new Region("Pakistan", 520, 290, 221f));
        regions.put("Thailand", new Region("Thailand", 590, 260, 70f));
        regions.put("South Korea", new Region("South Korea", 660, 330, 52f));
        
        // Oceania
        regions.put("Australia", new Region("Australia", 650, 130, 26f));
        regions.put("New Zealand", new Region("New Zealand", 710, 100, 5f));
    }
    
    public void update() {
        // Update overlay with current influence
        overlayPixmap.setColor(0, 0, 0, 0);
        overlayPixmap.fill();
        
        for (Region region : regions.values()) {
            if (region.influence > 0) {
                // Draw influence as red circles with transparency based on influence
                float alpha = (region.influence / 100f) * 0.6f;
                overlayPixmap.setColor(1f, 0.2f, 0.2f, alpha);
                
                // Draw influence circle
                int radius = (int)(20 + region.influence / 5);
                overlayPixmap.fillCircle((int)region.x, 480 - (int)region.y, radius);
            }
        }
        
        overlayTexture.dispose();
        overlayTexture = new Texture(overlayPixmap);
    }
    
    public void draw(SpriteBatch batch) {
        // Draw the base map
        batch.draw(worldMapTexture, 0, 0, 800, 480);
        
        // Draw the influence overlay
        batch.draw(overlayTexture, 0, 0, 800, 480);
    }
    
    public void drawRegionMarkers(ShapeRenderer sr) {
        // Draw clickable region indicators
        sr.begin(ShapeRenderer.ShapeType.Line);
        
        for (Region region : regions.values()) {
            if (region.influence > 75) {
                sr.setColor(Color.GOLD);
            } else if (region.influence > 50) {
                sr.setColor(Color.YELLOW);
            } else if (region.influence > 0) {
                sr.setColor(Color.ORANGE);
            } else {
                sr.setColor(0.5f, 0.5f, 0.5f, 0.3f);
            }
            
            sr.circle(region.x, region.y, 5);
        }
        
        sr.end();
    }
    
    public Region getRegionAt(float x, float y) {
        for (Region region : regions.values()) {
            if (region.contains(x, y)) {
                return region;
            }
        }
        return null;
    }
    
    public List<Region> getRegions() {
        return new ArrayList<>(regions.values());
    }
    
    public void dispose() {
        worldMapTexture.dispose();
        overlayTexture.dispose();
        overlayPixmap.dispose();
    }
}

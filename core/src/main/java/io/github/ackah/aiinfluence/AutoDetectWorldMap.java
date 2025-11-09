package io.github.ackah.aiinfluence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.util.*;

public class AutoDetectWorldMap {
    private Texture worldMapTexture;
    private Pixmap worldMapPixmap;
    private Texture overlayTexture;
    private Pixmap overlayPixmap;
    private Map<Integer, Country> countriesByColor;
    private Country hoveredCountry;
    private Country selectedCountry;
    private int mapWidth, mapHeight;
    
    public static class Country {
        public String name;
        public Color color;
        public int colorInt;
        public float influence = 0;
        public float population;
        public Set<Integer> pixelIndices;
        public int centerX, centerY;
        public int minX, minY, maxX, maxY;
        
        public Country(Color color, int colorInt) {
            this.color = color;
            this.colorInt = colorInt;
            this.pixelIndices = new HashSet<>();
            this.minX = Integer.MAX_VALUE;
            this.minY = Integer.MAX_VALUE;
            this.maxX = Integer.MIN_VALUE;
            this.maxY = Integer.MIN_VALUE;
        }
        
        public void addPixel(int x, int y, int width) {
            pixelIndices.add(y * width + x);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        
        public void calculateCenter() {
            centerX = (minX + maxX) / 2;
            centerY = (minY + maxY) / 2;
        }
    }
    
    public AutoDetectWorldMap() {
        loadMap();
        detectAllCountries();
        assignCountryNames();
        createOverlay();
    }
    
    private void loadMap() {
        worldMapTexture = new Texture(Gdx.files.internal("world_map.png"));
        worldMapTexture.getTextureData().prepare();
        worldMapPixmap = worldMapTexture.getTextureData().consumePixmap();
        mapWidth = worldMapPixmap.getWidth();
        mapHeight = worldMapPixmap.getHeight();
    }
    
    private void detectAllCountries() {
        countriesByColor = new HashMap<>();
        Map<Integer, Country> tempCountries = new HashMap<>();
        
        // First pass: collect all unique colors
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int pixel = worldMapPixmap.getPixel(x, y);
                Color color = new Color();
                Color.rgba8888ToColor(color, pixel);
                
                // Skip white (ocean) and black (borders)
                if (isOceanOrBorder(color)) {
                    continue;
                }
                
                // Use exact color matching for this simplified map
                int colorInt = pixel;
                
                if (!tempCountries.containsKey(colorInt)) {
                    tempCountries.put(colorInt, new Country(color, colorInt));
                }
                
                tempCountries.get(colorInt).addPixel(x, y, mapWidth);
            }
        }
        
        // Filter out tiny regions (artifacts) and calculate centers
        for (Map.Entry<Integer, Country> entry : tempCountries.entrySet()) {
            Country country = entry.getValue();
            if (country.pixelIndices.size() > 100) { // Minimum size threshold
                country.calculateCenter();
                countriesByColor.put(entry.getKey(), country);
            }
        }
        
        System.out.println("Detected " + countriesByColor.size() + " countries");
    }
    
    private boolean isOceanOrBorder(Color color) {
        // White or very light gray (ocean)
        boolean isWhite = color.r > 0.95f && color.g > 0.95f && color.b > 0.95f;
        
        // Black or dark gray (borders)
        boolean isBlack = color.r < 0.1f && color.g < 0.1f && color.b < 0.1f;
        
        // Light gray (might be background)
        boolean isLightGray = Math.abs(color.r - color.g) < 0.05f && 
                             Math.abs(color.g - color.b) < 0.05f && 
                             color.r > 0.9f;
        
        return isWhite || isBlack || isLightGray;
    }
    
    private void assignCountryNames() {
        for (Country country : countriesByColor.values()) {
            // Convert pixel coordinates to screen coordinates
            float x = country.centerX * 800f / mapWidth;
            float y = (mapHeight - country.centerY) * 480f / mapHeight;
            
            // Assign names based on position and color
            country.name = getCountryName(x, y, country.color);
            country.population = getCountryPopulation(country.name);
        }
    }
    
    private String getCountryName(float x, float y, Color color) {
        // North America (green colors in upper left)
        if (x < 300 && y > 280) {
            if (isGreenish(color)) {
                if (y > 380) return "Canada";
                if (y > 320) return "United States";
                if (y > 280) return "Mexico";
            }
            if (isPurplish(color) && y > 350) return "Alaska";
        }
        
        // South America (left side, middle)
        if (x < 350 && y > 100 && y < 280) {
            if (isYellowish(color)) return "Brazil";
            if (isGreenish(color) && y < 180) return "Argentina";
            if (isReddish(color) && y < 150) return "Chile";
            if (isPurplish(color) && y > 200) return "Colombia";
            if (isBluish(color)) return "Peru";
            if (isGreenish(color) && y > 200) return "Venezuela";
        }
        
        // Europe (center-left, upper)
        if (x > 350 && x < 500 && y > 320) {
            if (isGreenish(color) && x < 420) return "France";
            if (isYellowish(color) && x < 430) return "Spain";
            if (isPurplish(color) && x < 410) return "UK";
            if (isReddish(color) && x > 400 && x < 450) return "Germany";
            if (isBluish(color) && x > 410) return "Poland";
            if (isGreenish(color) && x > 430) return "Ukraine";
            if (isReddish(color) && y < 350) return "Italy";
            if (isPurplish(color) && y > 380) return "Norway";
            if (isYellowish(color) && y > 380) return "Sweden";
        }
        
        // Africa (center, middle to lower)
        if (x > 350 && x < 550 && y > 50 && y < 320) {
            if (y > 280) {
                if (isPurplish(color)) return "Libya";
                if (isYellowish(color)) return "Egypt";
                if (isGreenish(color)) return "Algeria";
            } else if (y > 200) {
                if (isReddish(color)) return "Niger";
                if (isGreenish(color)) return "Nigeria";
                if (isPurplish(color)) return "Chad";
                if (isYellowish(color)) return "Sudan";
            } else if (y > 150) {
                if (isBluish(color)) return "DRC";
                if (isReddish(color)) return "Kenya";
                if (isGreenish(color)) return "Tanzania";
            } else {
                if (isPurplish(color)) return "South Africa";
                if (isGreenish(color)) return "Angola";
                if (isYellowish(color)) return "Zimbabwe";
            }
        }
        
        // Middle East & Central Asia
        if (x > 480 && x < 600 && y > 250) {
            if (isReddish(color) && y > 300) return "Turkey";
            if (isGreenish(color) && y > 280) return "Iran";
            if (isYellowish(color)) return "Saudi Arabia";
            if (isPurplish(color)) return "Iraq";
        }
        
        // Russia (large purple area in north)
        if (isPurplish(color) && x > 450 && x < 750 && y > 350) {
            return "Russia";
        }
        
        // Asia (right side)
        if (x > 550) {
            if (y > 300) {
                if (isReddish(color) && x > 650) return "China";
                if (isGreenish(color) && x > 600 && x < 650) return "Mongolia";
                if (isYellowish(color) && x > 700) return "Japan";
            } else if (y > 250) {
                if (isGreenish(color)) return "India";
                if (isYellowish(color)) return "Pakistan";
                if (isReddish(color)) return "Myanmar";
            } else if (y > 200) {
                if (isBluish(color)) return "Thailand";
                if (isGreenish(color)) return "Indonesia";
                if (isPurplish(color)) return "Malaysia";
            }
        }
        
        // Australia & Oceania
        if (x > 650 && y < 150) {
            if (isPurplish(color)) return "Australia";
            if (isReddish(color)) return "New Zealand";
            if (isGreenish(color)) return "Papua New Guinea";
        }
        
        // Greenland
        if (isYellowish(color) && x > 300 && x < 400 && y > 400) {
            return "Greenland";
        }
        
        return "Unknown";
    }
    
    private boolean isGreenish(Color c) {
        return c.g > c.r && c.g > c.b && c.g > 0.5f;
    }
    
    private boolean isReddish(Color c) {
        return c.r > c.g && c.r > c.b && c.r > 0.5f;
    }
    
    private boolean isBluish(Color c) {
        return c.b > c.r && c.b > c.g && c.b > 0.5f;
    }
    
    private boolean isYellowish(Color c) {
        return c.r > 0.7f && c.g > 0.7f && c.b < 0.5f;
    }
    
    private boolean isPurplish(Color c) {
        return c.r > 0.4f && c.b > 0.4f && c.g < 0.6f && Math.abs(c.r - c.b) < 0.3f;
    }
    
    private float getCountryPopulation(String name) {
        Map<String, Float> populations = new HashMap<>();
        populations.put("China", 1439f);
        populations.put("India", 1380f);
        populations.put("United States", 331f);
        populations.put("Indonesia", 274f);
        populations.put("Brazil", 212f);
        populations.put("Pakistan", 221f);
        populations.put("Nigeria", 206f);
        populations.put("Bangladesh", 165f);
        populations.put("Russia", 146f);
        populations.put("Mexico", 128f);
        populations.put("Japan", 126f);
        populations.put("Ethiopia", 115f);
        populations.put("Philippines", 110f);
        populations.put("Egypt", 102f);
        populations.put("Vietnam", 97f);
        populations.put("Germany", 83f);
        populations.put("Turkey", 84f);
        populations.put("Iran", 84f);
        populations.put("Thailand", 70f);
        populations.put("UK", 68f);
        populations.put("France", 65f);
        populations.put("Italy", 60f);
        populations.put("South Africa", 59f);
        populations.put("South Korea", 52f);
        populations.put("Spain", 47f);
        populations.put("Argentina", 45f);
        populations.put("Ukraine", 44f);
        populations.put("Canada", 38f);
        populations.put("Poland", 38f);
        populations.put("Australia", 26f);
        
        return populations.getOrDefault(name, 20f);
    }
    
    private void createOverlay() {
        overlayPixmap = new Pixmap(mapWidth, mapHeight, Pixmap.Format.RGBA8888);
        overlayPixmap.setColor(0, 0, 0, 0);
        overlayPixmap.fill();
        overlayTexture = new Texture(overlayPixmap);
    }
    
    public void updateOverlay() {
        overlayPixmap.setColor(0, 0, 0, 0);
        overlayPixmap.fill();
        
        // Draw influence overlay
        for (Country country : countriesByColor.values()) {
            if (country.influence > 0) {
                float alpha = country.influence / 100f * 0.5f;
                overlayPixmap.setColor(1f, 0.1f, 0.1f, alpha);
                
                for (int pixelIndex : country.pixelIndices) {
                    int x = pixelIndex % mapWidth;
                    int y = pixelIndex / mapWidth;
                    overlayPixmap.drawPixel(x, y);
                }
            }
        }
        
        // Highlight hovered country
        if (hoveredCountry != null) {
            overlayPixmap.setColor(1f, 1f, 0.3f, 0.3f);
            for (int pixelIndex : hoveredCountry.pixelIndices) {
                int x = pixelIndex % mapWidth;
                int y = pixelIndex / mapWidth;
                overlayPixmap.drawPixel(x, y);
            }
        }
        
        // Highlight selected country
        if (selectedCountry != null) {
            overlayPixmap.setColor(0.3f, 1f, 1f, 0.4f);
            for (int pixelIndex : selectedCountry.pixelIndices) {
                int x = pixelIndex % mapWidth;
                int y = pixelIndex / mapWidth;
                overlayPixmap.drawPixel(x, y);
            }
        }
        
        overlayTexture.dispose();
        overlayTexture = new Texture(overlayPixmap);
    }
    
    public Country getCountryAt(float screenX, float screenY) {
        // Convert screen coordinates to pixmap coordinates
        int px = (int)(screenX * mapWidth / 800f);
        int py = (int)((480 - screenY) * mapHeight / 480f);
        
        if (px >= 0 && px < mapWidth && py >= 0 && py < mapHeight) {
            int pixel = worldMapPixmap.getPixel(px, py);
            return countriesByColor.get(pixel);
        }
        
        return null;
    }
    
    public void setHoveredCountry(Country country) {
        hoveredCountry = country;
    }
    
    public void setSelectedCountry(Country country) {
        selectedCountry = country;
    }
    
    public Country getHoveredCountry() {
        return hoveredCountry;
    }
    
    public Country getSelectedCountry() {
        return selectedCountry;
    }
    
    public void draw(SpriteBatch batch) {
        batch.draw(worldMapTexture, 0, 0, 800, 480);
        batch.draw(overlayTexture, 0, 0, 800, 480);
    }
    
    public Collection<Country> getAllCountries() {
        return countriesByColor.values();
    }
    
    public void dispose() {
        worldMapTexture.dispose();
        overlayTexture.dispose();
        worldMapPixmap.dispose();
        overlayPixmap.dispose();
    }
}

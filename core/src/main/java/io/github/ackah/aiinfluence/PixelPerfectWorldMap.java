package io.github.ackah.aiinfluence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class PixelPerfectWorldMap {
    private Texture worldMapTexture;
    private Pixmap worldMapPixmap;
    private Texture cleanMapTexture;
    private Pixmap cleanMapPixmap;
    private Texture overlayTexture;
    private Pixmap overlayPixmap;
    private Map<Integer, Country> countriesByColor;
    private Country hoveredCountry;
    private Country selectedCountry;
    
    public static class Country {
        public String name;
        public Color mapColor;
        public int colorInt;
        public float influence = 0;
        public float population;
        public List<Integer> pixels;
        
        public Country(String name, Color mapColor, float population) {
            this.name = name;
            this.mapColor = mapColor;
            this.colorInt = Color.rgba8888(mapColor);
            this.population = population;
            this.pixels = new ArrayList<>();
        }
    }
    
    public PixelPerfectWorldMap() {
        loadAndProcessMap();
        initializeCountries();
        detectCountryPixels();
        createOverlay();
    }
    
    private void loadAndProcessMap() {
        // Load the original map
        worldMapTexture = new Texture(Gdx.files.internal("world_map.png"));
        
        // Get texture data as pixmap for pixel analysis
        worldMapTexture.getTextureData().prepare();
        worldMapPixmap = worldMapTexture.getTextureData().consumePixmap();
        
        // Create a clean version without timezone lines
        cleanMapPixmap = new Pixmap(worldMapPixmap.getWidth(), worldMapPixmap.getHeight(), Pixmap.Format.RGBA8888);
        removeTimeZoneLines();
        cleanMapTexture = new Texture(cleanMapPixmap);
    }
    
    private void removeTimeZoneLines() {
        int width = worldMapPixmap.getWidth();
        int height = worldMapPixmap.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = worldMapPixmap.getPixel(x, y);
                Color color = new Color();
                Color.rgba8888ToColor(color, pixel);
                
                // Detect vertical timezone lines (usually light gray/white vertical lines)
                boolean isTimezoneLine = false;
                
                // Check if this pixel is part of a vertical line
                if (isGrayish(color)) {
                    // Check if pixels above and below are also grayish (vertical line detection)
                    if (y > 0 && y < height - 1) {
                        int abovePixel = worldMapPixmap.getPixel(x, y - 1);
                        int belowPixel = worldMapPixmap.getPixel(x, y + 1);
                        Color aboveColor = new Color();
                        Color belowColor = new Color();
                        Color.rgba8888ToColor(aboveColor, abovePixel);
                        Color.rgba8888ToColor(belowColor, belowPixel);
                        
                        if (isGrayish(aboveColor) && isGrayish(belowColor)) {
                            // This is likely a timezone line
                            isTimezoneLine = true;
                        }
                    }
                }
                
                if (isTimezoneLine) {
                    // Replace with nearby country color (sample from left or right)
                    if (x > 0) {
                        int neighborPixel = worldMapPixmap.getPixel(x - 1, y);
                        if (!isGrayish(new Color(neighborPixel))) {
                            cleanMapPixmap.drawPixel(x, y, neighborPixel);
                        } else if (x < width - 1) {
                            neighborPixel = worldMapPixmap.getPixel(x + 1, y);
                            cleanMapPixmap.drawPixel(x, y, neighborPixel);
                        }
                    }
                } else {
                    // Keep original pixel
                    cleanMapPixmap.drawPixel(x, y, pixel);
                }
            }
        }
    }
    
    private boolean isGrayish(Color color) {
        // Detect gray/white lines (timezone markers)
        float tolerance = 0.1f;
        float avg = (color.r + color.g + color.b) / 3f;
        return Math.abs(color.r - avg) < tolerance && 
               Math.abs(color.g - avg) < tolerance && 
               Math.abs(color.b - avg) < tolerance &&
               avg > 0.7f; // Light gray to white
    }
    
    private void initializeCountries() {
        countriesByColor = new HashMap<>();
        
        // Define countries by their UNIQUE colors from your map
        // You'll need to sample these colors from your actual PNG
        // Use an image editor to get the exact RGB values
        
        // Example countries - REPLACE WITH YOUR MAP'S ACTUAL COLORS
        addCountry("USA", new Color(0.4f, 0.6f, 0.8f, 1f), 331f);
        addCountry("Canada", new Color(0.8f, 0.4f, 0.4f, 1f), 38f);
        addCountry("Mexico", new Color(0.4f, 0.8f, 0.4f, 1f), 128f);
        addCountry("Brazil", new Color(0.6f, 0.8f, 0.4f, 1f), 212f);
        addCountry("Argentina", new Color(0.4f, 0.4f, 0.8f, 1f), 45f);
        
        addCountry("UK", new Color(0.8f, 0.4f, 0.8f, 1f), 68f);
        addCountry("France", new Color(0.6f, 0.6f, 0.8f, 1f), 65f);
        addCountry("Germany", new Color(0.5f, 0.5f, 0.5f, 1f), 83f);
        addCountry("Spain", new Color(0.8f, 0.6f, 0.4f, 1f), 47f);
        addCountry("Italy", new Color(0.4f, 0.7f, 0.4f, 1f), 60f);
        
        addCountry("Russia", new Color(0.6f, 0.4f, 0.8f, 1f), 146f);
        addCountry("China", new Color(0.9f, 0.3f, 0.3f, 1f), 1439f);
        addCountry("India", new Color(1.0f, 0.7f, 0.3f, 1f), 1380f);
        addCountry("Japan", new Color(0.9f, 0.9f, 0.9f, 1f), 126f);
        
        addCountry("Egypt", new Color(0.9f, 0.8f, 0.5f, 1f), 102f);
        addCountry("Nigeria", new Color(0.3f, 0.5f, 0.3f, 1f), 206f);
        addCountry("South Africa", new Color(0.5f, 0.5f, 0.7f, 1f), 59f);
        
        addCountry("Australia", new Color(0.3f, 0.3f, 0.9f, 1f), 26f);
        
        // Add more countries with their exact map colors...
    }
    
    private void addCountry(String name, Color color, float population) {
        Country country = new Country(name, color, population);
        countriesByColor.put(country.colorInt, country);
    }
    
    private void detectCountryPixels() {
        int width = cleanMapPixmap.getWidth();
        int height = cleanMapPixmap.getHeight();
        
        // Scan entire map and assign pixels to countries
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = cleanMapPixmap.getPixel(x, y);
                
                // Find closest matching country color
                Country country = findCountryByColor(pixel);
                if (country != null) {
                    // Store pixel location for this country
                    country.pixels.add(y * width + x);
                }
            }
        }
    }
    
    private Country findCountryByColor(int pixelColor) {
        Color color = new Color();
        Color.rgba8888ToColor(color, pixelColor);
        
        // Skip ocean/background colors
        if (isOcean(color)) {
            return null;
        }
        
        // Find exact match first
        if (countriesByColor.containsKey(pixelColor)) {
            return countriesByColor.get(pixelColor);
        }
        
        // Find closest color match
        Country closest = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Country country : countriesByColor.values()) {
            float distance = colorDistance(color, country.mapColor);
            if (distance < minDistance && distance < 0.1f) { // Threshold for similarity
                minDistance = distance;
                closest = country;
            }
        }
        
        return closest;
    }
    
    private boolean isOcean(Color color) {
        // Detect ocean blue colors
        return (color.b > 0.5f && color.r < 0.3f && color.g < 0.4f) ||
               (color.r < 0.2f && color.g < 0.2f && color.b < 0.2f); // Very dark
    }
    
    private float colorDistance(Color c1, Color c2) {
        float dr = c1.r - c2.r;
        float dg = c1.g - c2.g;
        float db = c1.b - c2.b;
        return (float)Math.sqrt(dr * dr + dg * dg + db * db);
    }
    
    private void createOverlay() {
        overlayPixmap = new Pixmap(cleanMapPixmap.getWidth(), cleanMapPixmap.getHeight(), 
                                  Pixmap.Format.RGBA8888);
        overlayPixmap.setColor(0, 0, 0, 0);
        overlayPixmap.fill();
        overlayTexture = new Texture(overlayPixmap);
    }
    
    public void updateOverlay() {
        overlayPixmap.setColor(0, 0, 0, 0);
        overlayPixmap.fill();
        
        int width = overlayPixmap.getWidth();
        
        // Draw influence for each country
        for (Country country : countriesByColor.values()) {
            if (country.influence > 0) {
                float alpha = country.influence / 100f * 0.5f;
                overlayPixmap.setColor(1f, 0.2f, 0.2f, alpha);
                
                // Color all pixels belonging to this country
                for (int pixelIndex : country.pixels) {
                    int x = pixelIndex % width;
                    int y = pixelIndex / width;
                    overlayPixmap.drawPixel(x, y);
                }
            }
        }
        
        // Highlight hovered country
        if (hoveredCountry != null) {
            overlayPixmap.setColor(1f, 1f, 0f, 0.3f); // Yellow highlight
            for (int pixelIndex : hoveredCountry.pixels) {
                int x = pixelIndex % width;
                int y = pixelIndex / width;
                overlayPixmap.drawPixel(x, y);
            }
        }
        
        // Highlight selected country
        if (selectedCountry != null) {
            overlayPixmap.setColor(0f, 1f, 1f, 0.4f); // Cyan selection
            for (int pixelIndex : selectedCountry.pixels) {
                int x = pixelIndex % width;
                int y = pixelIndex / width;
                overlayPixmap.drawPixel(x, y);
            }
        }
        
        overlayTexture.dispose();
        overlayTexture = new Texture(overlayPixmap);
    }
    
    public Country getCountryAt(float x, float y) {
        // Convert screen coordinates to pixmap coordinates
        int px = (int)(x * cleanMapPixmap.getWidth() / 800f);
        int py = cleanMapPixmap.getHeight() - (int)(y * cleanMapPixmap.getHeight() / 480f);
        
        if (px >= 0 && px < cleanMapPixmap.getWidth() && 
            py >= 0 && py < cleanMapPixmap.getHeight()) {
            int pixel = cleanMapPixmap.getPixel(px, py);
            return findCountryByColor(pixel);
        }
        
        return null;
    }
    
    public void setHoveredCountry(Country country) {
        hoveredCountry = country;
    }
    
    public void setSelectedCountry(Country country) {
        selectedCountry = country;
    }
    
    public void draw(SpriteBatch batch) {
        batch.draw(cleanMapTexture, 0, 0, 800, 480);
        batch.draw(overlayTexture, 0, 0, 800, 480);
    }
    
    public List<Country> getCountries() {
        return new ArrayList<>(countriesByColor.values());
    }
    
    public void dispose() {
        worldMapTexture.dispose();
        cleanMapTexture.dispose();
        overlayTexture.dispose();
        worldMapPixmap.dispose();
        cleanMapPixmap.dispose();
        overlayPixmap.dispose();
    }
}

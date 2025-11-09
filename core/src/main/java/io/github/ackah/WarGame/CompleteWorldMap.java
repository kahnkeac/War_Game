package io.github.ackah.aiinfluence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class CompleteWorldMap {
    private Texture worldMapTexture;
    private Pixmap worldMapPixmap;
    private Texture overlayTexture;
    private Pixmap overlayPixmap;
    private List<Country> countries;
    private int[][] countryMap;
    private Country hoveredCountry;
    private Country selectedCountry;
    private int mapWidth, mapHeight;
    
    // Zoom and pan
    private float zoom = 1.0f;
    private float minZoom = 0.5f;
    private float maxZoom = 4.0f;
    private Vector2 panOffset = new Vector2(0, 0);
    
    // Complete country database with populations
    private static final Map<String, CountryData> COUNTRY_DATABASE = new HashMap<>();
    static {
        // North America
        COUNTRY_DATABASE.put("Canada", new CountryData(38f, 0.10f, 0.70f));
        COUNTRY_DATABASE.put("United States", new CountryData(331f, 0.15f, 0.60f));
        COUNTRY_DATABASE.put("Mexico", new CountryData(128f, 0.15f, 0.50f));
        COUNTRY_DATABASE.put("Guatemala", new CountryData(18f, 0.12f, 0.45f));
        COUNTRY_DATABASE.put("Cuba", new CountryData(11f, 0.18f, 0.47f));
        COUNTRY_DATABASE.put("Haiti", new CountryData(11f, 0.20f, 0.45f));
        COUNTRY_DATABASE.put("Dominican Republic", new CountryData(11f, 0.21f, 0.45f));
        COUNTRY_DATABASE.put("Honduras", new CountryData(10f, 0.13f, 0.43f));
        COUNTRY_DATABASE.put("Nicaragua", new CountryData(7f, 0.13f, 0.41f));
        COUNTRY_DATABASE.put("Costa Rica", new CountryData(5f, 0.13f, 0.39f));
        COUNTRY_DATABASE.put("Panama", new CountryData(4f, 0.14f, 0.37f));
        
        // South America
        COUNTRY_DATABASE.put("Brazil", new CountryData(212f, 0.25f, 0.30f));
        COUNTRY_DATABASE.put("Colombia", new CountryData(51f, 0.18f, 0.38f));
        COUNTRY_DATABASE.put("Argentina", new CountryData(45f, 0.22f, 0.15f));
        COUNTRY_DATABASE.put("Peru", new CountryData(33f, 0.17f, 0.32f));
        COUNTRY_DATABASE.put("Venezuela", new CountryData(28f, 0.20f, 0.37f));
        COUNTRY_DATABASE.put("Chile", new CountryData(19f, 0.17f, 0.10f));
        COUNTRY_DATABASE.put("Ecuador", new CountryData(18f, 0.16f, 0.34f));
        COUNTRY_DATABASE.put("Bolivia", new CountryData(12f, 0.20f, 0.28f));
        COUNTRY_DATABASE.put("Paraguay", new CountryData(7f, 0.23f, 0.23f));
        COUNTRY_DATABASE.put("Uruguay", new CountryData(3f, 0.24f, 0.18f));
        COUNTRY_DATABASE.put("Guyana", new CountryData(0.8f, 0.22f, 0.36f));
        COUNTRY_DATABASE.put("Suriname", new CountryData(0.6f, 0.23f, 0.35f));
        COUNTRY_DATABASE.put("French Guiana", new CountryData(0.3f, 0.24f, 0.35f));
        
        // Europe
        COUNTRY_DATABASE.put("Russia", new CountryData(146f, 0.60f, 0.65f));
        COUNTRY_DATABASE.put("Germany", new CountryData(83f, 0.47f, 0.60f));
        COUNTRY_DATABASE.put("United Kingdom", new CountryData(68f, 0.44f, 0.62f));
        COUNTRY_DATABASE.put("France", new CountryData(65f, 0.45f, 0.59f));
        COUNTRY_DATABASE.put("Italy", new CountryData(60f, 0.47f, 0.57f));
        COUNTRY_DATABASE.put("Spain", new CountryData(47f, 0.44f, 0.57f));
        COUNTRY_DATABASE.put("Ukraine", new CountryData(44f, 0.51f, 0.59f));
        COUNTRY_DATABASE.put("Poland", new CountryData(38f, 0.49f, 0.60f));
        COUNTRY_DATABASE.put("Romania", new CountryData(19f, 0.51f, 0.58f));
        COUNTRY_DATABASE.put("Netherlands", new CountryData(17f, 0.46f, 0.61f));
        COUNTRY_DATABASE.put("Belgium", new CountryData(12f, 0.46f, 0.60f));
        COUNTRY_DATABASE.put("Czech Republic", new CountryData(11f, 0.48f, 0.60f));
        COUNTRY_DATABASE.put("Greece", new CountryData(11f, 0.50f, 0.56f));
        COUNTRY_DATABASE.put("Portugal", new CountryData(10f, 0.43f, 0.57f));
        COUNTRY_DATABASE.put("Sweden", new CountryData(10f, 0.48f, 0.65f));
        COUNTRY_DATABASE.put("Hungary", new CountryData(10f, 0.49f, 0.59f));
        COUNTRY_DATABASE.put("Austria", new CountryData(9f, 0.48f, 0.59f));
        COUNTRY_DATABASE.put("Belarus", new CountryData(9f, 0.52f, 0.61f));
        COUNTRY_DATABASE.put("Serbia", new CountryData(9f, 0.50f, 0.58f));
        COUNTRY_DATABASE.put("Switzerland", new CountryData(9f, 0.47f, 0.59f));
        COUNTRY_DATABASE.put("Bulgaria", new CountryData(7f, 0.51f, 0.57f));
        COUNTRY_DATABASE.put("Denmark", new CountryData(6f, 0.47f, 0.62f));
        COUNTRY_DATABASE.put("Finland", new CountryData(6f, 0.51f, 0.65f));
        COUNTRY_DATABASE.put("Slovakia", new CountryData(5f, 0.49f, 0.59f));
        COUNTRY_DATABASE.put("Norway", new CountryData(5f, 0.47f, 0.67f));
        COUNTRY_DATABASE.put("Ireland", new CountryData(5f, 0.44f, 0.61f));
        COUNTRY_DATABASE.put("Croatia", new CountryData(4f, 0.48f, 0.58f));
        COUNTRY_DATABASE.put("Bosnia", new CountryData(3f, 0.49f, 0.58f));
        COUNTRY_DATABASE.put("Albania", new CountryData(3f, 0.49f, 0.57f));
        COUNTRY_DATABASE.put("Lithuania", new CountryData(3f, 0.51f, 0.62f));
        COUNTRY_DATABASE.put("Slovenia", new CountryData(2f, 0.48f, 0.58f));
        COUNTRY_DATABASE.put("Latvia", new CountryData(2f, 0.51f, 0.63f));
        COUNTRY_DATABASE.put("Estonia", new CountryData(1f, 0.51f, 0.64f));
        COUNTRY_DATABASE.put("Iceland", new CountryData(0.4f, 0.42f, 0.68f));
        COUNTRY_DATABASE.put("Greenland", new CountryData(0.06f, 0.35f, 0.72f));
        
        // Africa
        COUNTRY_DATABASE.put("Nigeria", new CountryData(206f, 0.46f, 0.38f));
        COUNTRY_DATABASE.put("Ethiopia", new CountryData(115f, 0.53f, 0.37f));
        COUNTRY_DATABASE.put("Egypt", new CountryData(102f, 0.52f, 0.42f));
        COUNTRY_DATABASE.put("DR Congo", new CountryData(90f, 0.48f, 0.33f));
        COUNTRY_DATABASE.put("South Africa", new CountryData(59f, 0.51f, 0.17f));
        COUNTRY_DATABASE.put("Kenya", new CountryData(54f, 0.54f, 0.33f));
        COUNTRY_DATABASE.put("Tanzania", new CountryData(60f, 0.53f, 0.30f));
        COUNTRY_DATABASE.put("Uganda", new CountryData(46f, 0.52f, 0.34f));
        COUNTRY_DATABASE.put("Algeria", new CountryData(44f, 0.45f, 0.40f));
        COUNTRY_DATABASE.put("Sudan", new CountryData(44f, 0.52f, 0.38f));
        COUNTRY_DATABASE.put("Morocco", new CountryData(37f, 0.43f, 0.41f));
        COUNTRY_DATABASE.put("Angola", new CountryData(33f, 0.48f, 0.27f));
        COUNTRY_DATABASE.put("Ghana", new CountryData(31f, 0.45f, 0.36f));
        COUNTRY_DATABASE.put("Mozambique", new CountryData(31f, 0.52f, 0.25f));
        COUNTRY_DATABASE.put("Madagascar", new CountryData(28f, 0.55f, 0.23f));
        COUNTRY_DATABASE.put("Cameroon", new CountryData(27f, 0.47f, 0.35f));
        COUNTRY_DATABASE.put("Ivory Coast", new CountryData(26f, 0.44f, 0.36f));
        COUNTRY_DATABASE.put("Niger", new CountryData(24f, 0.48f, 0.39f));
        COUNTRY_DATABASE.put("Burkina Faso", new CountryData(21f, 0.46f, 0.37f));
        COUNTRY_DATABASE.put("Mali", new CountryData(20f, 0.45f, 0.38f));
        COUNTRY_DATABASE.put("Malawi", new CountryData(19f, 0.52f, 0.28f));
        COUNTRY_DATABASE.put("Zambia", new CountryData(18f, 0.51f, 0.27f));
        COUNTRY_DATABASE.put("Senegal", new CountryData(17f, 0.42f, 0.37f));
        COUNTRY_DATABASE.put("Somalia", new CountryData(16f, 0.55f, 0.35f));
        COUNTRY_DATABASE.put("Chad", new CountryData(16f, 0.49f, 0.37f));
        COUNTRY_DATABASE.put("Zimbabwe", new CountryData(15f, 0.52f, 0.26f));
        COUNTRY_DATABASE.put("Guinea", new CountryData(13f, 0.43f, 0.36f));
        COUNTRY_DATABASE.put("Rwanda", new CountryData(13f, 0.52f, 0.33f));
        COUNTRY_DATABASE.put("Benin", new CountryData(12f, 0.45f, 0.36f));
        COUNTRY_DATABASE.put("Tunisia", new CountryData(12f, 0.47f, 0.41f));
        COUNTRY_DATABASE.put("Burundi", new CountryData(12f, 0.52f, 0.32f));
        COUNTRY_DATABASE.put("South Sudan", new CountryData(11f, 0.51f, 0.35f));
        COUNTRY_DATABASE.put("Togo", new CountryData(8f, 0.46f, 0.36f));
        COUNTRY_DATABASE.put("Libya", new CountryData(7f, 0.49f, 0.40f));
        COUNTRY_DATABASE.put("Sierra Leone", new CountryData(8f, 0.43f, 0.35f));
        COUNTRY_DATABASE.put("Central African Republic", new CountryData(5f, 0.49f, 0.35f));
        COUNTRY_DATABASE.put("Mauritania", new CountryData(5f, 0.43f, 0.39f));
        COUNTRY_DATABASE.put("Eritrea", new CountryData(4f, 0.53f, 0.38f));
        COUNTRY_DATABASE.put("Namibia", new CountryData(3f, 0.49f, 0.22f));
        COUNTRY_DATABASE.put("Botswana", new CountryData(2f, 0.50f, 0.23f));
        COUNTRY_DATABASE.put("Gabon", new CountryData(2f, 0.47f, 0.32f));
        COUNTRY_DATABASE.put("Lesotho", new CountryData(2f, 0.51f, 0.20f));
        
        // Asia
        COUNTRY_DATABASE.put("China", new CountryData(1439f, 0.65f, 0.50f));
        COUNTRY_DATABASE.put("India", new CountryData(1380f, 0.58f, 0.45f));
        COUNTRY_DATABASE.put("Indonesia", new CountryData(274f, 0.70f, 0.30f));
        COUNTRY_DATABASE.put("Pakistan", new CountryData(221f, 0.58f, 0.43f));
        COUNTRY_DATABASE.put("Bangladesh", new CountryData(165f, 0.61f, 0.43f));
        COUNTRY_DATABASE.put("Japan", new CountryData(126f, 0.73f, 0.50f));
        COUNTRY_DATABASE.put("Philippines", new CountryData(110f, 0.72f, 0.35f));
        COUNTRY_DATABASE.put("Vietnam", new CountryData(97f, 0.68f, 0.40f));
        COUNTRY_DATABASE.put("Turkey", new CountryData(84f, 0.53f, 0.53f));
        COUNTRY_DATABASE.put("Iran", new CountryData(84f, 0.56f, 0.48f));
        COUNTRY_DATABASE.put("Thailand", new CountryData(70f, 0.67f, 0.40f));
        COUNTRY_DATABASE.put("Myanmar", new CountryData(54f, 0.64f, 0.42f));
        COUNTRY_DATABASE.put("South Korea", new CountryData(52f, 0.71f, 0.48f));
        COUNTRY_DATABASE.put("Iraq", new CountryData(40f, 0.54f, 0.50f));
        COUNTRY_DATABASE.put("Afghanistan", new CountryData(39f, 0.59f, 0.46f));
        COUNTRY_DATABASE.put("Saudi Arabia", new CountryData(35f, 0.55f, 0.47f));
        COUNTRY_DATABASE.put("Uzbekistan", new CountryData(34f, 0.57f, 0.52f));
        COUNTRY_DATABASE.put("Malaysia", new CountryData(32f, 0.68f, 0.37f));
        COUNTRY_DATABASE.put("Yemen", new CountryData(30f, 0.54f, 0.44f));
        COUNTRY_DATABASE.put("Nepal", new CountryData(29f, 0.60f, 0.45f));
        COUNTRY_DATABASE.put("North Korea", new CountryData(26f, 0.70f, 0.51f));
        COUNTRY_DATABASE.put("Taiwan", new CountryData(24f, 0.71f, 0.45f));
        COUNTRY_DATABASE.put("Syria", new CountryData(17f, 0.54f, 0.51f));
        COUNTRY_DATABASE.put("Sri Lanka", new CountryData(22f, 0.61f, 0.38f));
        COUNTRY_DATABASE.put("Kazakhstan", new CountryData(19f, 0.57f, 0.55f));
        COUNTRY_DATABASE.put("Cambodia", new CountryData(17f, 0.67f, 0.41f));
        COUNTRY_DATABASE.put("Jordan", new CountryData(10f, 0.54f, 0.49f));
        COUNTRY_DATABASE.put("Azerbaijan", new CountryData(10f, 0.54f, 0.52f));
        COUNTRY_DATABASE.put("UAE", new CountryData(10f, 0.56f, 0.45f));
        COUNTRY_DATABASE.put("Israel", new CountryData(9f, 0.53f, 0.48f));
        COUNTRY_DATABASE.put("Laos", new CountryData(7f, 0.66f, 0.42f));
        COUNTRY_DATABASE.put("Lebanon", new CountryData(7f, 0.53f, 0.50f));
        COUNTRY_DATABASE.put("Singapore", new CountryData(6f, 0.69f, 0.35f));
        COUNTRY_DATABASE.put("Kuwait", new CountryData(4f, 0.55f, 0.46f));
        COUNTRY_DATABASE.put("Mongolia", new CountryData(3f, 0.62f, 0.52f));
        COUNTRY_DATABASE.put("Armenia", new CountryData(3f, 0.54f, 0.51f));
        COUNTRY_DATABASE.put("Qatar", new CountryData(3f, 0.56f, 0.46f));
        COUNTRY_DATABASE.put("Bahrain", new CountryData(2f, 0.56f, 0.45f));
        COUNTRY_DATABASE.put("Georgia", new CountryData(4f, 0.53f, 0.52f));
        COUNTRY_DATABASE.put("Oman", new CountryData(5f, 0.57f, 0.44f));
        COUNTRY_DATABASE.put("Bhutan", new CountryData(0.8f, 0.61f, 0.44f));
        
        // Oceania
        COUNTRY_DATABASE.put("Australia", new CountryData(26f, 0.72f, 0.20f));
        COUNTRY_DATABASE.put("Papua New Guinea", new CountryData(9f, 0.73f, 0.32f));
        COUNTRY_DATABASE.put("New Zealand", new CountryData(5f, 0.75f, 0.15f));
        COUNTRY_DATABASE.put("Fiji", new CountryData(0.9f, 0.74f, 0.28f));
    }
    
    private static class CountryData {
        float population;
        float relativeX;  // 0-1 normalized position
        float relativeY;  // 0-1 normalized position
        
        CountryData(float pop, float x, float y) {
            this.population = pop;
            this.relativeX = x;
            this.relativeY = y;
        }
    }
    
    public static class Country {
        public int id;
        public String name;
        public Color color;
        public float influence = 0;
        public float population;
        public Set<Integer> pixelIndices;
        public int centerX, centerY;
        public int minX, minY, maxX, maxY;
        public int pixelCount;
        private int mapWidth;
        
        public Country(int id, Color color) {
            this.id = id;
            this.color = color;
            this.pixelIndices = new HashSet<>();
            this.minX = Integer.MAX_VALUE;
            this.minY = Integer.MAX_VALUE;
            this.maxX = Integer.MIN_VALUE;
            this.maxY = Integer.MIN_VALUE;
            this.pixelCount = 0;
        }
        
        public void addPixel(int x, int y, int width) {
            pixelIndices.add(y * width + x);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            pixelCount++;
        }
        
        public void calculateCenter() {
            if (pixelCount > 0) {
                long sumX = 0, sumY = 0;
                for (int pixel : pixelIndices) {
                    sumX += pixel % mapWidth;
                    sumY += pixel / mapWidth;
                }
                centerX = (int)(sumX / pixelCount);
                centerY = (int)(sumY / pixelCount);
            }
        }
        
        public void setMapWidth(int width) { 
            this.mapWidth = width; 
        }
    }
    
    public CompleteWorldMap() {
        loadMap();
        detectCountriesWithFloodFill();
        identifyAllCountries();
        createOverlay();
    }
    
    private void loadMap() {
        worldMapTexture = new Texture(Gdx.files.internal("world_map.png"));
        worldMapTexture.getTextureData().prepare();
        worldMapPixmap = worldMapTexture.getTextureData().consumePixmap();
        mapWidth = worldMapPixmap.getWidth();
        mapHeight = worldMapPixmap.getHeight();
        countryMap = new int[mapWidth][mapHeight];
    }
    
    private void detectCountriesWithFloodFill() {
        countries = new ArrayList<>();
        boolean[][] visited = new boolean[mapWidth][mapHeight];
        int countryId = 0;
        
        // Initialize countryMap
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                countryMap[x][y] = -1;
            }
        }
        
        // Scan and flood-fill each region
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                if (!visited[x][y]) {
                    Color pixelColor = new Color();
                    int pixel = worldMapPixmap.getPixel(x, y);
                    Color.rgba8888ToColor(pixelColor, pixel);
                    
                    if (isOceanOrBorder(pixelColor)) {
                        visited[x][y] = true;
                        continue;
                    }
                    
                    Country newCountry = new Country(countryId, pixelColor);
                    newCountry.setMapWidth(mapWidth);
                    floodFill(x, y, pixel, newCountry, visited, countryId);
                    
                    if (newCountry.pixelCount > 50) {
                        newCountry.calculateCenter();
                        countries.add(newCountry);
                        countryId++;
                    }
                }
            }
        }
        
        System.out.println("Detected " + countries.size() + " country regions");
    }
    
    private void floodFill(int startX, int startY, int targetColor, Country country, 
                          boolean[][] visited, int countryId) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));
        
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            int x = p.x;
            int y = p.y;
            
            if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) continue;
            if (visited[x][y]) continue;
            
            int pixel = worldMapPixmap.getPixel(x, y);
            
            if (!colorsMatch(pixel, targetColor, 15)) continue;
            
            visited[x][y] = true;
            country.addPixel(x, y, mapWidth);
            countryMap[x][y] = countryId;
            
            // Add all 8 neighbors
            queue.add(new Point(x + 1, y));
            queue.add(new Point(x - 1, y));
            queue.add(new Point(x, y + 1));
            queue.add(new Point(x, y - 1));
            queue.add(new Point(x + 1, y + 1));
            queue.add(new Point(x + 1, y - 1));
            queue.add(new Point(x - 1, y + 1));
            queue.add(new Point(x - 1, y - 1));
        }
    }
    
    private boolean colorsMatch(int pixel1, int pixel2, int tolerance) {
        Color c1 = new Color();
        Color c2 = new Color();
        Color.rgba8888ToColor(c1, pixel1);
        Color.rgba8888ToColor(c2, pixel2);
        
        float dr = Math.abs(c1.r - c2.r) * 255;
        float dg = Math.abs(c1.g - c2.g) * 255;
        float db = Math.abs(c1.b - c2.b) * 255;
        
        return dr < tolerance && dg < tolerance && db < tolerance;
    }
    
    private boolean isOceanOrBorder(Color color) {
        // White or very light (background)
        boolean isWhite = color.r > 0.95f && color.g > 0.95f && color.b > 0.95f;
        
        // Black borders
        boolean isBlack = color.r < 0.1f && color.g < 0.1f && color.b < 0.1f;
        
        // Light gray
        boolean isLightGray = color.r > 0.9f && color.g > 0.9f && color.b > 0.9f;
        
        return isWhite || isBlack || isLightGray;
    }
    
    private void identifyAllCountries() {
        // Match detected regions to country database
        Set<String> assignedNames = new HashSet<>();
        
        // Sort countries by size for priority
        countries.sort((a, b) -> b.pixelCount - a.pixelCount);
        
        for (Country country : countries) {
            float nx = (float)country.centerX / mapWidth;
            float ny = 1.0f - (float)country.centerY / mapHeight;
            
            // Find closest country in database
            String bestMatch = null;
            float minDistance = Float.MAX_VALUE;
            
            for (Map.Entry<String, CountryData> entry : COUNTRY_DATABASE.entrySet()) {
                if (assignedNames.contains(entry.getKey())) continue;
                
                CountryData data = entry.getValue();
                float dx = nx - data.relativeX;
                float dy = ny - data.relativeY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                
                if (distance < minDistance && distance < 0.05f) { // Within 5% of map
                    minDistance = distance;
                    bestMatch = entry.getKey();
                }
            }
            
            if (bestMatch != null) {
                country.name = bestMatch;
                country.population = COUNTRY_DATABASE.get(bestMatch).population;
                assignedNames.add(bestMatch);
            } else {
                // Fallback naming
                country.name = "Region " + country.id;
                country.population = 5f; // Default population
            }
        }
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
        
        // Draw influence
        for (Country country : countries) {
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
        float mapX = (screenX - panOffset.x) / zoom;
        float mapY = (screenY - panOffset.y) / zoom;
        
        int px = (int)(mapX * mapWidth / 800f);
        int py = (int)((480 - mapY) * mapHeight / 480f);
        
        if (px >= 0 && px < mapWidth && py >= 0 && py < mapHeight) {
            int countryId = countryMap[px][py];
            if (countryId >= 0 && countryId < countries.size()) {
                return countries.get(countryId);
            }
        }
        
        return null;
    }
    
    // Zoom methods
    public void zoomIn() {
        zoom = Math.min(zoom * 1.1f, maxZoom);
    }
    
    public void zoomOut() {
        zoom = Math.max(zoom * 0.9f, minZoom);
    }
    
    public void setZoom(float newZoom) {
        zoom = Math.max(minZoom, Math.min(maxZoom, newZoom));
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public void pan(float dx, float dy) {
        panOffset.x += dx;
        panOffset.y += dy;
        
        float maxPanX = Math.max(0, (800 * zoom - 800) / 2);
        float maxPanY = Math.max(0, (480 * zoom - 480) / 2);
        panOffset.x = Math.max(-maxPanX, Math.min(maxPanX, panOffset.x));
        panOffset.y = Math.max(-maxPanY, Math.min(maxPanY, panOffset.y));
    }
    
    public Vector2 getPanOffset() {
        return panOffset;
    }
    
    public void draw(SpriteBatch batch) {
        batch.draw(worldMapTexture, panOffset.x, panOffset.y, 800 * zoom, 480 * zoom);
        batch.draw(overlayTexture, panOffset.x, panOffset.y, 800 * zoom, 480 * zoom);
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
    
    public List<Country> getAllCountries() {
        return countries;
    }
    
    public void dispose() {
        worldMapTexture.dispose();
        overlayTexture.dispose();
        worldMapPixmap.dispose();
        overlayPixmap.dispose();
    }
    
    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}

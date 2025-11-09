package io.github.ackah.aiinfluence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class RegionalWorldMap {
    private Texture worldMapTexture;
    private Pixmap worldMapPixmap;
    private Texture overlayTexture;
    private Pixmap overlayPixmap;
    private List<Region> regions;
    private int[][] regionMap;
    private Region hoveredRegion;
    private Region selectedRegion;
    private int mapWidth, mapHeight;
    
    // Zoom and pan
    private float zoom = 1.0f;
    private float minZoom = 1.0f;
    private float maxZoom = 4.0f;
    private Vector2 panOffset = new Vector2(0, 0);
    
    // Screen dimensions
    private static final float SCREEN_WIDTH = 800f;
    private static final float SCREEN_HEIGHT = 480f;
    
    // Map to track all regions with same name
    private Map<String, List<Region>> regionsByName;
    
    public static class Region {
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
        
        public Region(int id, Color color) {
            this.id = id;
            this.color = color;
            this.pixelIndices = new HashSet<>();
            this.minX = Integer.MAX_VALUE;
            this.minY = Integer.MAX_VALUE;
            this.maxX = Integer.MIN_VALUE;
            this.maxY = Integer.MIN_VALUE;
            this.pixelCount = 0;
            this.name = "Unknown Region";
            this.population = 10f;
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
    
    private static class RegionData {
        String name;
        float population;
        String colorHint;
        
        RegionData(String name, float population) {
            this(name, population, null);
        }
        
        RegionData(String name, float population, String colorHint) {
            this.name = name;
            this.population = population;
            this.colorHint = colorHint;
        }
    }
    
    public RegionalWorldMap() {
        loadMap();
        detectRegionsWithFloodFill();
        identifyRegionsByPosition();
        groupRegionsByName();
        createOverlay();
    }
    
    private void loadMap() {
        worldMapTexture = new Texture(Gdx.files.internal("world_map.png"));
        worldMapTexture.getTextureData().prepare();
        worldMapPixmap = worldMapTexture.getTextureData().consumePixmap();
        mapWidth = worldMapPixmap.getWidth();
        mapHeight = worldMapPixmap.getHeight();
        regionMap = new int[mapWidth][mapHeight];
        System.out.println("Map dimensions: " + mapWidth + "x" + mapHeight);
    }
    
    private void detectRegionsWithFloodFill() {
        regions = new ArrayList<>();
        boolean[][] visited = new boolean[mapWidth][mapHeight];
        int regionId = 0;
        
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                regionMap[x][y] = -1;
            }
        }
        
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                if (!visited[x][y]) {
                    Color pixelColor = new Color();
                    int pixel = worldMapPixmap.getPixel(x, y);
                    Color.rgba8888ToColor(pixelColor, pixel);
                    
                    if (isWater(pixelColor)) {
                        visited[x][y] = true;
                        continue;
                    }
                    
                    if (pixelColor.r > 0.95f && pixelColor.g > 0.95f && pixelColor.b > 0.95f) {
                        visited[x][y] = true;
                        continue;
                    }
                    
                    if (pixelColor.r < 0.1f && pixelColor.g < 0.1f && pixelColor.b < 0.1f) {
                        visited[x][y] = true;
                        continue;
                    }
                    
                    Region newRegion = new Region(regionId, pixelColor);
                    newRegion.setMapWidth(mapWidth);
                    floodFill(x, y, pixel, newRegion, visited, regionId);
                    
                    if (newRegion.pixelCount > 100) {
                        newRegion.calculateCenter();
                        regions.add(newRegion);
                        regionId++;
                    }
                }
            }
        }
        
        System.out.println("Detected " + regions.size() + " regions");
    }
    
    private void floodFill(int startX, int startY, int targetColor, Region region, 
                          boolean[][] visited, int regionId) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));
        
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            int x = p.x;
            int y = p.y;
            
            if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) continue;
            if (visited[x][y]) continue;
            
            int pixel = worldMapPixmap.getPixel(x, y);
            Color pixelColor = new Color();
            Color.rgba8888ToColor(pixelColor, pixel);
            
            if (isWater(pixelColor)) {
                visited[x][y] = true;
                continue;
            }
            
            if (pixelColor.r > 0.95f && pixelColor.g > 0.95f && pixelColor.b > 0.95f) {
                visited[x][y] = true;
                continue;
            }
            
            if (!colorsMatch(pixel, targetColor, 15)) continue;
            
            visited[x][y] = true;
            region.addPixel(x, y, mapWidth);
            regionMap[x][y] = regionId;
            
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
    
    private boolean isWater(Color color) {
        if (color.r > 0.3f && color.r < 0.6f && 
            color.g < 0.5f && 
            color.b > 0.6f && color.b < 0.9f) return true;
        
        if (color.r < 0.3f && color.g < 0.3f && color.b > 0.4f) return true;
        if (color.r < 0.4f && color.g < 0.5f && color.b > 0.6f) return true;
        
        return false;
    }
    
    public boolean isClickOnWater(float screenX, float screenY) {
        float drawX = SCREEN_WIDTH / 2 - (SCREEN_WIDTH * zoom) / 2 + panOffset.x;
        float drawY = SCREEN_HEIGHT / 2 - (SCREEN_HEIGHT * zoom) / 2 + panOffset.y;
        
        float mapX = (screenX - drawX) / (SCREEN_WIDTH * zoom);
        float mapY = (screenY - drawY) / (SCREEN_HEIGHT * zoom);
        
        int px = (int)(mapX * mapWidth);
        int py = (int)((1.0f - mapY) * mapHeight);
        
        if (px >= 0 && px < mapWidth && py >= 0 && py < mapHeight) {
            Color pixelColor = new Color();
            int pixel = worldMapPixmap.getPixel(px, py);
            Color.rgba8888ToColor(pixelColor, pixel);
            return isWater(pixelColor);
        }
        return false;
    }
    
    private void identifyRegionsByPosition() {
        Map<String, RegionData> regionDataMap = new HashMap<>();
        
        // North America
        regionDataMap.put("0.17,0.43", new RegionData("United States", 331f));
        regionDataMap.put("0.18,0.43", new RegionData("United States", 331f));
        regionDataMap.put("0.10,0.20", new RegionData("Alaska", 0.7f));
        regionDataMap.put("0.09,0.20", new RegionData("Alaska", 0.7f));
        regionDataMap.put("0.18,0.30", new RegionData("Canada", 38f));
        regionDataMap.put("0.20,0.30", new RegionData("Canada", 38f));
        regionDataMap.put("0.17,0.54", new RegionData("Mexico", 128f));
        regionDataMap.put("0.17,0.52", new RegionData("Mexico", 128f));
        regionDataMap.put("0.24,0.54", new RegionData("Cuba", 11f));
        regionDataMap.put("0.22,0.58", new RegionData("Central America", 50f));
        
        // Arctic Islands
        String[] arcticCoords = {"0.28,0.19", "0.29,0.22", "0.58,0.19", "0.65,0.10", 
                                 "0.64,0.09", "0.62,0.08", "0.57,0.10", "0.54,0.11",
                                 "0.50,0.13", "0.49,0.14", "0.31,0.11", "0.29,0.09",
                                 "0.26,0.13", "0.23,0.13", "0.22,0.18", "0.20,0.15",
                                 "0.22,0.10", "0.24,0.10", "0.25,0.08", "0.31,0.10",
                                 "0.29,0.08", "0.25,0.17", "0.25,0.18", "0.21,0.18"};
        for (String coord : arcticCoords) {
            regionDataMap.put(coord, new RegionData("Arctic Islands", 0.1f));
        }
        
        // South America
        regionDataMap.put("0.25,0.63", new RegionData("Venezuela", 28f));
        regionDataMap.put("0.26,0.64", new RegionData("Venezuela", 28f));
        regionDataMap.put("0.31,0.69", new RegionData("Brazil", 212f));
        regionDataMap.put("0.31,0.70", new RegionData("Brazil", 212f));
        regionDataMap.put("0.28,0.81", new RegionData("Argentina", 45f));
        regionDataMap.put("0.28,0.80", new RegionData("Argentina", 45f));
        
        // Europe
        regionDataMap.put("0.42,0.28", new RegionData("Iceland", 0.4f));
        regionDataMap.put("0.50,0.29", new RegionData("Scandinavia", 30f));
        regionDataMap.put("0.50,0.39", new RegionData("Central Europe", 180f));
        regionDataMap.put("0.50,0.40", new RegionData("Central Europe", 180f));
        regionDataMap.put("0.45,0.37", new RegionData("England", 68f)); // Fixed: was Ireland, now England
        regionDataMap.put("0.44,0.36", new RegionData("England", 68f)); // Fixed: was Ireland, now England
        regionDataMap.put("0.45,0.36", new RegionData("England", 68f)); // Fixed: was Ireland, now England
        regionDataMap.put("0.50,0.46", new RegionData("Central Europe", 180f));
        regionDataMap.put("0.48,0.45", new RegionData("Central Europe", 180f));
        regionDataMap.put("0.48,0.43", new RegionData("Central Europe", 180f));
        
        // Africa
        regionDataMap.put("0.52,0.56", new RegionData("Northern Africa", 250f));
        regionDataMap.put("0.53,0.57", new RegionData("Northern Africa", 250f));
        regionDataMap.put("0.44,0.54", new RegionData("Western Africa", 400f));
        regionDataMap.put("0.45,0.54", new RegionData("Western Africa", 400f));
        regionDataMap.put("0.53,0.68", new RegionData("Central Africa", 180f));
        regionDataMap.put("0.53,0.67", new RegionData("Central Africa", 180f));
        regionDataMap.put("0.52,0.77", new RegionData("Southern Africa", 70f));
        regionDataMap.put("0.53,0.76", new RegionData("Southern Africa", 70f));
        regionDataMap.put("0.59,0.74", new RegionData("Madagascar", 28f));
        
        // Asia
        regionDataMap.put("0.71,0.27", new RegionData("Russia", 146f));
        regionDataMap.put("0.71,0.26", new RegionData("Russia", 146f));
        regionDataMap.put("0.65,0.38", new RegionData("Kazakhstan", 19f));
        regionDataMap.put("0.64,0.39", new RegionData("Kazakhstan", 19f));
        regionDataMap.put("0.74,0.39", new RegionData("Mongolia", 3f));
        regionDataMap.put("0.75,0.46", new RegionData("China", 1439f));
        regionDataMap.put("0.75,0.45", new RegionData("China", 1439f));
        regionDataMap.put("0.81,0.44", new RegionData("Korea", 78f));
        regionDataMap.put("0.81,0.43", new RegionData("Korea", 78f));
        
        // Japan
        regionDataMap.put("0.85,0.44", new RegionData("Japan", 126f));
        regionDataMap.put("0.84,0.45", new RegionData("Japan", 126f));
        regionDataMap.put("0.85,0.39", new RegionData("Japan", 126f));
        regionDataMap.put("0.84,0.39", new RegionData("Japan", 126f));
        
        // Taiwan
        regionDataMap.put("0.81,0.53", new RegionData("Taiwan", 24f));
        regionDataMap.put("0.80,0.53", new RegionData("Taiwan", 24f));
        
        // Middle East & India
        regionDataMap.put("0.60,0.48", new RegionData("Middle East", 400f));
        regionDataMap.put("0.61,0.49", new RegionData("Middle East", 400f));
        regionDataMap.put("0.69,0.53", new RegionData("India", 1380f));
        regionDataMap.put("0.70,0.53", new RegionData("India", 1380f));
        regionDataMap.put("0.69,0.61", new RegionData("India", 1380f)); // Fixed: unknown region now India
        regionDataMap.put("0.76,0.56", new RegionData("Indochina", 250f));
        regionDataMap.put("0.76,0.57", new RegionData("Indochina", 250f));
        
        // Oceanic Islands
        regionDataMap.put("0.79,0.64", new RegionData("Oceanic Islands", 700f));
        regionDataMap.put("0.78,0.68", new RegionData("Oceanic Islands", 700f));
        regionDataMap.put("0.81,0.57", new RegionData("Oceanic Islands", 700f));
        regionDataMap.put("0.87,0.67", new RegionData("Oceanic Islands", 700f));
        regionDataMap.put("0.79,0.65", new RegionData("Oceanic Islands", 700f));
        regionDataMap.put("0.75,0.65", new RegionData("Oceanic Islands", 700f));
        
        // Oceania
        regionDataMap.put("0.85,0.77", new RegionData("Australia", 26f));
        regionDataMap.put("0.84,0.78", new RegionData("Australia", 26f));
        regionDataMap.put("0.86,0.89", new RegionData("Australia", 26f));
        regionDataMap.put("0.93,0.91", new RegionData("New Zealand", 5f));
        regionDataMap.put("0.93,0.90", new RegionData("New Zealand", 5f));
        
        // Greenland
        regionDataMap.put("0.38,0.18", new RegionData("Greenland", 0.06f));
        regionDataMap.put("0.38,0.17", new RegionData("Greenland", 0.06f));
        
        Set<Region> assignedRegions = new HashSet<>();
        
        for (Region region : regions) {
            float nx = (float)region.centerX / mapWidth;
            float ny = (float)region.centerY / mapHeight;
            
            String key = String.format("%.2f,%.2f", nx, ny);
            RegionData data = regionDataMap.get(key);
            
            if (data != null) {
                region.name = data.name;
                region.population = data.population;
                assignedRegions.add(region);
                System.out.println("Region at " + key + " identified as: " + region.name);
            }
        }
        
        for (Region region : regions) {
            if (assignedRegions.contains(region)) continue;
            
            float nx = (float)region.centerX / mapWidth;
            float ny = (float)region.centerY / mapHeight;
            
            String closestKey = null;
            float minDist = 0.03f;
            
            for (String mapKey : regionDataMap.keySet()) {
                String[] parts = mapKey.split(",");
                float mx = Float.parseFloat(parts[0]);
                float my = Float.parseFloat(parts[1]);
                
                float dist = (float)Math.sqrt((nx - mx) * (nx - mx) + (ny - my) * (ny - my));
                if (dist < minDist) {
                    minDist = dist;
                    closestKey = mapKey;
                }
            }
            
            if (closestKey != null) {
                RegionData data = regionDataMap.get(closestKey);
                region.name = data.name;
                region.population = data.population;
                System.out.println("Region at " + String.format("%.2f,%.2f", nx, ny) + 
                                 " matched to " + closestKey + " as: " + region.name);
            } else {
                System.out.println("Region at " + String.format("%.2f,%.2f", nx, ny) + 
                                 " could not be identified (size: " + region.pixelCount + ")");
            }
        }
    }
    
    private void groupRegionsByName() {
        regionsByName = new HashMap<>();
        
        for (Region region : regions) {
            if (!regionsByName.containsKey(region.name)) {
                regionsByName.put(region.name, new ArrayList<>());
            }
            regionsByName.get(region.name).add(region);
        }
        
        // Share influence among regions with same name
        for (List<Region> sameNameRegions : regionsByName.values()) {
            if (sameNameRegions.size() > 1) {
                // Sync influence across all regions with same name
                float maxInfluence = 0;
                for (Region r : sameNameRegions) {
                    maxInfluence = Math.max(maxInfluence, r.influence);
                }
                for (Region r : sameNameRegions) {
                    r.influence = maxInfluence;
                }
            }
        }
        
        System.out.println("Grouped regions into " + regionsByName.size() + " unique territories");
    }
    
    // Get all regions with the same name as the given region
    private List<Region> getAllRelatedRegions(Region region) {
        if (region == null) return new ArrayList<>();
        return regionsByName.getOrDefault(region.name, new ArrayList<>());
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
        
        // Draw influence for all regions
        for (Region region : regions) {
            if (region.influence > 0) {
                float alpha = region.influence / 100f * 0.5f;
                overlayPixmap.setColor(1f, 0.1f, 0.1f, alpha);
                
                for (int pixelIndex : region.pixelIndices) {
                    int x = pixelIndex % mapWidth;
                    int y = pixelIndex / mapWidth;
                    overlayPixmap.drawPixel(x, y);
                }
            }
        }
        
        // Highlight ALL regions with same name as hovered region
        if (hoveredRegion != null) {
            List<Region> relatedRegions = getAllRelatedRegions(hoveredRegion);
            overlayPixmap.setColor(1f, 1f, 0.3f, 0.3f);
            for (Region r : relatedRegions) {
                for (int pixelIndex : r.pixelIndices) {
                    int x = pixelIndex % mapWidth;
                    int y = pixelIndex / mapWidth;
                    overlayPixmap.drawPixel(x, y);
                }
            }
        }
        
        // Highlight ALL regions with same name as selected region
        if (selectedRegion != null) {
            List<Region> relatedRegions = getAllRelatedRegions(selectedRegion);
            overlayPixmap.setColor(0.3f, 1f, 1f, 0.4f);
            for (Region r : relatedRegions) {
                for (int pixelIndex : r.pixelIndices) {
                    int x = pixelIndex % mapWidth;
                    int y = pixelIndex / mapWidth;
                    overlayPixmap.drawPixel(x, y);
                }
            }
        }
        
        overlayTexture.dispose();
        overlayTexture = new Texture(overlayPixmap);
    }
    
    // Apply influence to all regions with the same name
    public void applyInfluenceToSelected(float amount) {
        if (selectedRegion != null) {
            List<Region> relatedRegions = getAllRelatedRegions(selectedRegion);
            for (Region r : relatedRegions) {
                r.influence = Math.min(100, Math.max(0, r.influence + amount));
            }
        }
    }
    
    public Region getRegionAt(float screenX, float screenY) {
        float drawX = SCREEN_WIDTH / 2 - (SCREEN_WIDTH * zoom) / 2 + panOffset.x;
        float drawY = SCREEN_HEIGHT / 2 - (SCREEN_HEIGHT * zoom) / 2 + panOffset.y;
        
        float mapX = (screenX - drawX) / (SCREEN_WIDTH * zoom);
        float mapY = (screenY - drawY) / (SCREEN_HEIGHT * zoom);
        
        int px = (int)(mapX * mapWidth);
        int py = (int)((1.0f - mapY) * mapHeight);
        
        if (px >= 0 && px < mapWidth && py >= 0 && py < mapHeight) {
            Color pixelColor = new Color();
            int pixel = worldMapPixmap.getPixel(px, py);
            Color.rgba8888ToColor(pixelColor, pixel);
            
            if (isWater(pixelColor)) {
                return null;
            }
            
            int regionId = regionMap[px][py];
            if (regionId >= 0 && regionId < regions.size()) {
                return regions.get(regionId);
            }
        }
        
        return null;
    }
    
    public int getMapWidth() { return mapWidth; }
    public int getMapHeight() { return mapHeight; }
    
    // Get count of unique territories (regions grouped by name)
    public int getUniqueRegionCount() {
        return regionsByName.size();
    }
    
    public void zoomAtPosition(float screenX, float screenY, float zoomFactor) {
        float oldZoom = zoom;
        float newZoom = Math.max(minZoom, Math.min(maxZoom, zoom * zoomFactor));
        
        if (newZoom == 1.0f) {
            zoom = 1.0f;
            panOffset.set(0, 0);
            return;
        }
        
        if (newZoom != oldZoom) {
            float drawX = SCREEN_WIDTH / 2 - (SCREEN_WIDTH * oldZoom) / 2 + panOffset.x;
            float drawY = SCREEN_HEIGHT / 2 - (SCREEN_HEIGHT * oldZoom) / 2 + panOffset.y;
            
            float mapX = (screenX - drawX) / (SCREEN_WIDTH * oldZoom);
            float mapY = (screenY - drawY) / (SCREEN_HEIGHT * oldZoom);
            
            zoom = newZoom;
            
            float newDrawX = SCREEN_WIDTH / 2 - (SCREEN_WIDTH * zoom) / 2;
            float newDrawY = SCREEN_HEIGHT / 2 - (SCREEN_HEIGHT * zoom) / 2;
            
            panOffset.x = screenX - (mapX * SCREEN_WIDTH * zoom) - newDrawX;
            panOffset.y = screenY - (mapY * SCREEN_HEIGHT * zoom) - newDrawY;
            
            clampPan();
        }
    }
    
    public void zoomAtPinchCenter(float centerX, float centerY, float newZoom) {
        float oldZoom = zoom;
        newZoom = Math.max(minZoom, Math.min(maxZoom, newZoom));
        
        if (newZoom == 1.0f) {
            zoom = 1.0f;
            panOffset.set(0, 0);
            return;
        }
        
        if (newZoom != oldZoom) {
            float drawX = SCREEN_WIDTH / 2 - (SCREEN_WIDTH * oldZoom) / 2 + panOffset.x;
            float drawY = SCREEN_HEIGHT / 2 - (SCREEN_HEIGHT * oldZoom) / 2 + panOffset.y;
            
            float mapX = (centerX - drawX) / (SCREEN_WIDTH * oldZoom);
            float mapY = (centerY - drawY) / (SCREEN_HEIGHT * oldZoom);
            
            zoom = newZoom;
            
            float newDrawX = SCREEN_WIDTH / 2 - (SCREEN_WIDTH * zoom) / 2;
            float newDrawY = SCREEN_HEIGHT / 2 - (SCREEN_HEIGHT * zoom) / 2;
            
            panOffset.x = centerX - (mapX * SCREEN_WIDTH * zoom) - newDrawX;
            panOffset.y = centerY - (mapY * SCREEN_HEIGHT * zoom) - newDrawY;
            
            clampPan();
        }
    }
    
    public void zoomIn() {
        zoomAtPosition(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 1.1f);
    }
    
    public void zoomOut() {
        zoomAtPosition(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0.9f);
    }
    
    public void setZoom(float newZoom) {
        zoom = Math.max(minZoom, Math.min(maxZoom, newZoom));
        if (zoom == 1.0f) {
            panOffset.set(0, 0);
        } else {
            clampPan();
        }
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public void pan(float dx, float dy) {
        if (zoom > 1.0f) {
            panOffset.x += dx;
            panOffset.y += dy;
            clampPan();
        }
    }
    
    private void clampPan() {
        float zoomedWidth = SCREEN_WIDTH * zoom;
        float zoomedHeight = SCREEN_HEIGHT * zoom;
        
        float maxPanX = (zoomedWidth - SCREEN_WIDTH) / 2;
        float maxPanY = (zoomedHeight - SCREEN_HEIGHT) / 2;
        
        panOffset.x = Math.max(-maxPanX, Math.min(maxPanX, panOffset.x));
        panOffset.y = Math.max(-maxPanY, Math.min(maxPanY, panOffset.y));
    }
    
    public Vector2 getPanOffset() {
        return panOffset;
    }
    
    public void draw(SpriteBatch batch) {
        float drawX = SCREEN_WIDTH / 2 - (SCREEN_WIDTH * zoom) / 2 + panOffset.x;
        float drawY = SCREEN_HEIGHT / 2 - (SCREEN_HEIGHT * zoom) / 2 + panOffset.y;
        
        batch.draw(worldMapTexture, drawX, drawY, SCREEN_WIDTH * zoom, SCREEN_HEIGHT * zoom);
        batch.draw(overlayTexture, drawX, drawY, SCREEN_WIDTH * zoom, SCREEN_HEIGHT * zoom);
    }
    
    public void setHoveredRegion(Region region) {
        hoveredRegion = region;
    }
    
    public void setSelectedRegion(Region region) {
        selectedRegion = region;
    }
    
    public Region getHoveredRegion() {
        return hoveredRegion;
    }
    
    public Region getSelectedRegion() {
        return selectedRegion;
    }
    
    public List<Region> getAllRegions() {
        return regions;
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

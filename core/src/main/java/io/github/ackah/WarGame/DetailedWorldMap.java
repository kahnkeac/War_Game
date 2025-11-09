package io.github.ackah.aiinfluence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class DetailedWorldMap {
    private List<Continent> continents;
    private float mapWidth = 800;
    private float mapHeight = 480;
    
    public class Continent {
        public String name;
        public float[] vertices;
        public Color baseColor;
        public float influence = 0;
        public float population;
        
        public Continent(String name, float[] vertices, Color color, float population) {
            this.name = name;
            this.vertices = vertices;
            this.baseColor = color;
            this.population = population;
        }
        
        public Color getDisplayColor() {
            float r = Math.min(1f, baseColor.r + (influence / 100f) * 0.5f);
            float g = baseColor.g * (1f - influence / 200f);
            float b = baseColor.b * (1f - influence / 200f);
            return new Color(r, g, b, 1f);
        }
        
        public void draw(ShapeRenderer sr) {
            sr.setColor(getDisplayColor());
            // Draw filled polygon using triangulation
            if (vertices.length >= 6) {
                // Simple triangle fan from first vertex
                for (int i = 2; i < vertices.length / 2 - 1; i++) {
                    sr.triangle(
                        vertices[0], vertices[1],
                        vertices[i * 2], vertices[i * 2 + 1],
                        vertices[(i + 1) * 2], vertices[(i + 1) * 2 + 1]
                    );
                }
            }
        }
        
        public boolean contains(float x, float y) {
            // Simple bounding box check for now
            float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
            float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
            
            for (int i = 0; i < vertices.length / 2; i++) {
                minX = Math.min(minX, vertices[i * 2]);
                maxX = Math.max(maxX, vertices[i * 2]);
                minY = Math.min(minY, vertices[i * 2 + 1]);
                maxY = Math.max(maxY, vertices[i * 2 + 1]);
            }
            
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }
    }
    
    public DetailedWorldMap() {
        initializeContinents();
    }
    
    private void initializeContinents() {
        continents = new ArrayList<>();
        
        // Simplified but recognizable continent shapes
        
        // NORTH AMERICA
        float[] northAmerica = {
            100, 350, 120, 360, 140, 365, 160, 360, 180, 350,
            200, 340, 210, 320, 215, 300, 210, 280, 200, 260,
            190, 250, 180, 245, 170, 240, 160, 245, 150, 250,
            140, 260, 130, 280, 120, 300, 110, 320, 100, 340
        };
        
        // SOUTH AMERICA
        float[] southAmerica = {
            170, 230, 180, 225, 190, 220, 195, 210, 200, 200,
            205, 180, 207, 160, 205, 140, 200, 120, 195, 100,
            190, 80, 185, 60, 180, 50, 175, 45, 170, 50,
            165, 60, 160, 80, 155, 100, 150, 120, 145, 140,
            143, 160, 145, 180, 150, 200, 155, 210, 160, 220,
            165, 225
        };
        
        // AFRICA
        float[] africa = {
            380, 320, 400, 318, 420, 315, 430, 300, 435, 280,
            437, 260, 435, 240, 430, 220, 425, 200, 420, 180,
            415, 160, 410, 140, 405, 120, 400, 100, 395, 95,
            390, 100, 385, 120, 380, 140, 375, 160, 370, 180,
            365, 200, 360, 220, 355, 240, 350, 260, 348, 280,
            350, 300, 360, 310, 370, 315
        };
        
        // EUROPE
        float[] europe = {
            390, 360, 410, 358, 430, 355, 440, 350, 445, 340,
            443, 330, 440, 325, 435, 328, 430, 330, 420, 332,
            410, 335, 400, 338, 390, 340, 385, 345, 380, 350,
            385, 355
        };
        
        // ASIA
        float[] asia = {
            450, 350, 480, 348, 510, 345, 540, 340, 570, 335,
            600, 330, 620, 325, 640, 320, 650, 310, 655, 290,
            650, 270, 645, 250, 640, 230, 630, 220, 610, 225,
            590, 230, 570, 235, 550, 240, 530, 245, 510, 250,
            490, 260, 470, 280, 455, 300, 450, 320, 448, 340
        };
        
        // OCEANIA (Australia)
        float[] oceania = {
            570, 180, 590, 178, 610, 175, 625, 170, 635, 160,
            640, 150, 638, 140, 635, 130, 630, 120, 620, 115,
            600, 112, 580, 110, 560, 112, 550, 115, 545, 120,
            540, 130, 538, 140, 540, 150, 545, 160, 550, 170,
            560, 175
        };
        
        // Add continents with colors and population
        continents.add(new Continent("North America", northAmerica, 
            new Color(0.2f, 0.6f, 0.3f, 1), 579f));
        continents.add(new Continent("South America", southAmerica, 
            new Color(0.3f, 0.7f, 0.2f, 1), 422f));
        continents.add(new Continent("Africa", africa, 
            new Color(0.8f, 0.6f, 0.3f, 1), 1340f));
        continents.add(new Continent("Europe", europe, 
            new Color(0.4f, 0.4f, 0.7f, 1), 746f));
        continents.add(new Continent("Asia", asia, 
            new Color(0.7f, 0.7f, 0.4f, 1), 4641f));
        continents.add(new Continent("Oceania", oceania, 
            new Color(0.5f, 0.7f, 0.8f, 1), 42f));
    }
    
    public void draw(ShapeRenderer shapeRenderer) {
        // Ocean background
        shapeRenderer.setColor(0.15f, 0.3f, 0.5f, 1);
        shapeRenderer.rect(0, 0, mapWidth, mapHeight);
        
        // Draw all continents
        for (Continent c : continents) {
            c.draw(shapeRenderer);
        }
    }
    
    public void drawOutlines(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
        for (Continent c : continents) {
            float[] v = c.vertices;
            for (int i = 0; i < v.length / 2; i++) {
                int next = (i + 1) % (v.length / 2);
                shapeRenderer.line(v[i * 2], v[i * 2 + 1], 
                                 v[next * 2], v[next * 2 + 1]);
            }
        }
    }
    
    public Continent getContinentAt(float x, float y) {
        for (Continent c : continents) {
            if (c.contains(x, y)) {
                return c;
            }
        }
        return null;
    }
    
    public List<Continent> getContinents() {
        return continents;
    }
}

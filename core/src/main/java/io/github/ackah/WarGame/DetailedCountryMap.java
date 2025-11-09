package io.github.ackah.aiinfluence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class DetailedCountryMap {
    private List<Country> countries;
    
    public static class Country {
        public String name;
        public float[] vertices;
        public Color baseColor;
        public float influence = 0;
        public float population;
        
        public Country(String name, float[] vertices, Color color, float population) {
            this.name = name;
            this.vertices = vertices;
            this.baseColor = color;
            this.population = population;
        }
        
        public void draw(ShapeRenderer sr) {
            Color displayColor = new Color(
                Math.min(1f, baseColor.r + (influence / 100f) * 0.5f),
                baseColor.g * (1f - influence / 200f),
                baseColor.b * (1f - influence / 200f),
                1f
            );
            sr.setColor(displayColor);
            
            // Draw as triangles
            if (vertices.length >= 6) {
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
            // Simple bounding box check
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
    
    public DetailedCountryMap() {
        initializeCountries();
    }
    
    private void initializeCountries() {
        countries = new ArrayList<>();
        
        // USA
        countries.add(new Country("USA", new float[]{
            100, 340, 120, 345, 140, 342, 160, 338, 180, 335,
            200, 330, 220, 325, 230, 320, 235, 310, 238, 300,
            240, 290, 238, 280, 235, 270, 230, 265, 220, 262,
            200, 260, 180, 262, 160, 265, 140, 270, 120, 275,
            100, 280, 90, 290, 85, 300, 83, 310, 85, 320,
            90, 330, 95, 335
        }, new Color(0.2f, 0.3f, 0.8f, 1), 331f));
        
        // Canada
        countries.add(new Country("Canada", new float[]{
            80, 380, 100, 385, 140, 383, 180, 380, 220, 378,
            250, 375, 270, 370, 275, 360, 270, 350, 250, 345,
            220, 343, 180, 345, 140, 347, 100, 350, 80, 355,
            70, 365, 70, 375
        }, new Color(0.8f, 0.2f, 0.2f, 1), 38f));
        
        // Mexico
        countries.add(new Country("Mexico", new float[]{
            120, 270, 140, 268, 160, 265, 170, 260, 175, 250,
            172, 240, 168, 230, 163, 225, 155, 223, 145, 225,
            135, 230, 125, 235, 115, 240, 110, 250, 108, 260,
            110, 265, 115, 268
        }, new Color(0.2f, 0.8f, 0.2f, 1), 128f));
        
        // Brazil
        countries.add(new Country("Brazil", new float[]{
            200, 220, 220, 215, 240, 210, 255, 200, 265, 190,
            270, 180, 272, 170, 270, 160, 265, 150, 260, 145,
            250, 143, 235, 145, 220, 150, 205, 155, 190, 160,
            180, 170, 175, 180, 173, 190, 175, 200, 180, 210,
            190, 215, 195, 218
        }, new Color(0.2f, 0.7f, 0.2f, 1), 212f));
        
        // Add more countries here...
        // This would need hundreds more countries to match the map perfectly
        
        // China
        countries.add(new Country("China", new float[]{
            550, 340, 580, 338, 610, 335, 640, 330, 660, 325,
            670, 315, 672, 305, 670, 295, 665, 285, 655, 280,
            640, 278, 610, 280, 580, 285, 550, 290, 540, 300,
            538, 315, 540, 325, 545, 335
        }, new Color(0.8f, 0.2f, 0.2f, 1), 1439f));
        
        // India
        countries.add(new Country("India", new float[]{
            520, 280, 535, 275, 550, 270, 560, 260, 565, 250,
            563, 240, 560, 230, 555, 225, 545, 223, 530, 225,
            515, 230, 505, 240, 500, 250, 498, 260, 500, 270,
            510, 275, 515, 278
        }, new Color(1.0f, 0.6f, 0.2f, 1), 1380f));
        
        // More European countries...
        countries.add(new Country("Germany", new float[]{
            405, 360, 415, 358, 420, 355, 422, 350, 420, 345,
            415, 343, 410, 344, 405, 346, 402, 350, 402, 355
        }, new Color(0.3f, 0.3f, 0.3f, 1), 83f));
        
        countries.add(new Country("France", new float[]{
            385, 355, 395, 353, 402, 350, 404, 345, 402, 340,
            395, 338, 385, 340, 380, 345, 380, 350
        }, new Color(0.4f, 0.4f, 0.8f, 1), 65f));
    }
    
    public void draw(ShapeRenderer shapeRenderer) {
        // Ocean
        shapeRenderer.setColor(0.15f, 0.3f, 0.5f, 1);
        shapeRenderer.rect(0, 0, 800, 480);
        
        // Draw all countries
        for (Country c : countries) {
            c.draw(shapeRenderer);
        }
    }
    
    public void drawBorders(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.BLACK);
        for (Country c : countries) {
            float[] v = c.vertices;
            for (int i = 0; i < v.length / 2; i++) {
                int next = (i + 1) % (v.length / 2);
                shapeRenderer.line(v[i * 2], v[i * 2 + 1], 
                                 v[next * 2], v[next * 2 + 1]);
            }
        }
    }
    
    public Country getCountryAt(float x, float y) {
        for (Country c : countries) {
            if (c.contains(x, y)) {
                return c;
            }
        }
        return null;
    }
    
    public List<Country> getCountries() {
        return countries;
    }
}

package io.github.ackah.aiinfluence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class Region {
    public String name;
    public float x, y, width, height;
    public float influence;
    public float population;
    public Rectangle bounds;
    public Color baseColor;
    
    public Region(String name, float x, float y, float width, float height, float population, Color baseColor) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.population = population;
        this.influence = 0;
        this.bounds = new Rectangle(x, y, width, height);
        this.baseColor = baseColor;
    }
    
    public Color getDisplayColor() {
        // Mix base color with red based on influence level
        float r = Math.min(1f, baseColor.r + (influence / 100f) * 0.5f);
        float g = baseColor.g * (1f - influence / 200f);
        float b = baseColor.b * (1f - influence / 200f);
        return new Color(r, g, b, 1f);
    }
    
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }
}

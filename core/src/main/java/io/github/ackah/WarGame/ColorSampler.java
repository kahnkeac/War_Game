package io.github.ackah.aiinfluence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;

public class ColorSampler {
    public static void sampleColors() {
        Texture tex = new Texture(Gdx.files.internal("world_map.png"));
        tex.getTextureData().prepare();
        Pixmap pixmap = tex.getTextureData().consumePixmap();
        
        // Sample specific pixel locations (adjust these coordinates)
        sampleAt(pixmap, 150, 320, "USA");
        sampleAt(pixmap, 150, 380, "Canada");
        sampleAt(pixmap, 600, 320, "China");
        sampleAt(pixmap, 540, 270, "India");
        // Add more sample points...
        
        pixmap.dispose();
        tex.dispose();
    }
    
    private static void sampleAt(Pixmap pixmap, int x, int y, String countryName) {
        int pixel = pixmap.getPixel(x, pixmap.getHeight() - y);
        Color color = new Color();
        Color.rgba8888ToColor(color, pixel);
        
        System.out.println(countryName + ": new Color(" + 
            color.r + "f, " + color.g + "f, " + color.b + "f, 1f)");
    }
}

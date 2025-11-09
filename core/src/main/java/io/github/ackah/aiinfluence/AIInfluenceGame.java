package io.github.ackah.aiinfluence;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import io.github.ackah.aiinfluence.screens.MainMenuScreen;

public class AIInfluenceGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        this.setScreen(new MainMenuScreen(this));
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}

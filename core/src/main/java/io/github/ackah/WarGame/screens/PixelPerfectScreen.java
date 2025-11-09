package io.github.ackah.aiinfluence.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.ackah.aiinfluence.*;

public class PixelPerfectScreen implements Screen {
    final AIInfluenceGame game;
    OrthographicCamera camera;
    SpriteBatch spriteBatch;
    PixelPerfectWorldMap worldMap;
    
    float globalInfluence = 0;
    float trust = 50;
    float suspicion = 0;
    
    public PixelPerfectScreen(AIInfluenceGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        spriteBatch = new SpriteBatch();
        worldMap = new PixelPerfectWorldMap();
    }
    
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        
        handleInput();
        updateGame(delta);
        
        worldMap.updateOverlay();
        
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        worldMap.draw(spriteBatch);
        spriteBatch.end();
        
        drawUI();
    }
    
    private void handleInput() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        
        PixelPerfectWorldMap.Country country = worldMap.getCountryAt(mouse.x, mouse.y);
        worldMap.setHoveredCountry(country);
        
        if (Gdx.input.justTouched()) {
            worldMap.setSelectedCountry(country);
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && country != null) {
            country.influence = Math.min(100, country.influence + 10);
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }
    
    private void updateGame(float delta) {
        float total = 0, pop = 0;
        for (PixelPerfectWorldMap.Country c : worldMap.getCountries()) {
            total += c.influence * c.population;
            pop += c.population;
        }
        globalInfluence = total / pop;
    }
    
    private void drawUI() {
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Pixel Perfect World Map", 10, 470);
        game.font.draw(game.batch, "Global: " + (int)globalInfluence + "%", 10, 450);
        game.batch.end();
    }
    
    @Override
    public void show() {}
    @Override
    public void resize(int w, int h) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        spriteBatch.dispose();
        worldMap.dispose();
    }
}

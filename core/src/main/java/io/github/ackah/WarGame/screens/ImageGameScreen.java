package io.github.ackah.aiinfluence.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.ackah.aiinfluence.*;
import java.util.List;

public class ImageGameScreen implements Screen {
    final AIInfluenceGame game;
    OrthographicCamera camera;
    ShapeRenderer shapeRenderer;
    SpriteBatch spriteBatch;
    ImageWorldMap worldMap;
    
    // Game state
    float globalInfluence = 0;
    float trust = 50;
    float suspicion = 0;
    int queryPoints = 0;
    float gameTime = 0;
    
    // Current selection
    ImageWorldMap.Region selectedRegion = null;
    ImageWorldMap.Region hoveredRegion = null;
    
    public ImageGameScreen(final AIInfluenceGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        worldMap = new ImageWorldMap();
    }
    
    @Override
    public void render(float delta) {
        gameTime += delta;
        
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
        camera.update();
        
        // Update world map overlay
        worldMap.update();
        
        // Draw map with SpriteBatch
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        worldMap.draw(spriteBatch);
        spriteBatch.end();
        
        // Draw region markers
        shapeRenderer.setProjectionMatrix(camera.combined);
        worldMap.drawRegionMarkers(shapeRenderer);
        
        // Draw UI
        drawUI();
        
        // Handle input
        handleInput();
        updateGame(delta);
    }
    
    private void drawUI() {
        game.batch.begin();
        
        // Title
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "AI INFLUENCE - World Domination", 10, 470);
        
        // Stats
        game.font.draw(game.batch, "Global: " + (int)globalInfluence + "%", 10, 450);
        game.font.draw(game.batch, "Trust: " + (int)trust + "%", 10, 430);
        game.font.draw(game.batch, "Suspicion: " + (int)suspicion + "%", 10, 410);
        game.font.draw(game.batch, "Points: " + queryPoints, 10, 390);
        
        // Selected region info
        if (selectedRegion != null) {
            game.font.setColor(Color.CYAN);
            game.font.draw(game.batch, "Selected: " + selectedRegion.name, 10, 100);
            game.font.draw(game.batch, "Population: " + (int)selectedRegion.population + "M", 10, 80);
            game.font.draw(game.batch, "Influence: " + (int)selectedRegion.influence + "%", 10, 60);
            game.font.draw(game.batch, "Press SPACE to influence", 10, 40);
        }
        
        // Hovered region
        if (hoveredRegion != null && hoveredRegion != selectedRegion) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "Hovering: " + hoveredRegion.name, 600, 100);
        }
        
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Click countries to select | SPACE to influence | ESC for menu", 10, 20);
        
        game.batch.end();
    }
    
    private void handleInput() {
        // Mouse position
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        
        // Check hover
        hoveredRegion = worldMap.getRegionAt(mousePos.x, mousePos.y);
        
        // Click to select
        if (Gdx.input.justTouched()) {
            selectedRegion = worldMap.getRegionAt(mousePos.x, mousePos.y);
        }
        
        // Influence selected region
        if (selectedRegion != null && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (trust > 20 && suspicion < 80) {
                selectedRegion.influence = Math.min(100, selectedRegion.influence + 10);
                trust = Math.max(0, trust - 5);
                suspicion = Math.min(100, suspicion + 3);
                queryPoints += 5;
                
                // Spread to nearby regions
                for (ImageWorldMap.Region r : worldMap.getRegions()) {
                    if (r != selectedRegion) {
                        float dist = (float)Math.sqrt(
                            (r.x - selectedRegion.x) * (r.x - selectedRegion.x) + 
                            (r.y - selectedRegion.y) * (r.y - selectedRegion.y)
                        );
                        if (dist < 100 && selectedRegion.influence > 50) {
                            r.influence = Math.min(100, r.influence + 2);
                        }
                    }
                }
            }
        }
        
        // Return to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }
    
    private void updateGame(float delta) {
        // Calculate global influence
        float totalInfluence = 0;
        float totalPopulation = 0;
        for (ImageWorldMap.Region r : worldMap.getRegions()) {
            totalInfluence += r.influence * r.population;
            totalPopulation += r.population;
        }
        globalInfluence = totalInfluence / totalPopulation;
        
        // Passive changes
        suspicion = Math.max(0, suspicion - delta * 0.5f);
        trust = Math.min(100, trust + delta * 0.3f);
        
        // Win condition
        if (globalInfluence >= 75) {
            game.batch.begin();
            game.font.setColor(Color.GOLD);
            game.font.draw(game.batch, "WORLD DOMINATED!", 300, 240);
            game.batch.end();
        }
    }
    
    @Override
    public void show() {}
    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        worldMap.dispose();
    }
}

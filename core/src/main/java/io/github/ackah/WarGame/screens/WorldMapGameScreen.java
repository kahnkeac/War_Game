package io.github.ackah.aiinfluence.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.ackah.aiinfluence.*;

public class WorldMapGameScreen implements Screen, InputProcessor {
    private final AIInfluenceGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private CompleteWorldMap worldMap;
    
    // Input handling
    private boolean isDragging = false;
    private Vector2 lastTouch = new Vector2();
    private Vector2 dragStart = new Vector2();
    
    // Pinch-to-zoom for mobile
    private boolean isPinching = false;
    private float initialDistance = 0;
    private float initialZoom = 1;
    
    // Game state
    private float globalInfluence = 0;
    
    public WorldMapGameScreen(AIInfluenceGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        worldMap = new CompleteWorldMap();
        Gdx.input.setInputProcessor(this);
    }
    
    @Override
    public void render(float delta) {
        handleInput();
        updateGame(delta);
        
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        // Update and draw world map
        worldMap.updateOverlay();
        batch.begin();
        worldMap.draw(batch);
        batch.end();
        
        // Draw UI
        drawUI();
    }
    
    private void handleInput() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        
        // Update hover
        CompleteWorldMap.Country country = worldMap.getCountryAt(mousePos.x, mousePos.y);
        worldMap.setHoveredCountry(country);
        
        // Keyboard controls
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            CompleteWorldMap.Country selected = worldMap.getSelectedCountry();
            if (selected != null) {
                selected.influence = Math.min(100, selected.influence + 15);
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
        
        // Keyboard zoom
        if (Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            worldMap.zoomIn();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            worldMap.zoomOut();
        }
    }
    
    private void updateGame(float delta) {
        float totalInfluence = 0;
        float totalPopulation = 0;
        
        for (CompleteWorldMap.Country country : worldMap.getAllCountries()) {
            totalInfluence += country.influence * country.population;
            totalPopulation += country.population;
        }
        
        if (totalPopulation > 0) {
            globalInfluence = totalInfluence / totalPopulation;
        }
    }
    
    private void drawUI() {
        game.batch.begin();
        
        // Title
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "World Map - " + worldMap.getAllCountries().size() + " Countries", 10, 470);
        game.font.draw(game.batch, "Global Influence: " + (int)globalInfluence + "%", 10, 450);
        
        // Hover info
        CompleteWorldMap.Country hovered = worldMap.getHoveredCountry();
        if (hovered != null) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "Country: " + hovered.name, 10, 100);
            game.font.draw(game.batch, "Population: " + (int)hovered.population + "M", 10, 80);
            game.font.draw(game.batch, "Influence: " + (int)hovered.influence + "%", 10, 60);
        }
        
        // Selected info
        CompleteWorldMap.Country selected = worldMap.getSelectedCountry();
        if (selected != null) {
            game.font.setColor(Color.CYAN);
            game.font.draw(game.batch, "Selected: " + selected.name, 10, 140);
        }
        
        // Controls
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Scroll: Zoom | Drag: Pan | Click: Select | Space: Influence", 10, 20);
        
        // Zoom level
        game.font.draw(game.batch, "Zoom: " + (int)(worldMap.getZoom() * 100) + "%", 700, 20);
        
        game.batch.end();
    }
    
    // InputProcessor methods - ALL OF THEM
    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (amountY < 0) {
            worldMap.zoomIn();
        } else {
            worldMap.zoomOut();
        }
        return true;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldPos = new Vector3(screenX, screenY, 0);
        camera.unproject(worldPos);
        
        if (pointer == 0) {
            CompleteWorldMap.Country country = worldMap.getCountryAt(worldPos.x, worldPos.y);
            if (country != null) {
                worldMap.setSelectedCountry(country);
            }
            
            isDragging = true;
            lastTouch.set(screenX, screenY);
            dragStart.set(screenX, screenY);
        } else if (pointer == 1) {
            isPinching = true;
            Vector2 touch1 = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
            Vector2 touch2 = new Vector2(Gdx.input.getX(1), Gdx.input.getY(1));
            initialDistance = touch1.dst(touch2);
            initialZoom = worldMap.getZoom();
        }
        
        return true;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isPinching && pointer <= 1) {
            Vector2 touch1 = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
            Vector2 touch2 = new Vector2(Gdx.input.getX(1), Gdx.input.getY(1));
            float distance = touch1.dst(touch2);
            float ratio = distance / initialDistance;
            worldMap.setZoom(initialZoom * ratio);
        } else if (isDragging && !isPinching && pointer == 0) {
            float dx = screenX - lastTouch.x;
            float dy = -(screenY - lastTouch.y);
            
            if (Math.abs(screenX - dragStart.x) > 5 || Math.abs(screenY - dragStart.y) > 5) {
                worldMap.pan(dx, dy);
            }
            
            lastTouch.set(screenX, screenY);
        }
        
        return true;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            isDragging = false;
        } else if (pointer == 1) {
            isPinching = false;
        }
        return true;
    }
    
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // This was missing!
        if (pointer == 0) {
            isDragging = false;
        } else if (pointer == 1) {
            isPinching = false;
        }
        return true;
    }
    
    @Override
    public boolean keyDown(int keycode) { return false; }
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    
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
        batch.dispose();
        worldMap.dispose();
        Gdx.input.setInputProcessor(null);
    }
}

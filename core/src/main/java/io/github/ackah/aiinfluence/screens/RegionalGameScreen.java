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
import java.util.*;

public class RegionalGameScreen implements Screen, InputProcessor {
    private final AIInfluenceGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private RegionalWorldMap worldMap;
    
    private boolean isDragging = false;
    private Vector2 lastTouch = new Vector2();
    private boolean isPinching = false;
    private float initialDistance = 0;
    private float initialZoom = 1;
    private Vector2 pinchCenter = new Vector2();
    
    private float globalInfluence = 0;
    
    // Debug coordinates
    private float debugX = 0;
    private float debugY = 0;
    
    public RegionalGameScreen(AIInfluenceGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        worldMap = new RegionalWorldMap();
        Gdx.input.setInputProcessor(this);
    }
    
    @Override
    public void render(float delta) {
        handleInput();
        updateGame(delta);
        
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        worldMap.updateOverlay();
        batch.begin();
        worldMap.draw(batch);
        batch.end();
        
        drawUI();
    }
    
    private void handleInput() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        
        // Calculate normalized debug coordinates
        float zoom = worldMap.getZoom();
        Vector2 pan = worldMap.getPanOffset();
        float mapX = (mousePos.x - pan.x) / zoom;
        float mapY = (mousePos.y - pan.y) / zoom;
        
        debugX = mapX / 800f;
        debugY = 1.0f - (mapY / 480f);
        
        RegionalWorldMap.Region region = worldMap.getRegionAt(mousePos.x, mousePos.y);
        worldMap.setHoveredRegion(region);
        
        // Apply influence to all regions with same name
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            worldMap.applyInfluenceToSelected(15);
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
        
        // Keyboard zoom (centered)
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
        
        // Calculate influence by unique regions (don't double-count same-named regions)
        Set<String> countedRegions = new HashSet<>();
        
        for (RegionalWorldMap.Region region : worldMap.getAllRegions()) {
            if (!countedRegions.contains(region.name)) {
                totalInfluence += region.influence * region.population;
                totalPopulation += region.population;
                countedRegions.add(region.name);
            }
        }
        
        if (totalPopulation > 0) {
            globalInfluence = totalInfluence / totalPopulation;
        }
    }
    
    private void drawUI() {
        game.batch.begin();
        
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "AI INFLUENCE - Regional Control", 10, 470);
        game.font.draw(game.batch, "Global Influence: " + (int)globalInfluence + "%", 10, 450);
        game.font.draw(game.batch, "Unique Territories: " + worldMap.getUniqueRegionCount(), 10, 430);
        
        // DEBUG COORDINATES
        game.font.setColor(Color.RED);
        game.font.draw(game.batch, "DEBUG POS: (" + String.format("%.2f", debugX) + ", " + 
                       String.format("%.2f", debugY) + ")", 10, 410);
        
        // Show region info if hovering
        RegionalWorldMap.Region hovered = worldMap.getHoveredRegion();
        if (hovered != null) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "Region: " + hovered.name, 10, 100);
            game.font.draw(game.batch, "Population: " + (int)hovered.population + "M", 10, 80);
            game.font.draw(game.batch, "Influence: " + (int)hovered.influence + "%", 10, 60);
            
            float regionCenterX = (float)hovered.centerX / worldMap.getMapWidth();
            float regionCenterY = (float)hovered.centerY / worldMap.getMapHeight();
            game.font.setColor(Color.ORANGE);
            game.font.draw(game.batch, "Region Center: (" + String.format("%.2f", regionCenterX) + 
                          ", " + String.format("%.2f", regionCenterY) + ")", 10, 40);
        }
        
        RegionalWorldMap.Region selected = worldMap.getSelectedRegion();
        if (selected != null) {
            game.font.setColor(Color.CYAN);
            game.font.draw(game.batch, "Selected: " + selected.name, 10, 140);
        }
        
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Scroll: Zoom | Click: Select | Space: Influence", 10, 20);
        game.font.draw(game.batch, "Zoom: " + (int)(worldMap.getZoom() * 100) + "%", 700, 20);
        
        game.batch.end();
    }
    
    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Get mouse position for cursor-centered zoom
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        
        // Zoom at cursor position
        if (amountY < 0) {
            worldMap.zoomAtPosition(mousePos.x, mousePos.y, 1.1f);
        } else {
            worldMap.zoomAtPosition(mousePos.x, mousePos.y, 0.9f);
        }
        return true;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldPos = new Vector3(screenX, screenY, 0);
        camera.unproject(worldPos);
        
        if (pointer == 0) {
            // Check if clicking on ocean to deselect
            if (worldMap.isClickOnWater(worldPos.x, worldPos.y)) {
                worldMap.setSelectedRegion(null);
                System.out.println("Clicked on ocean - deselecting");
            } else {
                RegionalWorldMap.Region region = worldMap.getRegionAt(worldPos.x, worldPos.y);
                if (region != null) {
                    worldMap.setSelectedRegion(region);
                    
                    float regionCenterX = (float)region.centerX / worldMap.getMapWidth();
                    float regionCenterY = (float)region.centerY / worldMap.getMapHeight();
                    System.out.println("CLICKED: " + region.name + " at (" + 
                                     String.format("%.2f", regionCenterX) + ", " + 
                                     String.format("%.2f", regionCenterY) + ")");
                }
            }
            isDragging = true;
            lastTouch.set(screenX, screenY);
        } else if (pointer == 1) {
            // Start pinch zoom
            isPinching = true;
            Vector2 touch1 = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
            Vector2 touch2 = new Vector2(Gdx.input.getX(1), Gdx.input.getY(1));
            initialDistance = touch1.dst(touch2);
            initialZoom = worldMap.getZoom();
            
            // Calculate pinch center in world coordinates
            Vector3 center = new Vector3((touch1.x + touch2.x) / 2, 
                                        (touch1.y + touch2.y) / 2, 0);
            camera.unproject(center);
            pinchCenter.set(center.x, center.y);
        }
        return true;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isPinching && pointer <= 1) {
            Vector2 touch1 = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
            Vector2 touch2 = new Vector2(Gdx.input.getX(1), Gdx.input.getY(1));
            float distance = touch1.dst(touch2);
            float newZoom = initialZoom * (distance / initialDistance);
            
            // Update pinch center in world coordinates
            Vector3 center = new Vector3((touch1.x + touch2.x) / 2, 
                                        (touch1.y + touch2.y) / 2, 0);
            camera.unproject(center);
            
            // Zoom at pinch center
            worldMap.zoomAtPinchCenter(center.x, center.y, newZoom);
        } else if (isDragging && !isPinching && pointer == 0) {
            float dx = screenX - lastTouch.x;
            float dy = -(screenY - lastTouch.y);
            worldMap.pan(dx, dy);
            lastTouch.set(screenX, screenY);
        }
        return true;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) isDragging = false;
        else if (pointer == 1) isPinching = false;
        return true;
    }
    
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return touchUp(screenX, screenY, pointer, button);
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

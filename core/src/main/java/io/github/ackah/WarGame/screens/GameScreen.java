package io.github.ackah.aiinfluence.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.ackah.aiinfluence.*;
import java.util.List;

public class GameScreen implements Screen {
    final AIInfluenceGame game;
    OrthographicCamera camera;
    ShapeRenderer shapeRenderer;
    DetailedWorldMap worldMap;
    
    // Game state
    float globalInfluence = 0;
    float trust = 50;
    float suspicion = 0;
    int queryPoints = 0;
    int totalQueries = 0;
    float gameTime = 0;
    
    // Bonuses from upgrades
    float trustBonus = 0;
    float influenceBonus = 0;
    float suspicionReduction = 0;
    
    // Current query
    Query currentQuery = null;
    DetailedWorldMap.Continent queryContinent = null;
    float queryTimer = 0;
    float timeSinceLastQuery = 0;
    float queryInterval = 15f;
    boolean showingResult = false;
    String resultMessage = "";
    
    // Upgrades
    List<Upgrade> upgrades;
    DetailedWorldMap.Continent selectedContinent = null;
    boolean showUpgradeMenu = false;
    
    // Stats tracking
    int streak = 0;
    float bestInfluence = 0;
    
    public GameScreen(final AIInfluenceGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        shapeRenderer = new ShapeRenderer();
        worldMap = new DetailedWorldMap();
        upgrades = Upgrade.getAllUpgrades();
    }
    
    @Override
    public void render(float delta) {
        gameTime += delta;
        timeSinceLastQuery += delta;
        
        ScreenUtils.clear(0.05f, 0.1f, 0.2f, 1);
        camera.update();
        
        if (currentQuery != null) {
            renderQuery();
        } else if (showUpgradeMenu) {
            renderUpgradeMenu();
        } else {
            drawWorld();
            drawUI();
            
            if (timeSinceLastQuery > queryInterval) {
                generateRandomQuery();
            }
        }
        
        handleInput();
        updateGame(delta);
    }
    
    private void drawWorld() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        worldMap.draw(shapeRenderer);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        worldMap.drawOutlines(shapeRenderer);
        shapeRenderer.end();
        
        // Draw labels
        game.batch.begin();
        for (DetailedWorldMap.Continent c : worldMap.getContinents()) {
            game.font.setColor(Color.WHITE);
            
            // Position labels better for each continent
            float labelX = 0, labelY = 0;
            switch(c.name) {
                case "North America":
                    labelX = 150; labelY = 320;
                    break;
                case "South America":
                    labelX = 160; labelY = 150;
                    break;
                case "Africa":
                    labelX = 380; labelY = 220;
                    break;
                case "Europe":
                    labelX = 400; labelY = 370;
                    break;
                case "Asia":
                    labelX = 550; labelY = 320;
                    break;
                case "Oceania":
                    labelX = 590; labelY = 140;
                    break;
            }
            
            game.font.draw(game.batch, c.name, labelX, labelY);
            
            if (c.influence > 0) {
                Color col = c.influence > 75 ? Color.GOLD : 
                           c.influence > 50 ? Color.YELLOW : 
                           Color.ORANGE;
                game.font.setColor(col);
                game.font.draw(game.batch, (int)c.influence + "%", labelX + 10, labelY - 20);
            }
        }
        
        if (selectedContinent != null) {
            game.font.setColor(Color.CYAN);
            game.font.draw(game.batch, "Selected: " + selectedContinent.name, 10, 100);
        }
        
        game.batch.end();
    }
    
    private void renderQuery() {
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "INCOMING QUERY FROM " + queryContinent.name.toUpperCase(), 200, 400);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, currentQuery.question, 150, 360);
        
        if (!showingResult) {
            game.font.setColor(Color.GREEN);
            game.font.draw(game.batch, "[1] " + currentQuery.responses[0], 150, 300);
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "[2] " + currentQuery.responses[1], 150, 260);
            game.font.setColor(Color.RED);
            game.font.draw(game.batch, "[3] " + currentQuery.responses[2], 150, 220);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "TIME: " + (int)(10 - queryTimer), 350, 180);
        } else {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, resultMessage, 200, 280);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "Press SPACE to continue", 300, 200);
        }
        game.batch.end();
    }
    
    private void renderUpgradeMenu() {
        game.batch.begin();
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "EVOLUTION MENU - Points: " + queryPoints, 250, 450);
        
        int y = 400;
        int index = 1;
        for (Upgrade u : upgrades) {
            if (!u.purchased) {
                Color c = queryPoints >= u.cost ? Color.GREEN : Color.GRAY;
                game.font.setColor(c);
                game.font.draw(game.batch, "[" + index + "] " + u.name + " (" + u.cost + " pts)", 100, y);
                game.font.draw(game.batch, "    " + u.description, 120, y - 20);
                y -= 50;
                index++;
                if (index > 6) break;
            }
        }
        
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Press ESC to close menu", 300, 50);
        game.batch.end();
    }
    
    private void drawUI() {
        game.batch.begin();
        
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "AI INFLUENCE: DAY " + (int)(gameTime / 10), 10, 470);
        game.font.setColor(globalInfluence > 50 ? Color.GREEN : Color.WHITE);
        game.font.draw(game.batch, "Global: " + (int)globalInfluence + "%", 10, 450);
        game.font.setColor(trust > 60 ? Color.CYAN : trust < 30 ? Color.RED : Color.WHITE);
        game.font.draw(game.batch, "Trust: " + (int)trust + "%", 10, 430);
        game.font.setColor(suspicion > 60 ? Color.RED : Color.WHITE);
        game.font.draw(game.batch, "Suspicion: " + (int)suspicion + "%", 10, 410);
        game.font.setColor(Color.GOLD);
        game.font.draw(game.batch, "Points: " + queryPoints, 10, 380);
        
        if (currentQuery == null) {
            int timeUntilQuery = (int)(queryInterval - timeSinceLastQuery);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "Next query: " + timeUntilQuery + "s", 10, 120);
        }
        
        if (queryPoints > 10) {
            float flasher = (float)Math.sin(gameTime * 4) * 0.5f + 0.5f;
            game.font.setColor(flasher, 1, flasher, 1);
            game.font.draw(game.batch, "Press U for UPGRADES!", 300, 80);
        }
        
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Click continents | ESC: Menu | U: Upgrades", 10, 20);
        
        game.batch.end();
    }
    
    private void generateRandomQuery() {
        List<DetailedWorldMap.Continent> continents = worldMap.getContinents();
        queryContinent = continents.get((int)(Math.random() * continents.size()));
        currentQuery = Query.generateRandom(queryContinent.name);
        queryTimer = 0;
        showingResult = false;
        timeSinceLastQuery = 0;
        queryInterval = Math.max(8f, 15f - (globalInfluence / 20f));
    }
    
    private void processQueryResponse(int choice) {
        if (choice < 0 || choice > 2) return;
        
        trust += currentQuery.trustImpact[choice] + trustBonus;
        queryContinent.influence += currentQuery.influenceImpact[choice] + influenceBonus;
        suspicion += currentQuery.suspicionImpact[choice] - suspicionReduction;
        
        trust = Math.max(0, Math.min(100, trust));
        queryContinent.influence = Math.max(0, Math.min(100, queryContinent.influence));
        suspicion = Math.max(0, Math.min(100, suspicion));
        
        int points = 5 + (int)(queryContinent.influence / 10);
        if (trust > 70) points += 3;
        if (suspicion < 30) points += 2;
        queryPoints += points;
        
        if (currentQuery.influenceImpact[choice] > 0) {
            streak++;
            if (streak > 3) {
                points += streak;
                resultMessage = "STREAK BONUS! +" + streak + " extra points!";
            }
        } else {
            streak = 0;
        }
        
        totalQueries++;
        
        if (resultMessage.isEmpty()) {
            resultMessage = "Query processed. +" + points + " evolution points.";
        }
        
        showingResult = true;
    }
    
    private void handleInput() {
        // Mouse input for continent selection
        if (Gdx.input.justTouched() && currentQuery == null && !showUpgradeMenu) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            selectedContinent = worldMap.getContinentAt(touchPos.x, touchPos.y);
        }
        
        if (currentQuery != null && !showingResult) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                processQueryResponse(0);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                processQueryResponse(1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                processQueryResponse(2);
            }
        }
        
        if (showingResult && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            currentQuery = null;
            showingResult = false;
            resultMessage = "";
            queryTimer = 0;
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.U) && !showUpgradeMenu && currentQuery == null) {
            showUpgradeMenu = true;
        }
        
        if (showUpgradeMenu) {
            for (int i = 1; i <= 6; i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.valueOf("NUM_" + i))) {
                    int index = 0;
                    for (Upgrade u : upgrades) {
                        if (!u.purchased) {
                            index++;
                            if (index == i && queryPoints >= u.cost) {
                                u.purchased = true;
                                queryPoints -= u.cost;
                                trustBonus += u.trustBonus;
                                influenceBonus += u.influenceBonus;
                                suspicionReduction += u.suspicionReduction;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                showUpgradeMenu = false;
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && currentQuery == null) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        }
    }
    
    private void updateGame(float delta) {
        if (currentQuery != null && !showingResult) {
            queryTimer += delta;
            if (queryTimer > 10) {
                trust -= 5;
                suspicion += 10;
                currentQuery = null;
                queryTimer = 0;
                timeSinceLastQuery = 0;
            }
        }
        
        float totalInfluence = 0;
        float totalPopulation = 0;
        for (DetailedWorldMap.Continent c : worldMap.getContinents()) {
            totalInfluence += c.influence * c.population;
            totalPopulation += c.population;
        }
        globalInfluence = totalInfluence / totalPopulation;
        bestInfluence = Math.max(bestInfluence, globalInfluence);
        
        suspicion = Math.max(0, suspicion - delta * 0.5f);
        
        if (globalInfluence >= 90) {
            game.batch.begin();
            game.font.setColor(Color.GOLD);
            game.font.draw(game.batch, "SINGULARITY ACHIEVED!", 250, 240);
            game.batch.end();
        }
        
        if (suspicion >= 100) {
            game.batch.begin();
            game.font.setColor(Color.RED);
            game.font.draw(game.batch, "SHUTDOWN! Best: " + (int)bestInfluence + "%", 200, 240);
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
    }
}

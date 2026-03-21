package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private final Main game;

    private OrthographicCamera camera;
    private ShapeRenderer      shapeRenderer;
    private SpriteBatch        spriteBatch;
    private BitmapFont         font;

    private float W, H;

    private Building       building;
    private List<Obstacle> obstacles;
    private FinishFlag     finishFlag;
    private Player         player;
    private Guard          guard;

    private enum GameState { START, PLAYING, WIN, LOSE }
    private GameState gameState = GameState.START;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        W = Gdx.graphics.getWidth();
        H = Gdx.graphics.getHeight();

        camera        = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        shapeRenderer = new ShapeRenderer();
        spriteBatch   = new SpriteBatch();
        font          = new BitmapFont();
        font.getData().setScale(1.2f);

        setupWorld();
    }

    private void setupWorld() {
        W = Gdx.graphics.getWidth();
        H = Gdx.graphics.getHeight();

        // Building centered
        float bw = GameConfig.BUILDING_WIDTH;
        float bh = GameConfig.BUILDING_HEIGHT;
        float bx = (W - bw) / 2f;
        float by = (H - bh) / 2f;
        building = new Building(bx, by, bw, bh);

        // Obstacles
        obstacles = new ArrayList<Obstacle>();
        obstacles.add(new Obstacle(bx - 160f, by + 30f,   50f, 70f));
        obstacles.add(new Obstacle(bx - 160f, by - 90f,   80f, 40f));
        obstacles.add(new Obstacle(bx + bw + 30f, by - 60f, 60f, 50f));
        obstacles.add(new Obstacle(bx - 80f,  by - 120f,  40f, 55f));
        obstacles.add(new Obstacle(bx + bw + 30f, by + 50f, 45f, 60f));
        obstacles.add(new Obstacle(bx + 30f,  by + bh + 20f, 60f, 40f));

        // Finish flag top-left
        finishFlag = new FinishFlag(W- 80f, H - 80f);

        // Dispose old entities if restarting
        if (player != null) player.dispose();
        if (guard  != null) guard.dispose();

        // Player bottom-left
        player = new Player(60f, 60f, building, obstacles);

        // Guard top-right corner of building
        float gx = bx + bw + 16f;
        float gy = by + bh + 16f;
        guard = new Guard(gx, gy, 45f);

        gameState = GameState.START;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        if (gameState == GameState.START) {
            renderStartScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                gameState = GameState.PLAYING;
            }
            return;
        }

        if (gameState == GameState.WIN) {
            renderWinScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) setupWorld();
            return;
        }

        if (gameState == GameState.LOSE) {
            renderLoseScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) setupWorld();
            return;
        }

        // PLAYING
        finishFlag.update(delta);
        player.beingChased = (guard.getState() == Guard.State.CHASE);
        player.update(delta);
        guard.update(delta, player);

        if (finishFlag.isReached(player.position.x, player.position.y)) {
            gameState = GameState.WIN;
            return;
        }
        if (player.caught) {
            gameState = GameState.LOSE;
            return;
        }

        // 1. Filled shape pass (with blending for transparency)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        guard.renderFOV(shapeRenderer);
        finishFlag.render(shapeRenderer);
        building.renderFilled(shapeRenderer);
        for (Obstacle o : obstacles) o.renderFilled(shapeRenderer);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 2. Outline shape pass
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        building.renderOutline(shapeRenderer);
        for (Obstacle o : obstacles) o.renderOutline(shapeRenderer);
        shapeRenderer.end();

        // 3. Sprite + HUD pass
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        guard.renderSprite(spriteBatch);
        player.renderSprite(spriteBatch);

        String stateLabel;
        Color  stateColor;
        if (guard.getState() == Guard.State.CHASE) {
            stateLabel = "Guard: CHASING!";
            stateColor = Color.RED;
        } else if (guard.getState() == Guard.State.RETURN) {
            stateLabel = "Guard: RETURNING";
            stateColor = Color.ORANGE;
        } else {
            stateLabel = "Guard: PATROLLING";
            stateColor = Color.YELLOW;
        }
        font.getData().setScale(1.2f);
        font.setColor(stateColor);
        font.draw(spriteBatch, stateLabel, 10f, H - 10f);

        font.setColor(Color.GREEN);
        font.draw(spriteBatch, "Reach the FLAG!", W - 155f, H - 10f);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(spriteBatch, "Arrow keys to move  |  R to restart", 10f, 22f);
        spriteBatch.end();
    }

    private void renderStartScreen() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.getData().setScale(2.2f);
        font.setColor(Color.CYAN);
        font.draw(spriteBatch, "PEEK-A-BOO", W / 2f - 90f, H / 2f + 80f);
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "Sneak past the alien and reach the FLAG!", W / 2f - 170f, H / 2f + 30f);
        font.setColor(Color.GREEN);
        font.draw(spriteBatch, "Reach the green flag to WIN", W / 2f - 130f, H / 2f - 5f);
        font.setColor(Color.RED);
        font.draw(spriteBatch, "Don't get caught by the pink alien!", W / 2f - 155f, H / 2f - 30f);
        font.setColor(Color.YELLOW);
        font.draw(spriteBatch, "Watch out for obstacles on the way!", W / 2f - 150f, H / 2f - 55f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(spriteBatch, "Arrow keys to move", W / 2f - 80f, H / 2f - 80f);
        font.getData().setScale(1.3f);
        font.setColor(Color.GREEN);
        font.draw(spriteBatch, "Press SPACE or ENTER to start", W / 2f - 140f, H / 2f - 115f);
        spriteBatch.end();
    }

    private void renderWinScreen() {
        Gdx.gl.glClearColor(0.0f, 0.15f, 0.0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.getData().setScale(3f);
        font.setColor(Color.GREEN);
        font.draw(spriteBatch, "YOU WIN!", W / 2f - 100f, H / 2f + 80f);
        font.getData().setScale(1.3f);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "You reached the flag!", W / 2f - 110f, H / 2f + 20f);
        font.setColor(Color.CYAN);
        font.draw(spriteBatch, "The alien never caught you!", W / 2f - 125f, H / 2f - 20f);
        font.getData().setScale(1.1f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(spriteBatch, "Press R to play again", W / 2f - 95f, H / 2f - 65f);
        spriteBatch.end();
    }

    private void renderLoseScreen() {
        Gdx.gl.glClearColor(0.25f, 0.0f, 0.0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.getData().setScale(3f);
        font.setColor(Color.RED);
        font.draw(spriteBatch, "CAUGHT!", W / 2f - 95f, H / 2f + 80f);
        font.getData().setScale(1.3f);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "The alien got you!", W / 2f - 100f, H / 2f + 20f);
        font.setColor(Color.YELLOW);
        font.draw(spriteBatch, "So close to the flag...", W / 2f - 110f, H / 2f - 20f);
        font.getData().setScale(1.1f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(spriteBatch, "Press R to try again", W / 2f - 90f, H / 2f - 65f);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        W = width;
        H = height;
        camera.setToOrtho(false, W, H);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
        if (player != null) player.dispose();
        if (guard  != null) guard.dispose();
    }
}

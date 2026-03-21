package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class FinishFlag {

    public final Vector2 position;
    private float pulseTimer = 0f;

    public FinishFlag(float x, float y) {
        this.position = new Vector2(x, y);
    }

    public void update(float delta) {
        pulseTimer += delta;
    }

    public void render(ShapeRenderer sr) {
        // Pulsing outer glow ring
        float pulse = (float) Math.sin(pulseTimer * 3f) * 0.5f + 0.5f;
        float outerR = GameConfig.FINISH_RADIUS + 8f + pulse * 6f;
        sr.setColor(GameConfig.FINISH_PULSE.r, GameConfig.FINISH_PULSE.g,
            GameConfig.FINISH_PULSE.b, 0.25f + pulse * 0.2f);
        sr.circle(position.x, position.y, outerR);

        // Solid inner circle
        sr.setColor(GameConfig.FINISH_COLOR);
        sr.circle(position.x, position.y, GameConfig.FINISH_RADIUS);

        // White flag pole line
        sr.setColor(Color.WHITE);
        sr.rectLine(position.x, position.y,
            position.x, position.y + GameConfig.FINISH_RADIUS + 18f, 2.5f);

        // Flag triangle on top of pole
        float poleTop = position.y + GameConfig.FINISH_RADIUS + 18f;
        sr.setColor(Color.WHITE);
        sr.triangle(
            position.x,        poleTop,
            position.x + 14f,  poleTop - 6f,
            position.x,        poleTop - 12f
        );
    }

    public boolean isReached(float px, float py) {
        float dx = px - position.x;
        float dy = py - position.y;
        return (dx * dx + dy * dy) < (GameConfig.FINISH_RADIUS * GameConfig.FINISH_RADIUS);
    }
}

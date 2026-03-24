package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class FinishFlag {

    public final Vector2 position;
    private float pulseTimer = 0f;

    public static final float RADIUS = 28f;
    public static final Color COLOR  = new Color(0.10f, 0.90f, 0.30f, 1f);
    public static final Color PULSE  = new Color(0.10f, 1.00f, 0.50f, 0.3f);

    public FinishFlag(float x, float y) {
        this.position = new Vector2(x, y);
    }

    public void update(float delta) {
        pulseTimer += delta;
    }

    public void render(ShapeRenderer sr) {
        // Pulsing outer glow ring
        float pulse = (float) Math.sin(pulseTimer * 3f) * 0.5f + 0.5f;
        float outerR = FinishFlag.RADIUS + 8f + pulse * 6f;
        sr.setColor(FinishFlag.PULSE.r, FinishFlag.PULSE.g,
            FinishFlag.PULSE.b, 0.25f + pulse * 0.2f);
        sr.circle(position.x, position.y, outerR);

        // Solid inner circle
        sr.setColor(FinishFlag.COLOR);
        sr.circle(position.x, position.y, FinishFlag.RADIUS);

        // White flag pole line
        sr.setColor(Color.WHITE);
        sr.rectLine(position.x, position.y,
            position.x, position.y + FinishFlag.RADIUS + 18f, 2.5f);

        // Flag triangle on top of pole
        float poleTop = position.y + FinishFlag.RADIUS + 18f;
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
        return (dx * dx + dy * dy) < (FinishFlag.RADIUS * FinishFlag.RADIUS);
    }
}

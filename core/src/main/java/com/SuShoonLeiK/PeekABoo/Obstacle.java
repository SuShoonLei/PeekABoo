package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public class Obstacle {

    public float x, y, width, height;

    public static final Color FILL   = new Color(0.30f, 0.18f, 0.10f, 1f);
    public static final Color BORDER = new Color(0.80f, 0.55f, 0.25f, 1f);

    public Obstacle(float x, float y, float width, float height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    public void renderFilled(ShapeRenderer sr) {
        sr.setColor(Obstacle.FILL);
        sr.rect(x, y, width, height);
    }

    public void renderOutline(ShapeRenderer sr) {
        sr.setColor(Obstacle.BORDER);
        sr.rect(x, y, width, height);
    }

    /** Circle-vs-AABB overlap — used for player + guard collision. */
    public boolean overlaps(float px, float py, float radius) {
        float nx = Math.max(x, Math.min(px, x + width));
        float ny = Math.max(y, Math.min(py, y + height));
        float dx = px - nx;
        float dy = py - ny;
        return (dx * dx + dy * dy) < (radius * radius);
    }
}

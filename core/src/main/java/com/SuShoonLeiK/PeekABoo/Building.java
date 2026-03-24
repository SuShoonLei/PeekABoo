package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Building {

    public float x, y, width, height;

    public static final Color FILL   = new Color(0.20f, 0.20f, 0.23f, 1f);
    public static final Color BORDER = new Color(0.85f, 0.85f, 0.85f, 1f);
    public static final float WIDTH  = 120f;
    public static final float HEIGHT =  90f;

    public Building(float x, float y, float width, float height) {
        this.x = x;  this.y = y;
        this.width = width;  this.height = height;
    }

    public void renderFilled(ShapeRenderer sr) {
        sr.setColor(Building.FILL);
        sr.rect(x, y, width, height);
    }

    public void renderOutline(ShapeRenderer sr) {
        sr.setColor(Building.BORDER);
        sr.rect(x, y, width, height);
    }

    public boolean overlaps(float px, float py, float radius) {
        float nx = Math.max(x, Math.min(px, x + width));
        float ny = Math.max(y, Math.min(py, y + height));
        float dx = px - nx;
        float dy = py - ny;
        return (dx * dx + dy * dy) < (radius * radius);
    }
}

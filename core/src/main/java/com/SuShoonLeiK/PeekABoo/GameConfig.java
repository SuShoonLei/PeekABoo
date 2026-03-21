package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.graphics.Color;

public class GameConfig {

    // ── Building ──────────────────────────────────────────────────
    public static final float BUILDING_WIDTH  = 120f;
    public static final float BUILDING_HEIGHT =  90f;
    public static final Color BUILDING_FILL   = new Color(0.20f, 0.20f, 0.23f, 1f);
    public static final Color BUILDING_BORDER = new Color(0.85f, 0.85f, 0.85f, 1f);

    // ── Obstacles ─────────────────────────────────────────────────
    public static final Color OBSTACLE_FILL   = new Color(0.30f, 0.18f, 0.10f, 1f);
    public static final Color OBSTACLE_BORDER = new Color(0.80f, 0.55f, 0.25f, 1f);

    // ── Finish flag ───────────────────────────────────────────────
    public static final float FINISH_RADIUS = 28f;
    public static final Color FINISH_COLOR  = new Color(0.10f, 0.90f, 0.30f, 1f);
    public static final Color FINISH_PULSE  = new Color(0.10f, 1.00f, 0.50f, 0.3f);

    // ── Player ────────────────────────────────────────────────────
    public static final float PLAYER_SPEED  = 120f;
    public static final float PLAYER_RADIUS =  14f;

    // ── Guard ─────────────────────────────────────────────────────
    public static final float GUARD_RADIUS       =  14f;
    public static final float GUARD_PATROL_SPEED =  45f;
    public static final float GUARD_CHASE_SPEED  =  90f;
    public static final float GUARD_RETURN_SPEED =  65f;
    public static final float GUARD_FOV_HALF     =  60f;
    public static final float GUARD_FOV_RANGE    = 130f;

    public static final Color GUARD_FOV_PATROL = new Color(1f, 1f,   0f,   0.22f);
    public static final Color GUARD_FOV_CHASE  = new Color(1f, 0f,   0f,   0.30f);
    public static final Color GUARD_FOV_RETURN = new Color(1f, 0.5f, 0f,   0.22f);
}

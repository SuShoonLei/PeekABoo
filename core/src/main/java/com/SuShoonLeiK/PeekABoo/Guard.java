package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Guard {

    public enum State { PATROL, CHASE, RETURN }

    public State   state    = State.PATROL;
    public Vector2 position;
    public float   angle;

    private static final float SPRITE_SIZE = 44f;

    private final float patrolCenter;
    private final float patrolRange = 135f;
    private       float patrolDir   =   1f;
    private final float catchDist   =  20f;

    private final float   homeAngle;
    private final Vector2 homePos;

    private final Texture texAlien;   // guard.png

    public Guard(float x, float y, float facingAngle) {
        position     = new Vector2(x, y);
        homePos      = new Vector2(x, y);
        angle        = facingAngle;
        homeAngle    = facingAngle;
        patrolCenter = facingAngle;
        texAlien     = new Texture(Gdx.files.internal("guard.png"));
    }

    public void update(float delta, Player player) {
        if      (state == State.PATROL) updatePatrol(delta, player);
        else if (state == State.CHASE)  updateChase(delta, player);
        else                            updateReturn(delta);
    }

    private void updatePatrol(float delta, Player player) {
        angle += patrolDir * GameConfig.GUARD_PATROL_SPEED * delta;
        if (angle > patrolCenter + patrolRange) {
            angle = patrolCenter + patrolRange; patrolDir = -1f;
        } else if (angle < patrolCenter - patrolRange) {
            angle = patrolCenter - patrolRange; patrolDir =  1f;
        }
        if (canSeePlayer(player)) state = State.CHASE;
    }

    private void updateChase(float delta, Player player) {
        float dx   = player.position.x - position.x;
        float dy   = player.position.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        if (dist > catchDist) {
            position.x += (dx / dist) * GameConfig.GUARD_CHASE_SPEED * delta;
            position.y += (dy / dist) * GameConfig.GUARD_CHASE_SPEED * delta;
        } else {
            player.caught = true;
        }

        if (!canSeePlayer(player) && dist > GameConfig.GUARD_FOV_RANGE * 1.2f) {
            state = State.RETURN;
        }
    }

    private void updateReturn(float delta) {
        float dx   = homePos.x - position.x;
        float dy   = homePos.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 2f) {
            angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            position.x += (dx / dist) * GameConfig.GUARD_RETURN_SPEED * delta;
            position.y += (dy / dist) * GameConfig.GUARD_RETURN_SPEED * delta;
        } else {
            position.set(homePos);
            angle = homeAngle;
            state = State.PATROL;
        }
    }

    public boolean canSeePlayer(Player player) {
        float dx   = player.position.x - position.x;
        float dy   = player.position.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > GameConfig.GUARD_FOV_RANGE) return false;
        float ap = (float) Math.toDegrees(Math.atan2(dy, dx));
        return Math.abs(angleDiff(ap, angle)) <= GameConfig.GUARD_FOV_HALF;
    }

    private float angleDiff(float a, float b) {
        float d = a - b;
        while (d >  180f) d -= 360f;
        while (d < -180f) d += 360f;
        return d;
    }

    /** Draw the FOV cone — call inside a Filled ShapeRenderer pass. */
    public void renderFOV(ShapeRenderer sr) {
        Color fovColor;
        if      (state == State.CHASE)  fovColor = GameConfig.GUARD_FOV_CHASE;
        else if (state == State.RETURN) fovColor = GameConfig.GUARD_FOV_RETURN;
        else                            fovColor = GameConfig.GUARD_FOV_PATROL;

        sr.setColor(fovColor);
        int   steps = 24;
        float step  = (GameConfig.GUARD_FOV_HALF * 2f) / steps;
        float start = angle - GameConfig.GUARD_FOV_HALF;
        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(start + i * step);
            float a2 = (float) Math.toRadians(start + (i + 1) * step);
            sr.triangle(
                position.x, position.y,
                position.x + (float) Math.cos(a1) * GameConfig.GUARD_FOV_RANGE,
                position.y + (float) Math.sin(a1) * GameConfig.GUARD_FOV_RANGE,
                position.x + (float) Math.cos(a2) * GameConfig.GUARD_FOV_RANGE,
                position.y + (float) Math.sin(a2) * GameConfig.GUARD_FOV_RANGE
            );
        }

        // Edge lines
        sr.setColor(fovColor.r, fovColor.g, fovColor.b, 0.75f);
        float lRad = (float) Math.toRadians(angle - GameConfig.GUARD_FOV_HALF);
        float rRad = (float) Math.toRadians(angle + GameConfig.GUARD_FOV_HALF);
        sr.line(position.x, position.y,
            position.x + (float) Math.cos(lRad) * GameConfig.GUARD_FOV_RANGE,
            position.y + (float) Math.sin(lRad) * GameConfig.GUARD_FOV_RANGE);
        sr.line(position.x, position.y,
            position.x + (float) Math.cos(rRad) * GameConfig.GUARD_FOV_RANGE,
            position.y + (float) Math.sin(rRad) * GameConfig.GUARD_FOV_RANGE);
    }

    /** Draw the alien sprite — call inside a SpriteBatch pass. */
    public void renderSprite(SpriteBatch batch) {
        float half = SPRITE_SIZE / 2f;
        batch.draw(
            texAlien,
            position.x - half,
            position.y - half,
            half, half,
            SPRITE_SIZE, SPRITE_SIZE,
            1f, 1f,
            angle - 90f,              // rotate to face movement direction
            0, 0,
            texAlien.getWidth(), texAlien.getHeight(),
            false, false
        );
    }

    public void dispose() {
        texAlien.dispose();
    }

    public State getState() { return state; }
}

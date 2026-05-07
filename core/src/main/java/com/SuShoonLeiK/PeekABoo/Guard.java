package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class Guard {

    public enum State { PATROL, CHASE, RETURN }

    public State   state = State.PATROL;
    public Vector2 position;
    public Vector2 velocity = new Vector2(0, 0);
    public float   maxAcceleration = 200f;
    public float   angle;

    private static final float SPRITE_SIZE = 44f;

    private static final float ARRIVE_SLOW_RADIUS = 60f;

    /** Total patrol width left–right of home (~120 units, within 100–130). */
    private static final float PATROL_HALFWIDTH   = 60f;
    private static final float PATROL_SWITCH_DIST = 10f;

    private final float catchDist = 20f;

    private final float   homeAngle;
    private final Vector2 homePos;

    private final Vector2 patrolWest = new Vector2();
    private final Vector2 patrolEast = new Vector2();
    private       int     patrolTargetIndex = 0;

    private final Texture texAlien;

    public static final float RADIUS       =  14f;
    public static final float PATROL_SPEED =  45f;
    public static final float CHASE_SPEED  =  90f;
    public static final float RETURN_SPEED =  65f;
    public static final float FOV_HALF     =  60f;
    public static final float FOV_RANGE    = 130f;
    public static final Color FOV_PATROL   = new Color(1f, 1f,   0f,   0.22f);
    public static final Color FOV_CHASE    = new Color(1f, 0f,   0f,   0.30f);
    public static final Color FOV_RETURN   = new Color(1f, 0.5f, 0f,   0.22f);

    private final Building       building;
    private final List<Obstacle> obstacles;

    private float chaseFxPhase = 0f;

    private final Vector2 desiredTmp  = new Vector2();
    private final Vector2 steeringTmp = new Vector2();
    private final Vector2 futureTmp   = new Vector2();

    private final Rectangle losRect = new Rectangle();
    private final Vector2   losP1   = new Vector2();
    private final Vector2   losP2   = new Vector2();

    public Guard(float x, float y, float facingAngle,
                 Building building, List<Obstacle> obstacles) {
        position     = new Vector2(x, y);
        homePos      = new Vector2(x, y);
        angle        = facingAngle;
        homeAngle    = facingAngle;
        texAlien     = new Texture(Gdx.files.internal("guard.png"));

        this.building  = building;
        this.obstacles = obstacles;

        patrolWest.set(homePos.x - PATROL_HALFWIDTH, homePos.y);
        patrolEast.set(homePos.x + PATROL_HALFWIDTH, homePos.y);
    }

    public void update(float delta, Player player) {
        if      (state == State.PATROL) updatePatrol(delta, player);
        else if (state == State.CHASE)  updateChase(delta, player);
        else                            updateReturn(delta);
    }

    private void applySteeringDesired(Vector2 desiredVel, float maxSpeed, float delta) {
        steeringTmp.set(desiredVel).sub(velocity);
        float cap = maxAcceleration * delta;
        if (steeringTmp.len2() > cap * cap) {
            steeringTmp.setLength(cap);
        }
        velocity.add(steeringTmp);
        if (velocity.len2() > maxSpeed * maxSpeed) {
            velocity.setLength(maxSpeed);
        }
    }

    private void steerSeek(Vector2 target, float maxSpeed, float delta) {
        desiredTmp.set(target).sub(position);
        if (desiredTmp.len2() < 1e-8f) {
            desiredTmp.setZero();
        } else {
            desiredTmp.nor().scl(maxSpeed);
        }
        applySteeringDesired(desiredTmp, maxSpeed, delta);
    }

    private void integratePosition(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);
        clampPositionAndVelocity();
    }

    /** Keeps the guard on-screen; damps velocity into hard edges so momentum cannot push them off-map. */
    private void clampPositionAndVelocity() {
        float r = Guard.RADIUS;
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float minX = r;
        float maxX = w - r;
        float minY = r;
        float maxY = h - r;

        if (position.x < minX) {
            position.x = minX;
            velocity.x = Math.min(0f, velocity.x);
        } else if (position.x > maxX) {
            position.x = maxX;
            velocity.x = Math.max(0f, velocity.x);
        }
        if (position.y < minY) {
            position.y = minY;
            velocity.y = Math.min(0f, velocity.y);
        } else if (position.y > maxY) {
            position.y = maxY;
            velocity.y = Math.max(0f, velocity.y);
        }
    }

    private void syncFacingFromVelocity() {
        if (velocity.len2() > 1f) {
            angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));
        }
    }

    private void updatePatrol(float delta, Player player) {
        Vector2 wp = (patrolTargetIndex == 0) ? patrolWest : patrolEast;
        if (position.dst(wp) < PATROL_SWITCH_DIST) {
            patrolTargetIndex = 1 - patrolTargetIndex;
            wp = (patrolTargetIndex == 0) ? patrolWest : patrolEast;
        }

        steerSeek(wp, Guard.PATROL_SPEED, delta);
        integratePosition(delta);
        syncFacingFromVelocity();

        if (canSeePlayer(player) && hasClearLineOfSight(player)) {
            state = State.CHASE;
        }
    }

    private void updateChase(float delta, Player player) {
        if (!hasClearLineOfSight(player)) {
            state = State.RETURN;
            return;
        }

        chaseFxPhase += delta * 10f;

        float distGp = position.dst(player.position);
        float T = distGp / Guard.CHASE_SPEED;
        futureTmp.set(player.velocity).scl(T).add(player.position);

        steerSeek(futureTmp, Guard.CHASE_SPEED, delta);
        integratePosition(delta);
        syncFacingFromVelocity();

        float dx   = player.position.x - position.x;
        float dy   = player.position.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist <= catchDist) {
            player.caught = true;
        }
    }

    private void updateReturn(float delta) {
        float dist = position.dst(homePos);
        if (dist <= 2f) {
            position.set(homePos);
            velocity.setZero();
            angle = homeAngle;
            state = State.PATROL;
            clampPositionAndVelocity();
            return;
        }

        float desiredSpeed = (dist < ARRIVE_SLOW_RADIUS)
            ? Guard.RETURN_SPEED * (dist / ARRIVE_SLOW_RADIUS)
            : Guard.RETURN_SPEED;

        desiredTmp.set(homePos).sub(position);
        if (dist < 1e-5f) {
            desiredTmp.setZero();
        } else {
            desiredTmp.nor().scl(desiredSpeed);
        }
        applySteeringDesired(desiredTmp, Guard.RETURN_SPEED, delta);
        integratePosition(delta);
        syncFacingFromVelocity();
    }

    public boolean canSeePlayer(Player player) {
        float dx   = player.position.x - position.x;
        float dy   = player.position.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > Guard.FOV_RANGE) return false;
        float ap = (float) Math.toDegrees(Math.atan2(dy, dx));
        return Math.abs(angleDiff(ap, angle)) <= Guard.FOV_HALF;
    }

    /**
     * True if an unobstructed segment exists from the guard to the player
     * (building and obstacles block; FOV is not required here — used while chasing).
     */
    public boolean hasClearLineOfSight(Player player) {
        float dx = player.position.x - position.x;
        float dy = player.position.y - position.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) {
            return true;
        }
        dx /= len;
        dy /= len;
        losP1.set(position.x + dx * RADIUS, position.y + dy * RADIUS);
        losP2.set(player.position.x - dx * Player.RADIUS, player.position.y - dy * Player.RADIUS);

        losRect.set(building.x, building.y, building.width, building.height);
        if (Intersector.intersectSegmentRectangle(losP1, losP2, losRect)) {
            return false;
        }
        for (Obstacle o : obstacles) {
            losRect.set(o.x, o.y, o.width, o.height);
            if (Intersector.intersectSegmentRectangle(losP1, losP2, losRect)) {
                return false;
            }
        }
        return true;
    }

    private float angleDiff(float a, float b) {
        float d = a - b;
        while (d >  180f) d -= 360f;
        while (d < -180f) d += 360f;
        return d;
    }

    public void renderFOV(ShapeRenderer sr) {
        Color fovColor;
        if      (state == State.CHASE)  fovColor = Guard.FOV_CHASE;
        else if (state == State.RETURN) fovColor = Guard.FOV_RETURN;
        else                            fovColor = Guard.FOV_PATROL;

        float chasePulse = (state == State.CHASE)
            ? 0.78f + 0.22f * MathUtils.sin(chaseFxPhase)
            : 1f;

        sr.setColor(fovColor.r, fovColor.g, fovColor.b, fovColor.a * chasePulse);
        int   steps = 24;
        float step  = (Guard.FOV_HALF * 2f) / steps;
        float start = angle - Guard.FOV_HALF;
        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(start + i * step);
            float a2 = (float) Math.toRadians(start + (i + 1) * step);
            sr.triangle(
                position.x, position.y,
                position.x + (float) Math.cos(a1) * Guard.FOV_RANGE,
                position.y + (float) Math.sin(a1) * Guard.FOV_RANGE,
                position.x + (float) Math.cos(a2) * Guard.FOV_RANGE,
                position.y + (float) Math.sin(a2) * Guard.FOV_RANGE
            );
        }
        sr.setColor(fovColor.r, fovColor.g, fovColor.b, 0.75f * chasePulse);
        float lRad = (float) Math.toRadians(angle - Guard.FOV_HALF);
        float rRad = (float) Math.toRadians(angle + Guard.FOV_HALF);
        sr.line(position.x, position.y,
            position.x + (float) Math.cos(lRad) * Guard.FOV_RANGE,
            position.y + (float) Math.sin(lRad) * Guard.FOV_RANGE);
        sr.line(position.x, position.y,
            position.x + (float) Math.cos(rRad) * Guard.FOV_RANGE,
            position.y + (float) Math.sin(rRad) * Guard.FOV_RANGE);
    }

    public void renderSprite(SpriteBatch batch) {
        float half = SPRITE_SIZE / 2f;
        batch.draw(
            texAlien,
            position.x - half,
            position.y - half,
            half, half,
            SPRITE_SIZE, SPRITE_SIZE,
            1f, 1f,
            angle - 90f,
            0, 0,
            texAlien.getWidth(), texAlien.getHeight(),
            false, false
        );
    }

    public void dispose() { texAlien.dispose(); }

    public State getState() { return state; }
}

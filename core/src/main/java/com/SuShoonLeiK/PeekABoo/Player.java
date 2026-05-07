package com.SuShoonLeiK.PeekABoo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

public class Player {

    public Vector2 position;
    public Vector2 velocity = new Vector2(0, 0);
    public float   maxAcceleration = 300f;
    public float   angle       = 90f;
    public boolean caught      = false;
    public boolean beingChased = false;

    public static final float SPEED  = 120f;
    public static final float RADIUS =  14f;

    private static final float SPRITE_SIZE = 38f;

    private final Building       building;
    private final List<Obstacle> obstacles;
    private final Texture        texNormal;
    private final Texture        texScared;

    private final Vector2 tmpDesired = new Vector2();

    public Player(float x, float y, Building building, List<Obstacle> obstacles) {
        this.position  = new Vector2(x, y);
        this.building  = building;
        this.obstacles = obstacles;
        this.texNormal = new Texture(Gdx.files.internal("player.png"));
        this.texScared = new Texture(Gdx.files.internal("player_dead.png"));
    }

    public void update(float delta) {
        if (caught) {
            velocity.setZero();
            return;
        }

        float ix = 0, iy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  ix -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) ix += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    iy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  iy -= 1;

        if (ix != 0 || iy != 0) {
            float ilen = (float) Math.sqrt(ix * ix + iy * iy);
            ix /= ilen;
            iy /= ilen;
        }
        tmpDesired.set(ix * SPEED, iy * SPEED);

        tmpDesired.sub(velocity);
        float cap = maxAcceleration * delta;
        if (tmpDesired.len2() > cap * cap) {
            tmpDesired.setLength(cap);
        }
        velocity.add(tmpDesired);

        if (velocity.len2() > SPEED * SPEED) {
            velocity.setLength(SPEED);
        }

        if (velocity.len2() > 1f) {
            angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));
        }

        float r   = Player.RADIUS;
        float sw  = Gdx.graphics.getWidth();
        float sh  = Gdx.graphics.getHeight();

        float newX = Math.max(r, Math.min(sw - r, position.x + velocity.x * delta));
        float newY = Math.max(r, Math.min(sh - r, position.y + velocity.y * delta));

        if (canMoveTo(newX, newY, r)) {
            position.set(newX, newY);
        } else if (canMoveTo(newX, position.y, r)) {
            position.x = newX;
        } else if (canMoveTo(position.x, newY, r)) {
            position.y = newY;
        }
    }

    private boolean canMoveTo(float nx, float ny, float r) {
        if (building.overlaps(nx, ny, r)) return false;
        for (Obstacle o : obstacles) {
            if (o.overlaps(nx, ny, r)) return false;
        }
        return true;
    }

    public void renderSprite(SpriteBatch batch) {
        Texture tex  = beingChased ? texScared : texNormal;
        float   half = SPRITE_SIZE / 2f;
        batch.draw(tex,
            position.x - half, position.y - half,
            half, half,
            SPRITE_SIZE, SPRITE_SIZE,
            1f, 1f,
            angle - 90f,
            0, 0, tex.getWidth(), tex.getHeight(),
            false, false);
    }

    public void dispose() {
        texNormal.dispose();
        texScared.dispose();
    }


}

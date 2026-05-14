/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package anara.game;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Random;

public abstract class Entity {
    protected float x, y;
    public int maxHp, hp;
    public int attack, defense, speed;
    protected boolean alive = true;
    public float dx, dy; // velocity
    protected static final Random RNG = new Random();

    public Entity(float x, float y, int hp, int attack, int defense, int speed) {
        this.x = x; this.y = y;
        this.maxHp = hp; this.hp = hp;
        this.attack = attack; this.defense = defense; this.speed = speed;
    }

    public void takeDamage(int dmg) {
        int actual = Math.max(1, dmg - defense);
        hp = Math.max(0, hp - actual);
        if (hp <= 0) alive = false;
    }

    public float getHpRatio() { return (float) hp / maxHp; }
    public boolean isAlive() { return alive; }
    public float getX() { return x; }
    public float getY() { return y; }
    public Rectangle getBounds(int r) { return new Rectangle((int)x - r, (int)y - r, r * 2, r * 2); }

    public abstract void update(float targetX, float targetY, int mapW, int mapH);
    public abstract void draw(Graphics2D g2);
} 


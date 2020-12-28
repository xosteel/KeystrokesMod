package si.steel.keystrokes.overlay.particle;

public class Particle {

    private float posX, posY;
    private float velX, velY;
    private final long expiration;

    public Particle(float posX, float posY, float velX, float velY, long expiration) {
        this.posX = posX;
        this.posY = posY;
        this.velX = velX;
        this.velY = velY;
        this.expiration = expiration;
    }

    public void update() {
        posX += velX;
        posY += velY;
    }

    public long getExpiration() {
        return expiration;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosX() {
        return posX;
    }
}

package si.steel.keystrokes.overlay.particle;

import java.util.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.*;
import si.steel.keystrokes.*;

public class ParticleEmitter {

    private static final Random RANDOM = new Random();

    private List<Particle> particles = new ArrayList<>();

    public void emit() {
        float randomOffsetX = RANDOM.nextFloat() * 3;
        if (RANDOM.nextBoolean()) randomOffsetX *= -1;

        float randomOffsetY = RANDOM.nextFloat() * 3;
        if (RANDOM.nextBoolean()) randomOffsetY *= -1;

        for (float d = 0; d < 2 * Math.PI; d += Math.toRadians(20)) {
            float x = MathHelper.cos(d) + randomOffsetX;
            float y = MathHelper.sin(d) + randomOffsetY;

            float velX = 0.1f + RANDOM.nextFloat();
            if (RANDOM.nextBoolean()) velX *= -1;

            float velY = 0.1f + RANDOM.nextFloat();
            if (RANDOM.nextBoolean()) velY *= -1;

            particles.add(new Particle(x, y, velX, velY, KeystrokesMod.getCurrentTick() + (long) (5 + RANDOM.nextDouble() * 10)));
        }
    }

    public void tick() {
        particles.removeIf(p -> KeystrokesMod.getCurrentTick() - p.getExpiration() >= 0);
        particles.forEach(Particle::update);
    }

    public void render(int guiScaleFactor, boolean rainbow, int color) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        GL11.glPointSize(guiScaleFactor * 2);

        GL11.glBegin(GL11.GL_POINTS);

        if (!rainbow) Helper.setGlColor(color);

        int i = 0;

        for (Particle particle : particles) {
            if (rainbow) {
                double d = i++ / (particles.size() - 1D);
                Helper.setGlColor(Helper.rainbow(180 + (int) (d * 45)));
            }

            GL11.glVertex2f(particle.getPosX(), particle.getPosY());
        }

        GL11.glEnd();

        GL11.glPopAttrib();
    }
}

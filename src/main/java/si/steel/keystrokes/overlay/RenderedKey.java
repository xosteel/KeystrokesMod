package si.steel.keystrokes.overlay;

import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.settings.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
import si.steel.keystrokes.*;
import si.steel.keystrokes.overlay.particle.*;

public class RenderedKey {

    protected KeyBinding keyBinding;
    protected KeyConfiguration configuration;

    private ParticleEmitter particleEmitter;
    private double bounceFactor = 1;
    private long currentColors;
    private float colorFadeProgress;

    public RenderedKey(KeyBinding keyBinding, KeyConfiguration configuration) {
        this.keyBinding = keyBinding;
        this.configuration = configuration;
        this.particleEmitter = new ParticleEmitter();
    }

    @SubscribeEvent
    public final void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RenderedKey.this.onTick();
        }
    }

    @SubscribeEvent
    public final void onMouse(MouseEvent event) {
        if (keyBinding.getKeyCode() < 0 && event.buttonstate && event.button == keyBinding.getKeyCode() + 100) {
            RenderedKey.this.onPress();
        }
    }

    @SubscribeEvent
    public final void onKey(InputEvent.KeyInputEvent event) {
        if (keyBinding.getKeyCode() > 0 && Keyboard.getEventKey() == keyBinding.getKeyCode() && keyBinding.isKeyDown() && !Keyboard.isRepeatEvent()) {
            RenderedKey.this.onPress();
        }
    }

    public void render() {
        int guiScaleFactor = Helper.computeGuiScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        scissor(guiScaleFactor);

        GL11.glTranslated(configuration.centerX, configuration.centerY, 0);

        renderBase();
        if (configuration.particles) particleEmitter.render(guiScaleFactor, configuration.rainbowParticles, configuration.particleColor);

        boolean bounce = bounceFactor != 1;

        if (bounce) {
            GL11.glPushMatrix();
            GL11.glScaled(bounceFactor, bounceFactor, 1);
        }

        if (configuration.textScale == 1) {
            renderText();
        } else {
            GL11.glPushMatrix();
            GL11.glScaled(configuration.textScale, configuration.textScale, 1);
            renderText();
            GL11.glPopMatrix();
        }

        if (bounce) GL11.glPopMatrix();

        GL11.glTranslated(-configuration.centerX, -configuration.centerY, 0);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void renderBase() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        int colorA = (int) (currentColors >> 32L);
        int colorB = (int) (currentColors & 0xFFFFFFFFL);

        if (configuration.style == RenderStyle.RECTANGLE) {
            Helper.setGlColor(colorA);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-configuration.width / 2f, -configuration.height / 2f); // Top left
            GL11.glVertex2f(configuration.width / 2f, -configuration.height / 2f); // Top right
            Helper.setGlColor(colorB);
            GL11.glVertex2f(configuration.width / 2f, configuration.height / 2f); // Bottom right
            GL11.glVertex2f(-configuration.width / 2f, configuration.height / 2f); // Bottom left
        } else {
            double step = configuration.style == RenderStyle.ELLIPSE ? 15 : 60;
            step = Math.toRadians(step);

            GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
            GL11.glBegin(GL11.GL_POLYGON);

            for (float d = 0; d < 2 * Math.PI; d += step) {
                float x = MathHelper.cos(d) * configuration.width / 2f;
                float y = MathHelper.sin(d) * configuration.height / 2f;

                if (y < 0)
                    Helper.setGlColor(colorA);
                else
                    Helper.setGlColor(colorB);

                GL11.glVertex2f(x, y);
            }
        }

        GL11.glEnd();
        GL11.glPopAttrib();
    }

    protected void renderText() {
        String text = getDisplayText();
        if (text == null) return;

        int textWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        int textHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;

        Minecraft.getMinecraft().fontRendererObj.drawString(
                text,
                -textWidth / 2.0f + 0.5f,
                -textHeight / 2.0f + 1,
                getTextColor(),
                configuration.textShadow
        );
    }

    protected void onTick() {
        if (configuration.fades) {
            colorFadeProgress += ((getState() ? 1 : 0) - colorFadeProgress) / 2.2f;
            colorFadeProgress = Math.max(0, Math.min(1, colorFadeProgress));

            int inactiveColorA = (int) (configuration.baseColorsInactive >> 32L);
            int inactiveColorB = (int) (configuration.baseColorsInactive & 0xFFFFFFFFL);

            int activeColorA = (int) (configuration.baseColorsActive >> 32L);
            int activeColorB = (int) (configuration.baseColorsActive & 0xFFFFFFFFL);

            int colorA = interpolateColors(inactiveColorA, activeColorA, colorFadeProgress);
            int colorB = interpolateColors(inactiveColorB, activeColorB, colorFadeProgress);

            currentColors = colorA;
            currentColors <<= 32;
            currentColors |= colorB & 0xFFFFFFFFL;
        } else {
            currentColors = getState() ? configuration.baseColorsActive : configuration.baseColorsInactive;
        }

        if (configuration.particles) particleEmitter.tick();
        bounceFactor = Math.max(1, bounceFactor - 0.07);
    }

    private int interpolateColors(int colorA, int colorB, float factor) {
        int aA = colorA >> 24 & 0xFF;
        int rA = colorA >> 16 & 0xFF;
        int gA = colorA >> 8 & 0xFF;
        int bA = colorA & 0xFF;

        int aB = colorB >> 24 & 0xFF;
        int rB = colorB >> 16 & 0xFF;
        int gB = colorB >> 8 & 0xFF;
        int bB = colorB & 0xFF;

        int a = (int) interpolate(aA, aB, factor) & 0xFF;
        int r = (int) interpolate(rA, rB, factor) & 0xFF;
        int g = (int) interpolate(gA, gB, factor) & 0xFF;
        int b = (int) interpolate(bA, bB, factor) & 0xFF;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private float interpolate(float a, float b, float factor) {
        return a + (b - a) * factor;
    }

    protected void onPress() {
        if (configuration.particles) particleEmitter.emit();
        if (configuration.bounce) bounceFactor = 1.35;
    }

    private void scissor(int guiScaleFactor) {
        float x1 = configuration.centerX - configuration.width / 2f;
        float y1 = configuration.centerY - configuration.height / 2f;
        float x2 = configuration.centerX + configuration.width / 2f;
        float y2 = configuration.centerY + configuration.height / 2f;

        y1 = Math.min(y1, y2);
        y2 = Math.max(y1, y2);

        float displayWidth = Minecraft.getMinecraft().displayWidth;
        float displayHeight = Minecraft.getMinecraft().displayHeight;
        float factor = Display.getWidth() / (displayWidth / guiScaleFactor);
        GL11.glScissor((int) (x1 * factor), (int) ((Math.ceil(displayHeight / guiScaleFactor) - y2) * factor), (int) ((x2 - x1) * factor), (int) ((y2 - y1) * factor));
    }

    protected final int getTextColor() {
        if (configuration.rainbowText) {
            List<RenderedKey> keys = KeystrokesMod.getInstance().keys();
            return Helper.rainbow((int) (1D - keys.indexOf(this) / (keys.size() - 1D) * 90D));
        }

        return getState() ? configuration.textColorActive : configuration.textColorInactive;
    }

    protected boolean getState() {
        return keyBinding.isKeyDown();
    }

    public String getDisplayText() {
        if (keyBinding.getKeyCode() > 0)
            return Keyboard.getKeyName(keyBinding.getKeyCode());

        int keycode = keyBinding.getKeyCode() + 100;

        switch (keycode) {
            case 0:
                return "LMB";
            case 1:
                return "RMB";
            default:
                return "NONE";
        }
    }

    public KeyConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(KeyConfiguration configuration) {
        this.configuration = configuration;
    }

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }
}

package si.steel.keystrokes.gui.window.components;

import java.awt.*;
import java.util.function.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import org.lwjgl.opengl.*;
import si.steel.keystrokes.*;
import si.steel.keystrokes.gui.window.Component;

public class ColorPickerComponent extends Component {

    private String title;

    private boolean dragging;
    private int pickerX, pickerY;

    private Consumer<Integer> changeListener;
    private float currentHue;
    private int currentColor;

    public ColorPickerComponent(String title, int currentColor, Consumer<Integer> changeListener) {
        this.title = title;

        setWidth(100);
        setHeight(65);

        int r = currentColor >> 16 & 0xFF;
        int g = currentColor >> 8 & 0xFF;
        int b = currentColor & 0xFF;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);

        this.currentHue = hsb[0];
        this.currentColor = currentColor;

        pickerX = (int) (hsb[1] * getWidth());
        pickerY = (int) ((1 - hsb[2]) * getHeight());

        this.changeListener = changeListener;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            pickerX = Math.max(getX(), Math.min(getX() + getWidth(), mouseX));
            pickerY = Math.max(getY(), Math.min(getY() + getHeight(), mouseY));

            int color = computeColor();

            if (color != currentColor) {
                changeListener.accept(color);
                currentColor = color;
            }
        }

        drawPalette();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        fontRenderer.drawString(title, getX() + getWidth() / 2f - fontRenderer.getStringWidth(title) / 2f, getY() + 2, -1, true);
    }

    public void drawPalette() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);

        Helper.setGlColor(Color.HSBtoRGB(currentHue, 0, 1));
        GL11.glVertex2i(getX(), getY()); // Top left

        Helper.setGlColor(Color.HSBtoRGB(currentHue, 1, 1));
        GL11.glVertex2i(getX() + getWidth(), getY()); // Top right

        Helper.setGlColor(Color.HSBtoRGB(currentHue, 1, 0));
        GL11.glVertex2i(getX() + getWidth(), getY() + getHeight()); // Bottom right

        Helper.setGlColor(Color.HSBtoRGB(currentHue, 0, 0));
        GL11.glVertex2i(getX(), getY() + getHeight()); // Bottom left

        GL11.glEnd();

        GL11.glEnable(GL11.GL_POINT_SMOOTH);

        int pointScale = Helper.computeGuiScaleFactor();

        GL11.glPointSize(9 * pointScale / 2f);
        GL11.glColor4f(0, 0, 0, 1);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex2i(pickerX, pickerY);
        GL11.glEnd();

        GL11.glColor4f(1, 1, 1, 0.7f);
        GL11.glPointSize(7 * pointScale / 2f);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex2i(pickerX, pickerY);
        GL11.glEnd();

        GL11.glPopAttrib();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && mouseOver(mouseX, mouseY)) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            dragging = false;
        }
    }

    private int computeColor() {
        float dx = (float) (pickerX - getX()) / getWidth();
        float dy = (float) (pickerY - getY()) / getHeight();
        return Color.HSBtoRGB(currentHue, dx, 1 - dy);
    }

    @Override
    public void setX(int posX) {
        pickerX += posX - getX();
        super.setX(posX);
    }

    @Override
    public void setY(int posY) {
        pickerY += posY - getY();
        super.setY(posY);
    }

    public float getCurrentHue() {
        return currentHue;
    }

    public void setCurrentHue(float currentHue) {
        this.currentHue = currentHue;
        this.currentColor = computeColor();
        changeListener.accept(currentColor);
    }

    public int getCurrentColor() {
        return currentColor;
    }

    @Override
    public void keyTyped(int keyCode) {

    }
}
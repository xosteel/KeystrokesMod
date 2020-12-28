package si.steel.keystrokes.gui.window;

import si.steel.keystrokes.*;

public abstract class Component {

    private int posX, posY; // Top left corner
    private int width, height;

    public abstract void render(int mouseX, int mouseY, float partialTicks);

    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);

    public abstract void mouseReleased(int mouseX, int mouseY, int mouseButton);

    public abstract void keyTyped(int keyCode);

    public int getX() {
        return posX;
    }

    public void setX(int posX) {
        this.posX = posX;
    }

    public int getY() {
        return posY;
    }

    public void setY(int posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean mouseOver(int mouseX, int mouseY) {
        return Helper.isPointInsideRect(mouseX, mouseY, posX, posY, posX + width, posY + height);
    }
}

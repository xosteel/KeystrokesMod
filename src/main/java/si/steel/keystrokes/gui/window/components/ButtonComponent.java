package si.steel.keystrokes.gui.window.components;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import org.lwjgl.input.*;
import si.steel.keystrokes.gui.window.*;

public class ButtonComponent extends Component {

    private String text;
    private Runnable actionListener;
    private boolean mouseWasOver;

    public ButtonComponent(String text, Runnable actionListener) {
        this.text = text;
        this.actionListener = actionListener;

        int textWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        setWidth(textWidth + 15);
        setHeight(Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 4);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        boolean mouseOver = mouseOver(mouseX, mouseY);

        if (mouseOver) Gui.drawRect(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, Mouse.isButtonDown(0) ? -1 : 0xAAFFFFFF);
        Gui.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF1A1A1A);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        fontRenderer.drawString(text, getX() + getWidth() / 2f - fontRenderer.getStringWidth(text) / 2f, getY() + 3, mouseOver ? -1 : 0xFFCCCCCC, false);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        mouseWasOver = mouseButton == 0 && mouseOver(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseWasOver && mouseButton == 0 && mouseOver(mouseX, mouseY)) {
            actionListener.run();
            mouseWasOver = false;
        }
    }

    @Override
    public void keyTyped(int keyCode) {

    }
}

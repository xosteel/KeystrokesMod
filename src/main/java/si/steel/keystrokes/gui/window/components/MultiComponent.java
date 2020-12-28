package si.steel.keystrokes.gui.window.components;

import java.util.function.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import si.steel.keystrokes.*;
import si.steel.keystrokes.gui.window.*;

public class MultiComponent extends Component {

    private int selected;
    private Consumer<String> changeListener;
    private String[] options;

    public MultiComponent(Consumer<String> changeListener, int currentSelected, String... options) {
        this.selected = currentSelected;
        this.changeListener = changeListener;
        this.options = options;

        setWidth(100);
        setHeight(Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 4);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xff1a1a1a);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        fontRenderer.drawString("<", getX() + 3, getY() + 3.5f, mouseOverBack(mouseX, mouseY) ? -1 : 0xFF999999, false);
        Gui.drawRect(getX() + 12, getY(), getX() + 13, getY() + getHeight(), 0xff111111);

        fontRenderer.drawString(">", getX() + getWidth() - 7, getY() + 3.5f, mouseOverFront(mouseX, mouseY) ? -1 : 0xFF999999, false);
        Gui.drawRect(getX() + getWidth() - 12, getY(), getX() + getWidth() - 13, getY() + getHeight(), 0xff111111);

        fontRenderer.drawString(options[selected], getX() + getWidth() / 2f - fontRenderer.getStringWidth(options[selected]) / 2f, getY() + 3, -1, false);
    }

    private boolean mouseOverBack(int mouseX, int mouseY) {
        return Helper.isPointInsideRect(mouseX, mouseY, getX(), getY(), getX() + 12, getY() + getHeight());
    }

    private boolean mouseOverFront(int mouseX, int mouseY) {
        return Helper.isPointInsideRect(mouseX, mouseY, getX() + getWidth() - 12, getY(), getX() + getWidth(), getY() + getHeight());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;

        if (mouseOverBack(mouseX, mouseY)) {
            selected--;
            if (selected < 0) selected = options.length - 1;
            changeListener.accept(options[selected]);
        } else if (mouseOverFront(mouseX, mouseY)) {
            selected++;
            if (selected == options.length) selected = 0;
            changeListener.accept(options[selected]);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode) {

    }
}

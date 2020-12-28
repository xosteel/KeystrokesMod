package si.steel.keystrokes.gui.window.components;

import java.util.function.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import si.steel.keystrokes.gui.window.*;

public class BooleanComponent extends Component {

    private String title;
    private boolean state;
    private Consumer<Boolean> stateChangeListener;

    public BooleanComponent(String title, boolean state, Consumer<Boolean> stateChangeListener) {
        this.title = title;
        this.state = state;
        this.stateChangeListener = stateChangeListener;

        int textWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(title);
        setWidth(textWidth + 15);
        setHeight(Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 3);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x77555555);
        boolean mouseOver = mouseOver(mouseX, mouseY);

        Gui.drawRect(getX() + 1, getY() + 1, getX() + 11, getY() + getHeight() - 1, mouseOver ? 0x22fdfdfd : 0xFF212121);
        if (state) Gui.drawRect(getX() + 2, getY() + 2, getX() + 10, getY() + getHeight() - 2, mouseOver ? 0xFF00FF00 : 0xFF00AA00);

        Minecraft.getMinecraft().fontRendererObj.drawString(title, getX() + 13, getY() + 2, -1, false);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && mouseOver(mouseX, mouseY)) {
            state = !state;
            stateChangeListener.accept(state);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(int keyCode) {}
}

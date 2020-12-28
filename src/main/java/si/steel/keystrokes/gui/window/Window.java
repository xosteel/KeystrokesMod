package si.steel.keystrokes.gui.window;

import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
import si.steel.keystrokes.*;

public class Window extends Component {

    public static final int ALIGN_LEFT = -1;
    public static final int ALIGN_CENTER = 0;
    public static final int ALIGN_RIGHT = 1;

    private final WindowManager windowManager;
    protected String title;

    private boolean dragging;
    private int dragX, dragY;

    private Map<Integer, List<Component>> components = new HashMap<>();

    {
        components.put(ALIGN_LEFT, new ArrayList<>());
        components.put(ALIGN_CENTER, new ArrayList<>());
        components.put(ALIGN_RIGHT, new ArrayList<>());
    }

    public Window(WindowManager windowManager, String title) {
        this.windowManager = windowManager;
        this.title = title;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            setX(getX() + mouseX - dragX);
            setY(getY() + mouseY - dragY);
            dragX = mouseX;
            dragY = mouseY;
        }

        boolean mouseOverExit = mouseOverExitButton(mouseX, mouseY);

        Gui.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), windowManager.getTopmostWindow() == this ? 0x550000FF : 0xBB000000);

        Gui.drawRect(getX(), getY() + 9, getX() + getWidth(), getY() + 10, -1);
        Gui.drawRect(getX() + getWidth() - 10, getY(), getX() + getWidth() - 9, getY() + 9, -1);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        if (!mouseOverExit) fontRenderer.drawString("-", getX() + getWidth() - 7, getY() + 1.5f, -1, false);

        Gui.drawRect(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, 0x55555555);
        Gui.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xBB000000);

        if (mouseOverExit) fontRenderer.drawString("-", getX() + getWidth() - 7, getY() + 1.5f, -1, false);

        renderTitle();
        components.values().forEach(l -> l.forEach(c -> c.render(mouseX, mouseY, partialTicks)));
    }

    protected boolean mouseOverExitButton(int mouseX, int mouseY) {
        return Helper.isPointInsideRect(mouseX, mouseY, getX() + getWidth() - 10, getY(), getX() + getWidth(), getY() + 9);
    }

    protected void renderTitle() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        GL11.glPushMatrix();
        GL11.glTranslated(getX() + getWidth() / 2D - fontRenderer.getStringWidth(title) * 0.75 / 2 - 3.5, getY() + 1.75f, 0);
        GL11.glScaled(0.75, 0.75, 1);
        fontRenderer.drawString(title, 0, 0, -1, false);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && mouseOverExitButton(mouseX, mouseY)) {
            closeWindow();
            return;
        }

        if (mouseButton == 0 && Helper.isPointInsideRect(mouseX, mouseY, getX(), getY(), getX() + getWidth(), getY() + 9)) {
            dragging = true;
            dragX = mouseX;
            dragY = mouseY;
            return;
        }

        Component component = getMouseComponent(mouseX, mouseY);
        if (component != null) component.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (dragging && mouseButton == 0) {
            dragging = false;
            return;
        }

        if (!dragging) {
            components.values().forEach(l -> l.forEach(c -> c.mouseReleased(mouseX, mouseY, mouseButton)));
        }
    }

    @Override
    public void keyTyped(int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closeWindow();
        }
    }

    public void addComponents(int align, Component... components) {
        addComponents(align, 0, 0, components);
    }

    public void addComponents(int align, int xOffset, int yOffset, Component... components) {
        int y = getY() + 13 + yOffset;

        if (align == ALIGN_LEFT) {
            int x = getX() + 3 + xOffset;

            for (Component component : components) {
                if (component == null) continue;
                component.setX(x);
                component.setY(y);
                y += component.getHeight() + 3;
            }
        }

        if (align == ALIGN_CENTER) {
            int x = getX() + getWidth() / 2 + xOffset;

            for (Component component : components) {
                if (component == null) continue;
                component.setX(x - component.getWidth() / 2);
                component.setY(y);
                y += component.getHeight() + 3;
            }
        }

        if (align == ALIGN_RIGHT) {
            int x = getX() + getWidth() - 3 + xOffset;

            for (Component component : components) {
                if (component == null) continue;
                component.setX(x - component.getWidth());
                component.setY(y);
                y += component.getHeight() + 3;
            }
        }

        Arrays.stream(components).filter(Objects::nonNull).forEach(this.components.get(align)::add);
    }

    public void closeWindow() {
        windowManager.closeWindow(this);
    }

    // Get component under mouse
    private Component getMouseComponent(int mouseX, int mouseY) {
        for (List<Component> components : components.values()) {
            Component component = components.stream().filter(c -> c.mouseOver(mouseX, mouseY)).findFirst().orElse(null);

            if (component != null) {
                return component;
            }
        }

        return null;
    }

    @Override
    public void setX(int posX) {
        components.values().forEach(l -> l.forEach(c -> c.setX(c.getX() + posX - getX())));
        super.setX(posX);
    }

    @Override
    public void setY(int posY) {
        components.values().forEach(l -> l.forEach(c -> c.setY(c.getY() + posY - getY())));
        super.setY(posY);
    }
}

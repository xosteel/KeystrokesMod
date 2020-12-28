package si.steel.keystrokes.gui.window.windows;

import java.util.function.*;
import net.minecraft.client.gui.*;
import si.steel.keystrokes.*;
import si.steel.keystrokes.gui.window.*;

public class GradientColorPickerWindow extends Window {

    public GradientColorPickerWindow(WindowManager windowManager, String title, Pair<Integer, Integer> colors, Pair<Consumer<Integer>, Consumer<Integer>> changeListeners) {
        super(windowManager, title);

        setWidth(235);
        setHeight(150);

        Component[] picker1 = ColorPickerWindow.makeComponents("Color 1", colors.getKey(), changeListeners.getKey());
        addComponents(Window.ALIGN_LEFT, 3, 0, picker1[0]); // Add the actual color picker component

        picker1[0] = null;
        addComponents(Window.ALIGN_LEFT, 12, 70, picker1); // Add the sliders

        // Do the same for the other color picker
        Component[] picker2 = ColorPickerWindow.makeComponents("Color 2", colors.getValue(), changeListeners.getValue());
        addComponents(Window.ALIGN_RIGHT, -3, 0, picker2[0]);

        picker2[0] = null;
        addComponents(Window.ALIGN_RIGHT, -12, 70, picker2);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(getX() + getWidth() / 2 - 1, getY() + 10, getX() + getWidth() / 2 + 1, getY() + getHeight(), -1);
        super.render(mouseX, mouseY, partialTicks);
    }
}
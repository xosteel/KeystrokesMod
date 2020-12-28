package si.steel.keystrokes.gui.window.windows;

import java.util.function.*;
import si.steel.keystrokes.gui.window.*;
import si.steel.keystrokes.gui.window.components.*;

public class ColorPickerWindow extends Window {

    public ColorPickerWindow(WindowManager windowManager, String title, int currentColor, Consumer<Integer> colorChangeListener) {
        super(windowManager, title);

        setWidth(110);
        setHeight(150);

        addComponents(Window.ALIGN_CENTER, makeComponents("", currentColor, colorChangeListener));
    }

    public static Component[] makeComponents(String title, int currentColor, Consumer<Integer> colorChangeListener) {
        ColorPickerComponent picker = new ColorPickerComponent(title, currentColor, colorChangeListener);

        return new Component[] {
                picker,
                new SliderComponent("Hue", 0, 360, 5, picker.getCurrentHue() * 360, val -> picker.setCurrentHue(val.intValue() / 360f)),
                new SliderComponent("Transparency", 0, 100, 5, (int) (((currentColor >> 24) & 0xFF) / 255D * 100), val -> {
                    int color = picker.getCurrentColor();

                    int a = (int) ((val.intValue() / 100D) * 255);
                    int r = color >> 16 & 0xFF;
                    int g = color >> 8 & 0xFF;
                    int b = color & 0xFF;

                    color = (a << 24) | (r << 16) | (g << 8) | (b & 0xFF);

                    colorChangeListener.accept(color);
                })
        };
    }
}

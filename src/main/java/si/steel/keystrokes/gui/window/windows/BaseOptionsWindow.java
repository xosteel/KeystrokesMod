package si.steel.keystrokes.gui.window.windows;

import java.util.*;
import java.util.function.*;
import si.steel.keystrokes.*;
import si.steel.keystrokes.gui.window.*;
import si.steel.keystrokes.gui.window.components.*;
import si.steel.keystrokes.overlay.*;

public class BaseOptionsWindow extends SubConfigWindow {

    private Window currentChild;

    public BaseOptionsWindow(WindowManager windowManager, RenderedKey k) {
        super(windowManager);
        setKey(k);

        setWidth(220);
        setHeight(75);

        addComponents(Window.ALIGN_CENTER, new MultiComponent(a -> key.getConfiguration().style = RenderStyle.valueOf(a), key.getConfiguration().style.ordinal(),
                Arrays.stream(RenderStyle.values()).sorted(Comparator.comparing(RenderStyle::ordinal)).map(Enum::toString).toArray(String[]::new)));

        ButtonComponent heldColorButton = new ButtonComponent("Base Color (held)", () -> {
            if (currentChild != null) currentChild.closeWindow();

            long colors = key.getConfiguration().baseColorsActive;
            int colorA = (int) (colors >> 32L);
            int colorB = (int) (colors & 0xFFFFFFFFL);

            currentChild = new GradientColorPickerWindow(windowManager, "Base Color (held) (gradient)",
                    Pair.of(colorA, colorB),
                    makeListenerPair(
                            () -> key.getConfiguration().baseColorsActive,
                            col -> key.getConfiguration().baseColorsActive = col
                    )
            );

            currentChild.setX(getX());
            currentChild.setY(getY());
            windowManager.openWindow(currentChild);
        });

        addComponents(Window.ALIGN_RIGHT, 0, 17, heldColorButton);

        ButtonComponent colorButton = new ButtonComponent("Base Color", () -> {
            if (currentChild != null) currentChild.closeWindow();

            long colors = key.getConfiguration().baseColorsInactive;
            int colorA = (int) (colors >> 32L);
            int colorB = (int) (colors & 0xFFFFFFFFL);

            currentChild = new GradientColorPickerWindow(windowManager, "Base Color (gradient)",
                    Pair.of(colorA, colorB),
                    makeListenerPair(
                            () -> key.getConfiguration().baseColorsInactive,
                            col -> key.getConfiguration().baseColorsInactive = col
                    )
            );

            currentChild.setX(getX());
            currentChild.setY(getY());
            windowManager.openWindow(currentChild);
        });

        colorButton.setWidth(heldColorButton.getWidth());
        addComponents(Window.ALIGN_LEFT, 0, 17, colorButton);

        addComponents(Window.ALIGN_CENTER, 0, 33,
                new BooleanComponent("Bounce", key.getConfiguration().bounce, b -> key.getConfiguration().bounce = b),
                new BooleanComponent("Fade", key.getConfiguration().fades, b -> key.getConfiguration().fades = b)
        );
    }

    private Pair<Consumer<Integer>, Consumer<Integer>> makeListenerPair(Supplier<Long> currentColorSupplier, Consumer<Long> callback) {
        return Pair.of(val -> {
            long current = currentColorSupplier.get();
            int b = (int) (current & 0xFFFFFFFFL);
            current = val;
            current <<= 32;
            current |= b & 0xFFFFFFFFL;
            callback.accept(current);
        }, val -> {
            long current = currentColorSupplier.get();
            int a = (int) (current >> 32L);
            current = a;
            current <<= 32;
            current |= val & 0xFFFFFFFFL;
            callback.accept(current);
        });
    }

    @Override
    public void setKey(RenderedKey key) {
        super.setKey(key);
        this.title = "Base Options for " + key.getDisplayText();
    }

    @Override
    public void closeWindow() {
        if (currentChild != null) currentChild.closeWindow();
        super.closeWindow();
    }
}

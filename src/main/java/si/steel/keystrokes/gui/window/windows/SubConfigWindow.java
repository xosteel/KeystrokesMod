package si.steel.keystrokes.gui.window.windows;

import si.steel.keystrokes.gui.window.*;
import si.steel.keystrokes.overlay.*;

public class SubConfigWindow extends Window {

    protected RenderedKey key;

    public SubConfigWindow(WindowManager windowManager) {
        super(windowManager, "");
    }

    public void setKey(RenderedKey key) {
        this.key = key;
    }
}

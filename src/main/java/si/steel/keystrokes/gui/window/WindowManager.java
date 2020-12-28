package si.steel.keystrokes.gui.window;

import java.util.*;

public class WindowManager {

    private List<Window> windows = new ArrayList<>();

    public void drawWindows(int mouseX, int mouseY, float partialTicks) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window w = windows.get(i);
            w.render(i == 0 ? mouseX : -1, i == 0 ? mouseY : -1, partialTicks);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!windows.isEmpty()) {
            Window topMost = null;

            for (Window window : windows) {
                if (window.mouseOver(mouseX, mouseY)) {
                    topMost = window;
                    break;
                }
            }

            if (topMost != null && windows.get(0) != topMost) {
                windows.remove(topMost);
                windows.add(0, topMost);
            }

            windows.get(0).mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (!windows.isEmpty()) {
            windows.get(0).mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    public void keyTyped(int keyCode) {
        if (!windows.isEmpty()) {
            windows.get(0).keyTyped(keyCode);
        }
    }

    public void openWindow(Window window) {
        windows.remove(window);
        windows.add(0, window);
    }

    public void closeWindow(Window window) {
        windows.remove(window);
    }

    public boolean hasOpenWindows() {
        return windows.size() != 0;
    }

    public Window getTopmostWindow() {
        if (windows.isEmpty()) return null;
        return windows.get(0);
    }
}

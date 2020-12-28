package si.steel.keystrokes.gui;

import com.google.common.collect.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import net.minecraft.client.gui.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
import si.steel.keystrokes.*;
import si.steel.keystrokes.gui.window.Window;
import si.steel.keystrokes.gui.window.*;
import si.steel.keystrokes.gui.window.components.*;
import si.steel.keystrokes.gui.window.windows.*;
import si.steel.keystrokes.overlay.*;

public class KeystrokesModGUI extends GuiScreen {

    private RenderedKey dragging; // Key currently being moved around/resized
    private boolean draggingSize; // Is the key being resized instead of moved around
    private int draggingSizeXM, draggingSizeYM; // The direction of the resize (which blue rectangle is being dragged)

    private int previousMouseX, previousMouseY; // The position of the mouse on the previous render tick

    private int selectionX = -1, selectionY = -1; // The starting position of the selection rectangle
    private Set<RenderedKey> selection = new HashSet<>(); // Currently selected keys

    private WindowManager windowManager = new WindowManager();
    private ButtonComponent factoryResetButton, openConfigButton;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        factoryResetButton = new ButtonComponent("Factory Reset", () -> {
            KeystrokesMod.getInstance().initializeDefaultKeys();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        });

        factoryResetButton.setX(width - factoryResetButton.getWidth() - 3);
        factoryResetButton.setY(height - factoryResetButton.getHeight() - 3);

        openConfigButton = new ButtonComponent("Open Config File", () -> {
            try {
                Desktop.getDesktop().open(KeystrokesMod.getInstance().getConfigFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        openConfigButton.setX(width - openConfigButton.getWidth() - 3);
        openConfigButton.setY(height - openConfigButton.getHeight() - factoryResetButton.getHeight() - 6);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        try {
            KeystrokesMod.getInstance().saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int wmx = mouseX, wmy = mouseY;

        if (windowManager.hasOpenWindows()) {
            mouseX = mouseY = -1;
        }

        if (dragging != null) {
            KeyConfiguration cfg = dragging.getConfiguration();

            if (draggingSize) {
                int mx = clampToGrid(mouseX);
                int my = clampToGrid(mouseY);

                int draggingSizeX = cfg.centerX + draggingSizeXM * cfg.width / 2;
                int draggingSizeY = cfg.centerY + draggingSizeYM * cfg.height / 2;

                int x = -1 * draggingSizeXM * draggingSizeX - mx * draggingSizeXM * -1;
                int y = -1 * draggingSizeYM * draggingSizeY - my * draggingSizeYM * -1;

                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    x = Math.max(x, y);
                    y = x;
                }

                resize(cfg, x, y);

                for (RenderedKey key : selection) {
                    if (key != dragging) {
                        resize(key.getConfiguration(), x, y);
                    }
                }
            } else {
                int x = clampToGrid(mouseX) - clampToGrid(previousMouseX);
                int y = clampToGrid(mouseY) - clampToGrid(previousMouseY);

                cfg.centerX += x;
                cfg.centerY += y;

                if (selection.contains(dragging)) {
                    moveSelection(x, y);
                }
            }
        }

        previousMouseX = mouseX;
        previousMouseY = mouseY;

        drawRect(0, 0, width, height, 0xAA000000);
        renderHelpLabels();

        factoryResetButton.render(mouseX, mouseY, partialTicks);
        openConfigButton.render(mouseX, mouseY, partialTicks);

        boolean hovering = false;

        for (RenderedKey key : KeystrokesMod.getInstance().keys()) {
            KeyConfiguration cfg = key.getConfiguration();

            int x1 = cfg.centerX - cfg.width / 2;
            int x2 = cfg.centerX + cfg.width / 2;
            int y1 = cfg.centerY - cfg.height / 2;
            int y2 = cfg.centerY + cfg.height / 2;

            boolean renderOutline = !hovering && (dragging == key || dragging == null && Helper.isPointInsideRect(mouseX, mouseY, x1 - 2, y1 - 2, x2 + 2, y2 + 2));

            if (selection.contains(key) || (dragging != null && !draggingSize) || renderOutline) {
                int color = selection.contains(key) ? 0xAA00FF00 : dragging != null ? 0x88FFFFFF : 0xFFFFFFFF;

                drawHorizontalLine(x1 - 1, x2, y1 - 1, color); // top
                drawHorizontalLine(x1 - 1, x2, y2, color); // bottom
                drawVerticalLine(x1 - 1, y2, y1 - 1, color); // left
                drawVerticalLine(x2, y2, y1 - 1, color); // right
            }

            key.render();

            // Render the resize "grabbers"
            if (renderOutline) {
                hovering = true;

                int color = dragging != null ? 0x880055FF : 0xFF0055FF;
                int colorHover = dragging != null ? 0x8800AAFF : 0xFF00AAFF;

                drawRect(x1 - 2, y1 - 2, x1 + 2, y1 + 2, Helper.isPointInsideRect(mouseX, mouseY, x1 - 2, y1 - 2, x1 + 2, y1 + 2) ? colorHover : color);
                drawRect(x2 - 2, y1 - 2, x2 + 2, y1 + 2, Helper.isPointInsideRect(mouseX, mouseY, x2 - 2, y1 - 2, x2 + 2, y1 + 2) ? colorHover : color);
                drawRect(x1 - 2, y2 - 2, x1 + 2, y2 + 2, Helper.isPointInsideRect(mouseX, mouseY, x1 - 2, y2 - 2, x1 + 2, y2 + 2) ? colorHover : color);
                drawRect(x2 - 2, y2 - 2, x2 + 2, y2 + 2, Helper.isPointInsideRect(mouseX, mouseY, x2 - 2, y2 - 2, x2 + 2, y2 + 2) ? colorHover : color);
            }
            //

            // Render the XY/WH text
            if (dragging == key) {
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                if (draggingSize) {
                    String text = "w: " + cfg.width;
                    int textWidth = fontRendererObj.getStringWidth(text);
                    fontRendererObj.drawString(text, cfg.centerX - textWidth / 2f, cfg.centerY - cfg.height / 2f - fontRendererObj.FONT_HEIGHT, 0xAAFFFFFF, true);

                    text = "h: " + cfg.height;
                    fontRendererObj.drawString(text, cfg.centerX + cfg.width / 2f + 2, cfg.centerY - fontRendererObj.FONT_HEIGHT / 2f, 0xAAFFFFFF, true);
                } else if (selection.size() <= 1) {
                    String text = "x: " + cfg.centerX;
                    int textWidth = fontRendererObj.getStringWidth(text);
                    fontRendererObj.drawString(text, cfg.centerX - textWidth / 2f, cfg.centerY - cfg.height / 2f - fontRendererObj.FONT_HEIGHT, 0xAAFFFFFF, true);

                    text = "y: " + cfg.centerY;
                    fontRendererObj.drawString(text, cfg.centerX + cfg.width / 2f + 2, cfg.centerY - fontRendererObj.FONT_HEIGHT / 2f, 0xAAFFFFFF, true);
                }

                GL11.glPopAttrib();
            }
            //
        }

        if (windowManager.hasOpenWindows()) {
            windowManager.drawWindows(wmx, wmy, partialTicks);
        } else if (selectionX != -1 && selectionY != -1) {
            // Render the selection rectangle
            drawRect(selectionX, selectionY, mouseX, mouseY, 0xAAFFFFFF);
        }
    }

    private void renderHelpLabels() {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTranslatef(0, height, 0);

        Consumer<String> renderLabel = text -> {
            float tx = width / 2f - fontRendererObj.getStringWidth(text) / 2f;
            GL11.glTranslatef(tx, -fontRendererObj.FONT_HEIGHT - 1, 0);
            fontRendererObj.drawStringWithShadow(text, 0, 0, 0xCCFFFFFF);
            GL11.glTranslatef(-tx, 0, 0);
        };

        renderLabel.accept("Hold left shift while resizing a key to maintain ratio");
        renderLabel.accept("Left click & drag to select multiple keys at once");
        renderLabel.accept("Right click a key to open its configuration menu");
        renderLabel.accept("Left click & drag on a key to move it");

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (windowManager.hasOpenWindows()) {
            windowManager.keyTyped(keyCode);
            return;
        }

        super.keyTyped(typedChar, keyCode);

        if (!selection.isEmpty()) {
            int mod = 6;

            switch (keyCode) {
                case Keyboard.KEY_UP:
                    moveSelection(0, -mod);
                    break;
                case Keyboard.KEY_DOWN:
                    moveSelection(0, mod);
                    break;
                case Keyboard.KEY_LEFT:
                    moveSelection(-mod, 0);
                    break;
                case Keyboard.KEY_RIGHT:
                    moveSelection(mod, 0);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (windowManager.hasOpenWindows()) {
            windowManager.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (mouseButton == 0 && dragging == null || mouseButton == 1) {
            List<RenderedKey> keys = KeystrokesMod.getInstance().keys();

            for (int i = keys.size() - 1; i >= 0; i--) {
                RenderedKey key = keys.get(i);
                KeyConfiguration cfg = key.getConfiguration();

                int x1 = cfg.centerX - cfg.width / 2;
                int x2 = cfg.centerX + cfg.width / 2;
                int y1 = cfg.centerY - cfg.height / 2;
                int y2 = cfg.centerY + cfg.height / 2;

                if (mouseButton == 1) {
                    if (Helper.isPointInsideRect(mouseX, mouseY, x1, y1, x2, y2)) {
                        Window window;

                        if (selection.size() > 1) { // A "hacky" implementation of editing multiple keys at once
                            Set<String> excludedFields = Sets.newHashSet("centerX", "centerY", "width", "height");

                            KeyConfiguration newConfig = key.getConfiguration().copy(excludedFields);
                            KeyConfiguration newConfigInitial = newConfig.copy(excludedFields);

                            RenderedKey[] editing = selection.toArray(new RenderedKey[0]);

                            RenderedKey editKey = new RenderedKey(key.getKeyBinding(), newConfig) {
                                @Override
                                public String getDisplayText() {
                                    return editing.length + " selected keys";
                                }
                            };

                            window = new KeyConfigWindow(windowManager, editKey) {
                                @Override
                                public void closeWindow() {
                                    super.closeWindow();

                                    for (RenderedKey key : editing) {
                                        key.getConfiguration().setDiff(newConfigInitial, newConfig, excludedFields);
                                    }
                                }
                            };
                        } else {
                            window = new KeyConfigWindow(windowManager, key);
                        }

                        int wpx = x2 + 5;
                        if (wpx + window.getWidth() > this.width - 5)
                            wpx = x1 - 5 - window.getWidth();

                        int wpy = y1;
                        if (wpy + window.getHeight() > this.height - 5)
                            wpy = y1 - 5 - window.getHeight();

                        window.setX(wpx);
                        window.setY(wpy);

                        mouseReleased(mouseX, mouseY, 0);
                        windowManager.openWindow(window);
                        return;
                    }

                    if (i == 0) return;
                    else continue;
                }

                boolean tl = Helper.isPointInsideRect(mouseX, mouseY, x1 - 2, y1 - 2, x1 + 2, y1 + 2); // top left
                boolean tr = Helper.isPointInsideRect(mouseX, mouseY, x2 - 2, y1 - 2, x2 + 2, y1 + 2); // top right
                boolean bl = Helper.isPointInsideRect(mouseX, mouseY, x1 - 2, y2 - 2, x1 + 2, y2 + 2); // bottom left
                boolean br = Helper.isPointInsideRect(mouseX, mouseY, x2 - 2, y2 - 2, x2 + 2, y2 + 2); // bottom right

                if (tl || tr || bl || br) { // Dragging one of the resize "grabbers"?
                    draggingSize = true;
                    dragging = key;

                    if (tl) {
                        draggingSizeXM = -1;
                        draggingSizeYM = -1;
                    } else if (tr) {
                        draggingSizeXM = 1;
                        draggingSizeYM = -1;
                    } else if (bl) {
                        draggingSizeXM = -1;
                        draggingSizeYM = 1;
                    } else {
                        draggingSizeXM = 1;
                        draggingSizeYM = 1;
                    }

                    break;
                }

                if (Helper.isPointInsideRect(mouseX, mouseY, x1, y1, x2, y2)) { // Dragging the key itself?
                    draggingSize = false;
                    dragging = key;
                    break;
                }
            }

            if (dragging != null) { // Make the key that's being dragged render on top of all the other keys
                keys.remove(dragging);
                keys.add(dragging);
            } else {
                selection.clear();
                selectionX = mouseX;
                selectionY = mouseY;
            }
        }

        factoryResetButton.mouseClicked(mouseX, mouseY, mouseButton);
        openConfigButton.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (windowManager.hasOpenWindows()) {
            windowManager.mouseReleased(mouseX, mouseY, state);
            return;
        }

        if (state == 0) {
            if (selectionX != -1 && selectionY != -1) { // If there is a selection, add all the keys inside the selection to the selection set.
                int sx = Math.min(mouseX, selectionX);
                int sy = Math.min(mouseY, selectionY);
                Rectangle selectionRect = new Rectangle(sx, sy, Math.max(mouseX, selectionX) - sx, Math.max(mouseY, selectionY) - sy);

                for (RenderedKey key : KeystrokesMod.getInstance().keys()) {
                    KeyConfiguration cfg = key.getConfiguration();

                    int x = cfg.centerX - cfg.width / 2;
                    int y = cfg.centerY - cfg.height / 2;

                    Rectangle keyRect = new Rectangle(x, y, cfg.centerX + cfg.width / 2 - x, cfg.centerY + cfg.height / 2 - y);

                    if (selectionRect.intersects(keyRect)) {
                        selection.add(key);
                    }
                }

                selectionX = -1;
                selectionY = -1;
            }

            dragging = null;
            draggingSize = false;
            draggingSizeXM = 0;
            draggingSizeYM = 0;
        }

        factoryResetButton.mouseReleased(mouseX, mouseY, state);
        openConfigButton.mouseReleased(mouseX, mouseY, state);
    }

    private void moveSelection(int dx, int dy) {
        selection.stream().filter(k -> k != dragging).forEach(key -> {
            key.getConfiguration().centerX += dx;
            key.getConfiguration().centerY += dy;
        });
    }

    private void resize(KeyConfiguration cfg, int dx, int dy) {
        int textWidth = fontRendererObj.getStringWidth(dragging.getDisplayText()) + 1;

        int x1 = cfg.centerX - cfg.width / 2;
        cfg.width = Math.max(textWidth, cfg.width + dx);
        int x2 = cfg.centerX - cfg.width / 2;
        cfg.centerX += (x1 - x2) * draggingSizeXM;

        int y1 = cfg.centerY - cfg.height / 2;
        cfg.height = Math.max(10, cfg.height + dy);
        int y2 = cfg.centerY - cfg.height / 2;
        cfg.centerY += (y1 - y2) * draggingSizeYM;
    }

    private int clampToGrid(int val) {
        return (int) (Math.round(val / 2.0) * 2);
    }
}
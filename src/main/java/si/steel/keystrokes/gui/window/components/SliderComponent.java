package si.steel.keystrokes.gui.window.components;

import java.util.function.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import si.steel.keystrokes.gui.window.*;

public class SliderComponent extends Component {

    private String title;
    private double min, max, step;
    private double val;
    private Consumer<Double> changeListener;

    private boolean dragging;

    public SliderComponent(String title, double min, double max, double step, double initialValue, Consumer<Double> changeListener) {
        this.title = title;
        this.min = min;
        this.max = max;
        this.step = step;
        this.val = constrainValue(initialValue);
        this.changeListener = changeListener;
        setWidth(80);
        setHeight(30);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (dragging && mouseX >= getX() && mouseX <= getX() + getWidth()) {
            double d = (mouseX - getX()) / (double) getWidth();
            double newVal = min + (max - min) * d;
            newVal = constrainValue(newVal);

            if (newVal != val) {
                val = newVal;
                changeListener.accept(val);
            }
        }

        boolean mouseOver = mouseOver(mouseX, mouseY);

        Gui.drawRect(getX(), getY() + getHeight() / 2 - 1, getX() + getWidth(), getY() + getHeight() / 2 + 1, 0xFF111111);

        double d = (val - min) / (max - min);
        int progress = (int) (d * getWidth());

        Gui.drawRect(getX(), getY() + getHeight() / 2 - 1, getX() + progress, getY() + getHeight() / 2 + 1, mouseOver ? 0xFF00FF00 : 0xFF00CC00);

        Gui.drawRect(getX() + progress - 1, getY() + getHeight() / 2 - 4, getX() + progress + 1, getY() + getHeight() / 2 + 4, mouseOver ? 0xFFFF0000 : 0xFFCC0000);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        String valueText = (((int) val) == val ? String.valueOf((int) val) : String.valueOf(val));
        String text = title == null ? valueText : title + " (" + valueText + ")";
        fontRenderer.drawString(text, getX() + getWidth() / 2 - fontRenderer.getStringWidth(text) / 2, getY() + fontRenderer.FONT_HEIGHT / 2 - 2, -1);

        text = ((int) min) == min ? String.valueOf((int) min) : String.valueOf(min);
        fontRenderer.drawString(text, getX() - fontRenderer.getStringWidth(text) / 2, getY() + getHeight() - fontRenderer.FONT_HEIGHT, -1);

        text = ((int) max) == max ? String.valueOf((int) max) : String.valueOf(max);
        fontRenderer.drawString(text, getX() + getWidth() - fontRenderer.getStringWidth(text) / 2, getY() + getHeight() - fontRenderer.FONT_HEIGHT, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && mouseOver(mouseX, mouseY)) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            dragging = false;
        }
    }

    @Override
    public void keyTyped(int keyCode) {

    }

    private double constrainValue(double d) {
        d = step * Math.round(d / step);
        d = (int) (d * 100) / 100D;
        d = Math.max(min, Math.min(max, d));
        return d;
    }
}

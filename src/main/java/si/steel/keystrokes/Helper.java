package si.steel.keystrokes;

import java.awt.*;
import net.minecraft.client.*;
import org.lwjgl.opengl.*;

public class Helper {

    public static boolean isPointInsideRect(int pointX, int pointY, int rectX1, int rectY1, int rectX2, int rectY2) {
        return pointX >= rectX1 && pointX <= rectX2 && pointY >= rectY1 && pointY <= rectY2;
    }

    public static void setGlColor(int color) {
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        GL11.glColor4f(r, g, b, a);
    }

    public static int rainbow(int phaseOffset) {
        return Color.HSBtoRGB((KeystrokesMod.getCurrentTick() + phaseOffset) % 360.0f / 360.0f, 1, 1);
    }

    public static int computeGuiScaleFactor() { // Taken straight from ScaledResolution.class
        int i = Minecraft.getMinecraft().gameSettings.guiScale;
        if (i == 0) i = 1000;

        int guiScaleFactor = 1;

        while (guiScaleFactor < i && Minecraft.getMinecraft().displayWidth / (guiScaleFactor + 1) >= 320 && (Minecraft.getMinecraft().displayHeight / (guiScaleFactor + 1)) >= 240) {
            guiScaleFactor++;
        }

        if (guiScaleFactor != 1 && guiScaleFactor % 2 != 0 && Minecraft.getMinecraft().isUnicode()) {
            guiScaleFactor--;
        }

        return guiScaleFactor;
    }
}

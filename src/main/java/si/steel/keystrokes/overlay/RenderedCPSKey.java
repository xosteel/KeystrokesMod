package si.steel.keystrokes.overlay;

import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.settings.*;
import org.lwjgl.opengl.*;
import si.steel.keystrokes.*;

public class RenderedCPSKey extends RenderedKey {

    private List<Long> cpsList = new ArrayList<>(15);

    public RenderedCPSKey(KeyBinding keyBinding, KeyConfiguration configuration) {
        super(keyBinding, configuration);
    }

    @Override
    protected void onPress() {
        super.onPress();
        cpsList.add(KeystrokesMod.getCurrentTick());
    }

    @Override
    protected void onTick() {
        super.onTick();
        cpsList.removeIf(l -> KeystrokesMod.getCurrentTick() - l > 20);
    }

    @Override
    protected void renderText() {
        int textHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        float scale = 0.75f;

        GL11.glPushMatrix();
        GL11.glTranslatef(0, (textHeight / 2f + textHeight / 2f * scale) / 2f, 0);

        float t = -configuration.height / 2f + 3;

        if (configuration.style != RenderStyle.RECTANGLE)
            t += (configuration.height - 20) / 5.0;

        GL11.glTranslatef(0, t, 0);
        super.renderText();
        GL11.glTranslatef(0, -t, 0);

        GL11.glScaled(scale, scale, 1);

        String text = cpsList.size() + " CPS";
        int textWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, -textWidth / 2.0f + 0.5f, -textHeight / 2.0f * scale + 1, getTextColor(), configuration.textShadow);

        GL11.glPopMatrix();
    }
}

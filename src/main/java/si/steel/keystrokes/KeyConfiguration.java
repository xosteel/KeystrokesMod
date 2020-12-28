package si.steel.keystrokes;

import java.lang.reflect.*;
import java.util.*;
import si.steel.keystrokes.overlay.*;

public class KeyConfiguration {

    public int centerX, centerY;
    public int width = 20, height = 20;

    public boolean visible = true;
    public boolean countCPS = false;

    public double textScale = 1;
    public boolean textShadow = false;
    public boolean rainbowText = false;

    public RenderStyle style = RenderStyle.RECTANGLE;
    public boolean fades = true;
    public boolean bounce = true;

    public boolean particles = true;
    public int particleColor = -1;
    public boolean rainbowParticles = true;

    // All of the colors are in the ARGB format (the base colors are gradients, 2 32 bit ints encoded into a 64 bit long)
    public long baseColorsInactive = 0x8000000080000000L; // The color of the box when the key isn't held
    public long baseColorsActive = 0x80FFFFFF80FFFFFFL; // The color of the box when the key is held

    public int textColorInactive = 0xFFFFFFFF; // The color of the text when the key isn't held
    public int textColorActive = 0xFF000000; // The color of the text when the key is held

    public KeyConfiguration copy(Set<String> excludedFields) {
        KeyConfiguration copy = new KeyConfiguration();

        for (Field f : KeyConfiguration.class.getDeclaredFields()) {
            if (excludedFields.contains(f.getName()))
                continue;

            if (!f.isAccessible()) f.setAccessible(true);

            try {
                f.set(copy, f.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return copy;
    }

    public void setDiff(KeyConfiguration initial, KeyConfiguration diff, Set<String> excludedFields) { // Update only the fields that aren't the same between initial and diff
        for (Field f : KeyConfiguration.class.getDeclaredFields()) {
            if (excludedFields.contains(f.getName()))
                continue;

            if (!f.isAccessible()) f.setAccessible(true);

            try {
                Object initialValue = f.get(initial);
                Object diffValue = f.get(diff);

                if (!initialValue.equals(diffValue)) {
                    f.set(this, diffValue);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
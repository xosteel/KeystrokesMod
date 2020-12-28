package si.steel.keystrokes;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.settings.*;
import net.minecraft.util.*;
import net.minecraftforge.client.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.*;
import si.steel.keystrokes.gui.*;
import si.steel.keystrokes.overlay.*;

@Mod(modid = "keystrokesmod", useMetadata = true)
public class KeystrokesMod {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Mod.Instance
    private static KeystrokesMod instance;

    private static long currentTick;

    private File configFile;
    private boolean firstRun;

    private boolean showGui;

    private List<RenderedKey> keys = new ArrayList<>();

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new KeystrokesCommand());

        initializeDefaultKeys();

        configFile = new File(Minecraft.getMinecraft().mcDataDir, "blkeystrokesmod.json");

        try {
            if (!configFile.exists()) {
                firstRun = true;
                saveConfig();
            } else {
                loadConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent.Post event) {
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo && Minecraft.getMinecraft().currentScreen == null && event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            keys.stream().filter(k -> k.getConfiguration().visible).forEach(RenderedKey::render);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (firstRun) {
            firstRun = false;

            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                Minecraft.getMinecraft().addScheduledTask(() -> {
                    String color = (char) 167 + "b";
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(color + "Looks like this is your first time running Keystrokes Mod."));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(color + "Type /kgui to open the configuration GUI."));
                });
            }).start();
        }

        if (showGui) {
            Minecraft.getMinecraft().displayGuiScreen(new KeystrokesModGUI());
            showGui = false;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            currentTick++;
        }
    }

    public void loadConfig() throws IOException {
        StringBuilder builder = new StringBuilder();

        for (String line : Files.readAllLines(configFile.toPath()))
            builder.append(line).append('\n');

        JsonObject config = new JsonParser().parse(builder.toString()).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            RenderedKey key = keys.stream().filter(k -> k.getKeyBinding().getKeyDescription().equals(entry.getKey())).findFirst().orElse(null);

            if (key != null) {
                KeyConfiguration configuration = GSON.fromJson(entry.getValue(), KeyConfiguration.class);
                key.setConfiguration(configuration);
                updateKeyType(key);
            }
        }
    }

    public void saveConfig() throws IOException {
        JsonObject config = new JsonObject();

        for (RenderedKey key : keys)
            config.add(key.getKeyBinding().getKeyDescription(), GSON.toJsonTree(key.getConfiguration()));

        Files.write(configFile.toPath(), GSON.toJson(config).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public RenderedKey updateKeyType(RenderedKey key) {
        RenderedKey newKey = key;

        if (key.getConfiguration().countCPS && !(key instanceof RenderedCPSKey)) {
            keys.set(keys.indexOf(key), newKey = new RenderedCPSKey(key.getKeyBinding(), key.getConfiguration()));
        } else if (!key.getConfiguration().countCPS && key instanceof RenderedCPSKey) {
            keys.set(keys.indexOf(key), newKey = new RenderedKey(key.getKeyBinding(), key.getConfiguration()));
        }

        if (newKey != key) {
            MinecraftForge.EVENT_BUS.unregister(key);
            MinecraftForge.EVENT_BUS.register(newKey);
        }

        return newKey;
    }

    public void initializeDefaultKeys() {
        keys.forEach(MinecraftForge.EVENT_BUS::unregister);
        keys.clear();

        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;

        // Forward
        {
            KeyConfiguration configuration = new KeyConfiguration();
            configuration.style = RenderStyle.ELLIPSE;
            configuration.centerX = 42;
            configuration.centerY = 20;

            RenderedKey key = new RenderedKey(gameSettings.keyBindForward, configuration);
            MinecraftForge.EVENT_BUS.register(key);
            keys.add(key);
        }
        //

        // Left
        {
            KeyConfiguration configuration = new KeyConfiguration();
            configuration.style = RenderStyle.ELLIPSE;
            configuration.centerX = 20;
            configuration.centerY = 42;

            RenderedKey key = new RenderedKey(gameSettings.keyBindLeft, configuration);
            MinecraftForge.EVENT_BUS.register(key);
            keys.add(key);
        }
        //

        // Back
        {
            KeyConfiguration configuration = new KeyConfiguration();
            configuration.style = RenderStyle.ELLIPSE;
            configuration.centerX = 42;
            configuration.centerY = 42;

            RenderedKey key = new RenderedKey(gameSettings.keyBindBack, configuration);
            MinecraftForge.EVENT_BUS.register(key);
            keys.add(key);
        }
        //

        // Right
        {
            KeyConfiguration configuration = new KeyConfiguration();
            configuration.style = RenderStyle.ELLIPSE;
            configuration.centerX = 64;
            configuration.centerY = 42;

            RenderedKey key = new RenderedKey(gameSettings.keyBindRight, configuration);
            MinecraftForge.EVENT_BUS.register(key);
            keys.add(key);
        }
        //

        // Attack
        {
            KeyConfiguration configuration = new KeyConfiguration();
            configuration.width = 30;
            configuration.centerX = 25;
            configuration.centerY = 64;
            configuration.countCPS = true;
            configuration.textScale = 0.75;

            RenderedKey key = new RenderedCPSKey(gameSettings.keyBindAttack, configuration);
            MinecraftForge.EVENT_BUS.register(key);
            keys.add(key);
        }
        //

        // Use item
        {
            KeyConfiguration configuration = new KeyConfiguration();
            configuration.width = 30;
            configuration.centerX = 59;
            configuration.centerY = 64;
            configuration.countCPS = true;
            configuration.textScale = 0.75;

            RenderedKey key = new RenderedCPSKey(gameSettings.keyBindUseItem, configuration);
            MinecraftForge.EVENT_BUS.register(key);
            keys.add(key);
        }
        //

        // Jump
        {
            KeyConfiguration configuration = new KeyConfiguration();
            configuration.style = RenderStyle.HEXAGON;
            configuration.width = 64;
            configuration.height = 10;
            configuration.centerX = 42;
            configuration.centerY = 81;
            configuration.bounce = true;
            configuration.textScale = 0.75;

            RenderedKey key = new RenderedKey(gameSettings.keyBindJump, configuration);
            MinecraftForge.EVENT_BUS.register(key);
            keys.add(key);
        }
        //
    }

    public void showGui() {
        showGui = true;
    }

    public List<RenderedKey> keys() { // This should return an unmodifiable collection... oh well
        return keys;
    }

    public File getConfigFile() {
        return configFile;
    }

    public static long getCurrentTick() {
        return currentTick;
    }

    public static KeystrokesMod getInstance() {
        return instance;
    }
}

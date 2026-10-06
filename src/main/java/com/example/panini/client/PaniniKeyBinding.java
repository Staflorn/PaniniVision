package com.example.panini.client;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Registers the in-game key binding for opening the Panini config screen.
 */
public class PaniniKeyBinding {

    /**
     * The key used to open the config screen (P key, GLFW key code 80).
     */
    private static final int CONFIG_KEY_CODE = 80;

    private static KeyMapping configKey;

    /**
     * Register the key binding and attach the tick handler.
     *
     * <p>The default key is P (GLFW key code 80). The player can rebind it
     * in the Controls settings screen.
     */
    public static void register() {
        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.panini_vision.open_config",
            CONFIG_KEY_CODE,
            KeyMapping.Category.register(Identifier.parse("panini_vision:keys"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKey != null && configKey.consumeClick()) {
                PaniniConfig config = PaniniProjectionClient.getConfig();
                Minecraft.getInstance().setScreenAndShow(
                    new PaniniConfigScreen(null, config)
                );
            }
        });
    }
}

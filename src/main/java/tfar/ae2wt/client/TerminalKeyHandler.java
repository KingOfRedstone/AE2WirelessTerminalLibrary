package tfar.ae2wt.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.net.TerminalKeyPacket;

/**
 * TerminalKeyHandler class
 *
 * @author Pascal
 * @version 08.03.2025
 */
public class TerminalKeyHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        String type = null;

        if (isPressed(ae2wtlibclient.CRAFTING_TERMINAL_KEY)) {
            type = "crafting";
        }else if (isPressed(ae2wtlibclient.FLUID_TERMINAL_KEY)) {
            type = "fluid";
        }else if (isPressed(ae2wtlibclient.INTERFACE_TERMINAL_KEY)) {
            type = "interface";
        }else if (isPressed(ae2wtlibclient.PATTERN_TERMINAL_KEY)) {
            type = "pattern";
        }else if (isPressed(ae2wtlibclient.UNIVERSAL_TERMINAL_KEY)) {
            type = "universal";
        }

        if (type != null) {
            PacketHandler.INSTANCE.sendToServer(new TerminalKeyPacket(type));
        }
    }

    private boolean isPressed(KeyBinding keyBinding) {
        return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), keyBinding.getKey().getKeyCode());
    }

}

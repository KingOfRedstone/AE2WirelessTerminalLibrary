package tfar.ae2wt.client;

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

        if (ae2wtlibclient.CRAFTING_TERMINAL_KEY.isPressed()) {
            type = "crafting";
        }else if (ae2wtlibclient.FLUID_TERMINAL_KEY.isPressed()) {
            type = "fluid";
        }else if (ae2wtlibclient.INTERFACE_TERMINAL_KEY.isPressed()) {
            type = "interface";
        }else if (ae2wtlibclient.PATTERN_TERMINAL_KEY.isPressed()) {
            type = "pattern";
        }else if (ae2wtlibclient.UNIVERSAL_TERMINAL_KEY.isPressed()) {
            type = "universal";
        }

        if (type != null) {
            PacketHandler.INSTANCE.sendToServer(new TerminalKeyPacket(type));
        }
    }

}

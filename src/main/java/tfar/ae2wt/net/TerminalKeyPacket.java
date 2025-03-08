package tfar.ae2wt.net;

import appeng.container.ContainerLocator;
import appeng.container.me.crafting.CraftingCPUContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import tfar.ae2wt.init.ModItems;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.terminal.WTGuiObject;
import tfar.ae2wt.util.ContainerHelper;
import tfar.ae2wt.wirelesscraftingterminal.TermFactory;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wirelesscraftingterminal.WCTItem;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * TerminalKeyPacket class
 *
 * @author KingOfRedstone
 * @version 04.03.2025
 */
public class TerminalKeyPacket {

    private final String type;

    public TerminalKeyPacket(String type) {
        this.type = type;
    }

    public TerminalKeyPacket(PacketBuffer buf) {
        type = buf.readString();
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();
        if (player == null) return;
        ctx.get().enqueueWork(() -> run(player));
        ctx.get().setPacketHandled(true);
    }

    public void run(PlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        server.execute(() -> {

            ItemStack terminal = ContainerHelper.getTerminal(player, type);
            if (terminal == null) {
                player.sendMessage(new TranslationTextComponent("chat.ae2wtlib.terminal_not_found"), Util.DUMMY_UUID);
                return;
            }

            AbstractWirelessTerminalItem item = (AbstractWirelessTerminalItem) terminal.getItem();

            if (item.canOpen(terminal, player)) {
                item.open(player, ContainerHelper.getContainerLocatorForSlot(ContainerHelper.getTerminalItemSlot(player, type)));
            }

            //item.open(player, ContainerHelper.getContainerLocatorForSlot(ContainerHelper.));

            //WTGuiObject accessInterface;

            /*WCTGuiObject accessInterface = new WCTGuiObject((AbstractWirelessTerminalItem) terminal.getItem(), terminal, player);
            ContainerLocator locator = ContainerHelper.getContainerLocatorForSlot(ContainerHelper.getTerminalItemSlot(player));
            NetworkHooks.openGui((ServerPlayerEntity) player, new TermFactory(accessInterface, locator));*/


            //NetworkHooks.openGui((ServerPlayerEntity) player, new TestFactory(accessInterface, ContainerHelper.getContainerLocatorForSlot(0)));

            //AbstractWirelessTerminalItem abstractTerminal = (AbstractWirelessTerminalItem) terminal.getItem();
            //abstractTerminal.open(player, null);
        });
    }

}

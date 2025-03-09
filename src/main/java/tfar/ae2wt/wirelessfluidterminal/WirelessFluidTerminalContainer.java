package tfar.ae2wt.wirelessfluidterminal;

import appeng.api.config.Actionable;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.ContainerLocator;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.FluidSoundHelper;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.network.NetworkHooks;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.util.ContainerHelper;

import javax.annotation.Nullable;

public class WirelessFluidTerminalContainer extends MEMonitorableContainer<IAEFluidStack> {

    public WirelessFluidTerminalContainer(int id, PlayerInventory ip, ITerminalHost monitorable) {
        this(Menus.WIRELESS_FLUID_TERMINAL, id, ip, monitorable, false);
    }

    public WirelessFluidTerminalContainer(ContainerType<?> containerType, int id, PlayerInventory ip, ITerminalHost host, boolean bindInventory) {
        super(containerType, id, ip, host, bindInventory, Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));

        int slot;

        if (ip.getStackInSlot(ip.currentItem).getItem() instanceof WFTItem) {
            slot = ip.currentItem;
        }else {
            slot = ContainerHelper.getTerminalItemSlot(ip.player, "fluid");
        }

        this.lockPlayerInventorySlot(slot);

        this.createPlayerInventorySlots(ip);
    }

    public static void openServer(PlayerEntity player, ContainerLocator locator) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        WFTGuiObject accessInterface = new WFTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player);

        if (locator.hasItemIndex()) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new TermFactory(accessInterface,locator));
        }
    }

    //The game crashes when ClickType.PICKUP_ALL is called in the WirelessFluidTerminalContainer,
    //could not find the cause of this -> disabled the PICKUP_ALL, it is probably not needed anyway
    @Override
    public ItemStack slotClick(int slot, int button, ClickType clickType, PlayerEntity entity) {
        if (clickType != ClickType.PICKUP_ALL) {
            return super.slotClick(slot, button,clickType, entity);
        }
        return ItemStack.EMPTY;
    }

    public static WirelessFluidTerminalContainer openClient(int windowId, PlayerInventory inv) {
        PlayerEntity player = inv.player;
        /*ItemStack it = inv.player.getHeldItem(Hand.MAIN_HAND);
        ContainerLocator locator = ContainerLocator.forHand(inv.player, Hand.MAIN_HAND);
        WFluidTGuiObject host = new WFluidTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());*/
        ItemStack it = ContainerHelper.getTerminal(player, "fluid");

        if (it == null) {
            return null;
        }

        WFTGuiObject host = new WFTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player);
        return new WirelessFluidTerminalContainer(windowId, inv, host);
    }

    protected void handleNetworkInteraction(ServerPlayerEntity player, @Nullable IAEFluidStack stack, InventoryAction action) {
        if (action == InventoryAction.FILL_ITEM || action == InventoryAction.EMPTY_ITEM) {
            ItemStack held = player.inventory.getItemStack();
            if (held.getCount() == 1) {
                LazyOptional<IFluidHandlerItem> fhOpt = FluidUtil.getFluidHandler(held);
                if (fhOpt.isPresent()) {
                    IFluidHandlerItem fh = fhOpt.orElse(null);
                    IAEFluidStack notStorable;
                    int canFill;
                    IAEFluidStack pulled;
                    if (action == InventoryAction.FILL_ITEM && stack != null) {
                        stack.setStackSize(2147483647L);
                        int amountAllowed = fh.fill(stack.getFluidStack(), IFluidHandler.FluidAction.SIMULATE);
                        stack.setStackSize(amountAllowed);
                        notStorable = Platform.poweredExtraction(this.powerSource, this.monitor, stack, this.getActionSource(), Actionable.SIMULATE);
                        if (notStorable == null || notStorable.getStackSize() < 1L) {
                            return;
                        }

                        canFill = fh.fill(notStorable.getFluidStack(), IFluidHandler.FluidAction.SIMULATE);
                        if (canFill == 0) {
                            return;
                        }

                        stack.setStackSize(canFill);
                        pulled = Platform.poweredExtraction(this.powerSource, this.monitor, stack, this.getActionSource());
                        if (pulled == null || pulled.getStackSize() < 1L) {
                            AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                            return;
                        }

                        int used = fh.fill(pulled.getFluidStack(), IFluidHandler.FluidAction.EXECUTE);
                        if (used != canFill) {
                            AELog.error("Fluid item [%s] reported a different possible amount than it actually accepted.", held.getDisplayName());
                        }

                        player.inventory.setItemStack(fh.getContainer());
                        this.updateHeld(player);
                        FluidSoundHelper.playFillSound(player, pulled.getFluidStack());
                    } else if (action == InventoryAction.EMPTY_ITEM) {
                        FluidStack extract = fh.drain(2147483647, IFluidHandler.FluidAction.SIMULATE);
                        if (extract.isEmpty() || extract.getAmount() < 1) {
                            return;
                        }

                        notStorable = Platform.poweredInsert(this.powerSource, this.monitor, AEFluidStack.fromFluidStack(extract), this.getActionSource(), Actionable.SIMULATE);
                        if (notStorable != null && notStorable.getStackSize() > 0L) {
                            canFill = (int)((long)extract.getAmount() - notStorable.getStackSize());
                            FluidStack storable = fh.drain(canFill, IFluidHandler.FluidAction.SIMULATE);
                            if (storable.isEmpty() || storable.getAmount() == 0) {
                                return;
                            }

                            extract.setAmount(storable.getAmount());
                        }

                        FluidStack drained = fh.drain(extract, IFluidHandler.FluidAction.EXECUTE);
                        extract.setAmount(drained.getAmount());
                        pulled = Platform.poweredInsert(this.powerSource, this.monitor, AEFluidStack.fromFluidStack(extract), this.getActionSource());
                        if (pulled != null && pulled.getStackSize() > 0L) {
                            AELog.error("Fluid item [%s] reported a different possible amount to drain than it actually provided.", held.getDisplayName());
                        }

                        player.inventory.setItemStack(fh.getContainer());
                        this.updateHeld(player);
                        FluidSoundHelper.playEmptySound(player, extract);
                    }

                }
            }
        }
    }

}

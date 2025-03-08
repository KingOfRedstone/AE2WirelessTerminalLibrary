package tfar.ae2wt.util;

import appeng.api.util.AEPartLocation;
import appeng.container.ContainerLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.wirelesscraftingterminal.WCTItem;
import tfar.ae2wt.wirelessfluidterminal.WFTItem;
import tfar.ae2wt.wirelessinterfaceterminal.WITItem;
import tfar.ae2wt.wirelesspatternterminal.WPTItem;
import tfar.ae2wt.wirelessuniversalterminal.WUTItem;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public final class ContainerHelper {

    /**
     * creates a @link ContainerLocator} for any Inventory Slot since it's constructor is private and there is no static method which directly allows this
     * @param slot the slot the container is in
     * @return The new {@link ContainerLocator}
     */
    public static ContainerLocator getContainerLocatorForSlot(int slot) {
        try {
            Object containerLocatorTypePLAYER_INVENTORY = null;
            Class<?> containerLocatorTypeClass = Class.forName("appeng.container.ContainerLocator$Type");
            for (Object obj : containerLocatorTypeClass.getEnumConstants()) {
                if(obj.toString().equals("PLAYER_INVENTORY")) {
                    containerLocatorTypePLAYER_INVENTORY = obj;
                    break;
                }
            }

            Constructor<ContainerLocator> constructor = ContainerLocator.class.getDeclaredConstructor(containerLocatorTypeClass, int.class, ResourceLocation.class, BlockPos.class, AEPartLocation.class);
            constructor.setAccessible(true);
            ContainerLocator containerLocator = constructor.newInstance(containerLocatorTypePLAYER_INVENTORY, slot, null, null, null);
            constructor.setAccessible(false);
            return containerLocator;
        } catch(Exception ignored) {}
        return null;
    }

    private static HashMap<String, Class> typeMap = new HashMap<>();

    static {
        typeMap.put("crafting", WCTItem.class);
        typeMap.put("fluid", WFTItem.class);
        typeMap.put("interface", WITItem.class);
        typeMap.put("pattern", WPTItem.class);
        typeMap.put("universal", WUTItem.class);
    }

    public static ItemStack getTerminal(PlayerEntity player, String type) {
        Class searchClass = typeMap.getOrDefault(type, AbstractWirelessTerminalItem.class);

        //Check vanilla inventories
        PlayerInventory inventory = player.inventory;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (searchClass.isInstance(itemStack.getItem())) {
                return itemStack;
            }
        }

        //Not found -> return null
        return null;
    }

    public static int getTerminalItemSlot(PlayerEntity player, String type) {
        Class searchClass = typeMap.getOrDefault(type, AbstractWirelessTerminalItem.class);

        //Check vanilla inventories
        PlayerInventory inventory = player.inventory;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack itemStack = player.inventory.getStackInSlot(i);
            if (searchClass.isInstance(itemStack.getItem())) {
                return i;
            }
        }
        return -1;
    }
}
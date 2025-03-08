package tfar.ae2wt.wirelessfluidterminal;

import appeng.container.ContainerLocator;
import net.minecraft.entity.player.PlayerEntity;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;

import java.util.function.DoubleSupplier;

public class WFTItem extends AbstractWirelessTerminalItem {
    public WFTItem(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public void open(PlayerEntity player, ContainerLocator locator) {
        WirelessFluidTerminalContainer.openServer(player, locator);
    }
}

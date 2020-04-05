package net.johnbrooks.ports.listeners;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import net.johnbrooks.ports.ports.Port;

public class PortUseEvents implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.isCancelled() && event.getTo() != null) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                Optional<Port> optionalPort = Port.getPort(event.getTo());
                Optional<Port> notifiedPort = Port.getPort(event.getFrom());
                if (notifiedPort.isPresent() &&(!optionalPort.isPresent()||!optionalPort.get().equals(notifiedPort.get()))) {
                	notifiedPort.get().notified.remove(event.getPlayer());
                }
                optionalPort.ifPresent(port -> port.useBridge(event.getPlayer()));
            }
        }
    }
    @EventHandler
    public void onPlayerClickInventory(PlayerItemHeldEvent event) {
    	ItemStack is = event.getPlayer().getInventory().getItem(event.getNewSlot());
    	if (is != null && is.getType().equals(Material.PAPER) && is.getItemMeta().getLore() != null
				&& is.getItemMeta().getLore().get(0).indexOf("A Boat Ticket") != -1) {
    		Optional<Port> port  = Port.getPort(event.getPlayer().getLocation());
    		if (port.isPresent()) {
    			port.get().notified.remove(event.getPlayer());
    			System.out.println("supoisudfpoisudfpoiu");
    			port.get().useBridge(event.getPlayer(),is);
    		}
    	}
    		
    	
    }
}

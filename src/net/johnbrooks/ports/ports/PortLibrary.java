package net.johnbrooks.ports.ports;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Optional;

public class PortLibrary {

    private final HashMap<String, PortShelf> portShelves;

    /**
     * Creates a new PortLibrary where Ports are stored by world and chunk.
     */
    public PortLibrary() {
        this.portShelves = new HashMap<>();
    }

    /**
     * Add a port to the library collection.
     * @param port Port you wish to save into memory.
     * @return Whether any change was made to the library records.
     */
    public boolean insert(final Port port) {
        final String world = port.getWorld().toLowerCase();
        final PortShelf portShelf = getPortShelf(world);
        final boolean inserted = portShelf.insert(port);
        portShelves.put(world, portShelf);
        return inserted;
    }

    /**
     * Returns a port containing the provided location.
     * @param location The point within a Port.
     * @return Port that holds the provided location.
     */
    public Optional<Port> getPort(final Location location) {
        final String world = location.getWorld().getName().toLowerCase();
        final PortShelf portShelf = getPortShelf(world);
        return portShelf.getPort(location);
    }

    /**
     * Returns the port shelf for the given world. Port shelves hold all the ports of a world.
     * @param world The world name you want port shelves of.
     * @return PortShelf containing all ports of the requested world.
     */
    private PortShelf getPortShelf(final String world) {
        return portShelves.containsKey(world) ? portShelves.get(world) : new PortShelf();
    }



}

package net.johnbrooks.ports.ports;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

class PortShelf {

    private final HashMap<Chunk, List<Port>> portCollection;

    protected PortShelf() {
        this.portCollection = new HashMap<>();
    }

    /**
     * Inserts a port into the appropriate collection. If the port extends over multiple chunks,
     * the port will be inserted into each collection.
     * @param port The port that is being inserted into the collections.
     * @return Whether or not the port was inserted into at least 1 collection.
     */
    protected boolean insert(final Port port) {
//            final Chunk portChunkA = port.getPointA().getChunk();
//            final Chunk portChunkB = port.getPointB().getChunk();
//
//            // Insert for chunk A.
//            boolean updated = insert(portChunkA, port);
//
//            if (!portChunkA.equals(portChunkB)) {
//                // Insert for chunk B.
//                insert(portChunkB, port);
//            }
//
//            Location pointC = port.getPointA().clone();
//            pointC.setX(port.getPointB().getZ());
//            Location pointD = port.getPointB().clone();
//            pointD.setZ(port.getPointB().getX());
//
//            if (!pointC.getChunk().equals(portChunkA) || !pointC.getChunk().equals(portChunkB)) {
//                // Insert for chunk C.
//                insert(pointC.getChunk(), port);
//            }
//            if (!pointD.getChunk().equals(portChunkA) || !pointD.getChunk().equals(portChunkB)) {
//                // Insert for chunk D.
//                insert(pointD.getChunk(), port);
//            }

        final AtomicBoolean insertedSuccessfully = new AtomicBoolean();
        calculateChunksInPortCoverage(port).forEach(chunk -> {
            if (insert(chunk, port) && !insertedSuccessfully.get()) {
                insertedSuccessfully.set(true);
            }
        });

        return insertedSuccessfully.get();
    }

    protected void remove(final Port port) {
        calculateChunksInPortCoverage(port).forEach(chunk -> remove(chunk, port));
    }

    protected List<Chunk> calculateChunksInPortCoverage(final Port port) {
        final Chunk chunkPointA = port.getPointA().getChunk();
        final Chunk chunkPointB = port.getPointB().getChunk();

        final int totalChunks = (Math.abs(chunkPointA.getX() - chunkPointB.getZ()) + 1) * (Math.abs(chunkPointA.getZ() - chunkPointB.getZ()) + 1);
        final List<Chunk> chunkList = new ArrayList<>(totalChunks);

        return chunkList;
    }

    /**
     * Retrieve a port that covers the provided location.
     * @param location The target location.
     * @return an Optional of the searched for Port.
     */
    public Optional<Port> getPort(final Location location) {
        final List<Port> portList = getPorts(location.getChunk());
        final Vector locationVector = location.toVector();
        return portList.stream().filter(port -> locationVector
                .isInAABB(port.getPointA().toVector(), port.getPointB().toVector()))
                .findFirst();
    }

    private List<Port> getPorts(final Chunk chunk) {
        return portCollection.containsKey(chunk) ? portCollection.get(chunk) : new ArrayList<>();
    }

    private boolean insert(final Chunk chunk, final Port port) {
        List<Port> portList = getPorts(chunk);
        if (!portList.contains(port)) {
            portList.add(port);
            portCollection.put(chunk, portList);
            return true;
        } else {
            return false;
        }
    }

    private void remove(final Chunk chunk, final Port port) {
        List<Port> portList = getPorts(chunk);
        portList.remove(port);
        portCollection.put(chunk, portList);
    }

}
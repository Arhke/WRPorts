package net.johnbrooks.ports.utils;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import net.johnbrooks.ports.Main;
import org.bukkit.entity.Player;

public class WorldEditUtil {

    public static Region getSelection(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Null player not allowed");
        if (!player.isOnline())
            throw new IllegalArgumentException("Offline player not allowed");

        try {
            BukkitPlayer wPlayer = Main.worldEdit.wrapPlayer(player);
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(wPlayer);
            return session.getSelection(wPlayer.getWorld());
        } catch(IncompleteRegionException e) {
            return null;
        }
    }

}

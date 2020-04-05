package net.johnbrooks.ports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

import de.tr7zw.itemnbtapi.NBTItem;
import net.johnbrooks.ports.ports.Port;
import net.johnbrooks.ports.settings.Settings;
import net.johnbrooks.ports.settings.UpdateManager;
import net.johnbrooks.ports.utils.WorldEditUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Commands implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			if (sender instanceof Player) {
				sender.sendMessage(ChatColor.BLUE + "[Ports] This plugin was created by WiseHollow!");
				Player player = (Player) sender;
				TextComponent message = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "Click here "
						+ ChatColor.RESET + "" + ChatColor.BLUE + "to see my profile and my other plugins! "
						+ Main.plugin.getName() + "!");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
						"https://www.spigotmc.org/members/wisehollow.14804/"));
				player.spigot().sendMessage(message);
			} else
				sender.sendMessage(ChatColor.BLUE + Main.plugin.getName()
						+ " was created by WiseHollow. Check out my other plugins on my SpigotMC profile!");
			return true;
		} else {
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("Version")) {
					sender.sendMessage(ChatColor.BLUE + "[Ports] Current version is '"
							+ Main.plugin.getDescription().getVersion() + "'.");
					if (UpdateManager.isUpdateAvailable())
						sender.sendMessage(ChatColor.BLUE + "[Ports] Update is available.");
					else
						sender.sendMessage(ChatColor.BLUE + "[Ports] Everything is up-to-date.");
					return true;
				} else if (args[0].equalsIgnoreCase("Update")) {
					if (UpdateManager.isUpdateAvailable()) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							TextComponent message = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE
									+ "Click here " + ChatColor.RESET + "" + ChatColor.BLUE
									+ "to get the latest version of " + Main.plugin.getName() + "!");
							message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://goo.gl/aLYP5E"));
							player.spigot().sendMessage(message);
						} else
							sender.sendMessage("Go to https://goo.gl/aLYP5E to get the latest version of "
									+ Main.plugin.getName() + "!");
					} else
						sender.sendMessage(ChatColor.BLUE + "[Ports] Everything is up-to-date.");
					return true;
				} else if (args[0].equalsIgnoreCase("reload")) {
					Main.plugin.reloadConfig();
					Settings.load();
					sender.sendMessage(ChatColor.BLUE + "[Ports] Configuration has been reloaded!");
					return true;
				} else if (args[0].equalsIgnoreCase("list")) {
					sender.sendMessage(ChatColor.BLUE + "List of Ports: ");
					for (Port port : Port.getAllPorts()) {
						sender.sendMessage("Name: " + port.getName() + " | World: " + port.getWorld() + " (PointA: "
								+ port.getPointA().getX() + "," + port.getPointA().getY() + ","
								+ port.getPointA().getZ() + ")");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("help")) {
					sender.sendMessage("/port reload - Reload the config file.");
					sender.sendMessage("/port create [name] - Renames the port.");
					sender.sendMessage("/port delete [name] - Deletes the port.");
					sender.sendMessage("/port arrive [name] - Sets the arrival location.");
					sender.sendMessage("/port update [name] - Sets the activation zone. ");
					sender.sendMessage("/port schedule [name] [schedule] - Sets the schedule.");
					sender.sendMessage(
							"/port describe [name] [port description] - Sets the description that the port will be described as.");
					sender.sendMessage(
							"/port destination [from] [to] - Sets [from]'s destination without editing [to]'s destination.");
					sender.sendMessage(
							"/port hardlink [from] [to] - Links the destination of [from] and [to] and making nothing else link to them.");
					sender.sendMessage("/port link [from] [to] - Links the destination of [from] and [to].");
					sender.sendMessage("/port list - Lists all ports.");
					sender.sendMessage("/port rename [old-name] [new-name] - Renames the port.");
					sender.sendMessage(
							"/port permission [port] [permission.node] - Sets the required permission for Port use. ");
					return true;
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("Create")) {
					if (!(sender instanceof Player)) {
						sender.sendMessage("[Ports] You must be logged in to do this!");
						return true;
					}
					Player player = (Player) sender;
					Region selection = WorldEditUtil.getSelection(player);

					if (selection != null) {
						String portName = args[1];
						Location createLocation = player.getLocation();
						Vector minimumPoint = selection.getMinimumPoint();
						Vector maximumPoint = selection.getMaximumPoint();
						Port port = Port.insertNewPort(portName, player.getLocation().getWorld().getName(),
								minimumPoint, maximumPoint, createLocation);

						if (port != null) {
							Main.logger.info("Port was created by: " + player.getName());
							player.sendMessage(ChatColor.BLUE + "[Ports] The port was successfully created.");
						} else {
							player.sendMessage(ChatColor.BLUE + "[Ports] Could not create port!");
						}
					} else {
						player.sendMessage(ChatColor.BLUE + "[Ports] You've not made a selection yet.");
					}

					return true;
				} else if (args[0].equalsIgnoreCase("Delete") || args[0].equalsIgnoreCase("Remove")) {
					String portName = args[1];
					Port port = Port.getPort(portName);
					if (port != null) {
						port.delete();
						sender.sendMessage(ChatColor.BLUE + "[Ports] The selected port has been deleted!");
					} else {
						sender.sendMessage(ChatColor.BLUE + "[Ports] That port does not exist.");
					}

					return true;
				} else if (args[0].equalsIgnoreCase("Update")) {
					// Set ports regions to worldedit selection.
					if (!(sender instanceof Player)) {
						sender.sendMessage("[Ports] You must be logged in to do this!");
						return true;
					}
					Player player = (Player) sender;
					Region selection = WorldEditUtil.getSelection(player);

					if (selection != null) {
						String portName = args[1];
						Port port = Port.getPort(portName);
						if (port != null) {
							Location locationA = new Location(player.getWorld(),
									selection.getMinimumPoint().getBlockX(), selection.getMinimumPoint().getBlockY(),
									selection.getMinimumPoint().getBlockZ());
							Location locationB = new Location(player.getWorld(),
									selection.getMaximumPoint().getBlockX(), selection.getMaximumPoint().getBlockY(),
									selection.getMaximumPoint().getBlockZ());
							port.setPointA(locationA);
							port.setPointB(locationB);
							try {
								port.save();
							} catch (IOException e) {
								e.printStackTrace();
							}
							player.sendMessage(ChatColor.BLUE + "[Ports] The port was successfully updated.");
						} else {
							player.sendMessage(ChatColor.BLUE + "[Ports] Specified port does not exist!");
						}
					} else {
						player.sendMessage(ChatColor.BLUE + "[Ports] Invalid type of selection!");
					}

					return true;
				} else if (args[0].equalsIgnoreCase("Arrive")) {
					// Set arrival location
					if (!(sender instanceof Player)) {
						sender.sendMessage("[Ports] You must be logged in to do this!");
						return true;
					}
					Player player = (Player) sender;
					String portName = args[1];
					Port port = Port.getPort(portName);
					if (port != null) {
						port.setCreateLocation(player.getLocation());
						try {
							port.save();
						} catch (IOException e) {
							e.printStackTrace();
						}
						player.sendMessage(ChatColor.BLUE + "[Ports] The port's arrival was successfully updated.");
					} else {
						player.sendMessage(ChatColor.BLUE + "[Ports] Specified port does not exist!");
					}

					return true;
				} else if (args[0].equalsIgnoreCase("Permission")) {
					Port port = Port.getPort(args[1]);
					if (port == null) {
						sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
						return true;
					}
					port.setPermission(null);
					try {
						port.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s permission node has been removed.");

					return true;
				}else if(args[0].equalsIgnoreCase("shop")) {
					if (sender instanceof Player) {
						Player player = (Player)sender;
						Port port = Port.getPort(args[1]);
						if (port != null) {
							port.openShop(player);
						}else {
							sender.sendMessage(ChatColor.RED+"Specified Port Doesn't Exist");
						}
					}else {
						sender.sendMessage("Only Players Can Use This Command!");
					}
					return true;
				}
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("addconnection")) {
					Port portA = Port.getPort(args[1]);
					Port portB = Port.getPort(args[2]);

					if (portA != null && portB != null) {
						if (portA.getConnection() != null && !portA.getConnection().contains(portB)) {
							portA.addConnection(portB);
							sender.sendMessage(ChatColor.BLUE + "[Ports] Destinations updated for '" + portA.getName()+ "'.");
						}else {
							sender.sendMessage(ChatColor.RED +"[Ports] Connection update failed ");
							return true;
						}	
						try {
							portA.save(); 
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						sender.sendMessage(ChatColor.BLUE + "[Ports] One of these ports do not exist.");
					}
				} else if (args[0].equalsIgnoreCase("Link")) {
					Port portA = Port.getPort(args[1]);
					Port portB = Port.getPort(args[2]);

					if (portA != null && portB != null) {
						portA.addConnection(portB);
						portB.addConnection(portA);
						sender.sendMessage(ChatColor.BLUE + "[Ports] Destinations updated for '" + portA.getName()
								+ "' and '" + portB.getName() + "'.");
						try {
							portA.save();
							portB.save();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						sender.sendMessage(ChatColor.BLUE + "[Ports] One of these ports do not exist.");
					}
				} else if (args[0].equalsIgnoreCase("ticket")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						Port port1 = null, port2 = null;
						if (args[1].equals(args[2])) {
							player.sendMessage(
									ChatColor.RED + "Starting Port and Destination Port cannot be the same!");
							return true;
						}
						for (Port port : Port.getAllPorts()) {
							if (args[1].equals(port.getName())) {
								port1 = port;
							} else if (args[2].equals(port.getName())) {
								port2 = port;
							}
						}
						if (port1 == null || port2 == null) {
							player.sendMessage(ChatColor.RED + "Starting or Destination Port was not found");
							return true;
						} else {
							player.sendMessage("Giving you a ticket...");
							ItemStack item = new ItemStack(Material.PAPER, 1);
							ItemMeta itemmeta = item.getItemMeta();
							itemmeta.setDisplayName(ChatColor.GREEN + "Boat Ticket");
							List<String> lore = new ArrayList<String>();
							lore.add(ChatColor.GOLD + "A Boat Ticket from Port " + ChatColor.YELLOW + port1.getName()
									+ ChatColor.GOLD + " to Port " + ChatColor.YELLOW + port2.getName());
							itemmeta.setLore(lore);
							item.setItemMeta(itemmeta);
							NBTItem nbti = new NBTItem(item);
							nbti.setString("fromPort", port1.getName());
							nbti.setString("toPort", port2.getName());
							item = nbti.getItem();
							player.getInventory().addItem(item);
						}
						return true;
					} else {
						sender.sendMessage("Sender must be a player");
					}
				} else if (args[0].equalsIgnoreCase("destination")) {
					Port portA = Port.getPort(args[1]);
					Port portB = Port.getPort(args[2]);

					if (portA != null && portB != null) {
						portA.addConnection(portB);
						sender.sendMessage(
								ChatColor.BLUE + "[Ports] Destinations updated for '" + portA.getName() + "'.");
						try {
							portA.save();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						sender.sendMessage(ChatColor.BLUE + "[Ports] One of these ports do not exist.");
					}
				}else if(args[0].equalsIgnoreCase("shop")) {
					Port port = Port.getPort(args[1]);
					Player player = Bukkit.getPlayer(args[2]);
					if(player == null||!player.isOnline()) {
						Bukkit.getLogger().info("Error, Player" + args[2] + "Was Not Found Or Not Online");
					}
					if (port != null) {
						port.openShop(player);
					}else {
						Bukkit.getLogger().warning("Specified Port "+args[1]+" Was Not Found");;
					}
					return true;
				} else if (args[0].equalsIgnoreCase("Permission")) {
					Port port = Port.getPort(args[1]);
					if (port == null) {
						sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
						return true;
					}
					String permission = args[2];
					port.setPermission(permission);
					try {
						port.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage(
							ChatColor.BLUE + "[Ports] " + args[1] + "'s permission node is now '" + permission + "'.");
				} else if (args[0].equalsIgnoreCase("Rename")) {
					Port port = Port.getPort(args[1]);
					if (port == null) {
						sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
						return true;
					}
					File file = new File("plugins" + File.separator + Main.plugin.getName() + File.separator + "ports"
							+ File.separator + port.getFileName());
					file.delete();

					String nameToSet = args[2];
					port.setName(nameToSet);
					try {
						port.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s name is now '" + nameToSet + "'.");
				}

				return true;
			} else {
				if (args[0].equalsIgnoreCase("Describe")) {
					Port port = Port.getPort(args[1]);
					if (port == null) {
						sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
						return true;
					}
					String description = "";
					for (int i = 2; i < args.length; i++)
						description += args[i] + " ";
					description = description.substring(0, description.length() - 1);
					port.setDescription(description);
					try {
						port.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s description node is now '"
							+ description + "'.");
					return true;
				} else if (args[0].equalsIgnoreCase("Schedule")) {
					Port fromPort = Port.getPort(args[1]);
					if (fromPort == null) {
						sender.sendMessage(ChatColor.BLUE + "[Ports] The fromPort does not exist.");
						return true;
					}
					int departure;
					try {
						departure = Integer.parseInt(args[3]);
					} catch (NumberFormatException ex) {
						sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[2] + " is not a number.");
						return true;
					}
					Port toPort = Port.getPort(args[2]);
					if (toPort == null) {
						sender.sendMessage(ChatColor.BLUE + "[Ports] The toPort does not exist.");
						return true;
					}
					fromPort.addDeparture(departure, toPort);
					try {
						fromPort.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s departure time to " + args[2] + " is now " + departure
							+ " minute.");
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
		String[] tabs;

		int argLength = args.length;
		switch (argLength) {
		case 1:
			tabs = new String[] { "version", "reload", "list", "create", "update", "delete", "remove", "arrive",
					"hardlink", "link", "destination", "schedule", "permission", "rename", "describe" };
			break;
		case 2:
			tabs = new String[] { "<PORT_NAME>" };
			break;
		case 3:
			tabs = new String[] { "<PORT_NAME>" };
			break;
		default:
			tabs = new String[] {};
			break;
		}

		return Arrays.stream(tabs).collect(Collectors.toList());
	}
}

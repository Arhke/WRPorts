package net.johnbrooks.ports.ports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.sk89q.worldedit.Vector;

import de.tr7zw.itemnbtapi.NBTItem;
import net.johnbrooks.ports.Main;
import net.johnbrooks.ports.exceptions.InvalidPortException;
import net.johnbrooks.ports.settings.Settings;

public class Port {
	// Key: World Name
	private static HashMap<String, List<Port>> portHashMap = new HashMap<>();

	public static List<Port> getAllPorts() {
		List<Port> list = new ArrayList<>();
		for (List<Port> ports : portHashMap.values())
			list.addAll(ports);
		return list;
	}

	public static void savePorts() {
		for (String world : portHashMap.keySet()) {
			List<Port> ports = portHashMap.get(world);
			for (Port port : ports) {
				try {
					port.save();
				} catch (IOException exception) {
					Main.logger.severe("Error saving: " + port.name);
					exception.printStackTrace();
				}
			}
		}
	}

	public static void loadPorts() {
		portHashMap.clear();
		File directory = new File("plugins" + File.separator + Main.plugin.getName() + File.separator + "ports");
		if (!directory.isDirectory()) {
			final boolean createdDirectories = directory.mkdirs();
			if (!createdDirectories) {
				throw new RuntimeException("Could not create directories for Ports!");
			}

			return;
		}

		for (File file : Objects.requireNonNull(directory.listFiles())) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

			try {
				validateConfiguration(config);
			} catch (InvalidPortException e) {
				e.printStackTrace();
				continue;
			}

			final String name = config.getString("Name");
			final String worldName = config.getString("World");
			World world = Bukkit.getWorld(worldName);
			String description = null;
			if (config.isString("Description"))
				description = config.getString("Description");
			String permission = null;
			if (config.isString("Permission"))
				permission = config.getString("Permission");
			if (world != null) {
				Location creationLocation = new Location(world, config.getInt("Creation Location.X"),
						config.getInt("Creation Location.Y"), config.getInt("Creation Location.Z"));
				creationLocation.setPitch((float) config.getDouble("Creation Location.Pitch"));
				creationLocation.setYaw((float) config.getDouble("Creation Location.Yaw"));
				Location pointA = new Location(world, config.getInt("PointA.X"), config.getInt("PointA.Y"),
						config.getInt("PointA.Z"));
				Location pointB = new Location(world, config.getInt("PointB.X"), config.getInt("PointB.Y"),
						config.getInt("PointB.Z"));

				Port port = new Port(name, world.getName(), pointA, pointB, creationLocation);
				if (description != null)
					port.setDescription(description);
				if (permission != null)
					port.setPermission(permission);
				List<Port> portList = portHashMap.containsKey(world.getName()) ? portHashMap.get(world.getName())
						: new ArrayList<>();
				portList.add(port);
				portHashMap.put(world.getName(), portList);
			} else {
				Main.logger.severe("Error loading world upon loading Port: " + name);
			}
		}

		for (File file : directory.listFiles()) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			String name = config.getString("Name");
			World world = Bukkit.getWorld(config.getString("World"));
			Port port = getPort(name);
			if (port != null && world != null) {
				String depObj = (String) config.get("Departure");
				DepartureList departureList = null;
				if (depObj != null)
					departureList = new DepartureList(depObj);
				else {
					continue;
				}
				port.setDepartureList(departureList);
				port.tryEnableAllDeparture();
			} else {
				Main.logger.severe("Error loading world upon loading Port connection: " + name);
			}
		}

	}

	private static void validateConfiguration(final YamlConfiguration configuration) throws InvalidPortException {
		if (!configuration.isString("Name")) {
			throw new InvalidPortException(
					"Port name was not defined in port configuration: " + configuration.getName());
		} else if (!configuration.isString("World")) {
			throw new InvalidPortException(
					"Port world was not defined in port configuration: " + configuration.getName());
		}
	}

	public static Port insertNewPort(String name, String world, Vector pointA, Vector pointB, Location createLocation) {
		World worldSelection = Bukkit.getWorld(world);
		Location locationA = new Location(worldSelection, pointA.getBlockX(), pointA.getBlockY(), pointA.getBlockZ());
		Location locationB = new Location(worldSelection, pointB.getBlockX(), pointB.getBlockY(), pointB.getBlockZ());
		List<Port> ports;
		if (portHashMap.containsKey(world))
			ports = portHashMap.get(world);
		else
			ports = new ArrayList<>();
		Port port = new Port(name, world, locationA, locationB, createLocation);
		if (!ports.contains(port)) {
			ports.add(port);
			portHashMap.put(port.world, ports);
			try {
				port.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return port;
		}

		return null;
	}

	public static Optional<Port> getPort(Location location) {
		List<Port> ports;
		if (portHashMap.containsKey(location.getWorld().getName())) {
			ports = portHashMap.get(location.getWorld().getName());
			for (Port port : ports) {
				if (location.toVector().isInAABB(port.pointA.toVector(), port.pointB.toVector())) {
					return Optional.of(port);
				}
			}
		}
		return Optional.empty();
	}

	public static Pair<Port, Port> getTicketData(Player p) {
		@SuppressWarnings("deprecation")
		ItemStack is = p.getItemInHand();
		if (is.getType().equals(Material.PAPER) && is.getItemMeta().getLore() != null
				&& is.getItemMeta().getLore().get(0).indexOf("A Boat Ticket") != -1) {
			NBTItem nbti = new NBTItem(is);
			Port port1 = Port.getPort(nbti.getString("fromPort"));
			Port port2 = Port.getPort(nbti.getString("toPort"));
			if (port1 == null || port2 == null) {
				return null;
			} else {
				return new Pair<Port, Port>(port1, port2);
			}
		}
		return null;
	}
	public static Pair<Port, Port> getTicketData(ItemStack is) {
		if (is.getType().equals(Material.PAPER) && is.getItemMeta().getLore() != null
				&& is.getItemMeta().getLore().get(0).indexOf("A Boat Ticket") != -1) {
			NBTItem nbti = new NBTItem(is);
			Port port1 = Port.getPort(nbti.getString("fromPort"));
			Port port2 = Port.getPort(nbti.getString("toPort"));
			if (port1 == null || port2 == null) {
				return null;
			} else {
				return new Pair<Port, Port>(port1, port2);
			}
		}
		return null;
	}

	public static Port getPort(String portName) {
		for (String world : portHashMap.keySet()) {
			List<Port> ports = portHashMap.get(world);
			for (Port port : ports) {
				if (port.name.equalsIgnoreCase(portName))
					return port;
			}
		}

		return null;
	}

	private static void removePort(Port port) {
		List<Port> ports = portHashMap.get(port.world);
		ports.remove(port);
		portHashMap.put(port.world, ports);
	}

	private String name, world, description, permission;
	private Location pointA, pointB, createLocation;
	private double usage;
	private int tier;
	private boolean playerOwned = false;
	public List<Player> notified;
	private DepartureList departureList;

	private Port(String name, String world, Location pointA, Location pointB, Location createLocation) {
		this.name = name;
		this.world = world;
		this.description = null;
		this.permission = null;
		this.pointA = pointA;
		this.pointB = pointB;
		this.createLocation = createLocation;
		this.departureList = new DepartureList();
		this.usage = 0.0;
		this.tier = 0;
		this.notified = new ArrayList<>();

	}

	private void setDepartureList(DepartureList departureList) {
		this.departureList = departureList;
	}

	public DepartureList getDepartureList() {
		return this.departureList;
	}

	public void setPlayerOwned(boolean para) {
		this.playerOwned = para;
	}

	public boolean getPlayerOwned() {
		return this.playerOwned;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWorld() {
		return world;
	}

	public Location getPointA() {
		return pointA;
	}

	public Location getPointB() {
		return pointB;
	}

	public void setPointA(Location pointA) {
		this.pointA = pointA.clone();
	}

	public void setPointB(Location pointB) {
		this.pointB = pointB.clone();
	}

	public Location getCreateLocation() {
		return createLocation;
	}

	public void setCreateLocation(Location createLocation) {
		this.createLocation = createLocation.clone();
	}

	public DepartureList getConnection() {
		return this.departureList;
	}

	public void addConnection(Port connection) {
		this.departureList.add(connection);
		tryEnableDeparture(connection);
	}

	public void remConnection(Port connection) {
		this.departureList.remove(connection);
	}

	public void addDeparture(int departure, String connection) {
		Port port = Port.getPort(connection);
		if (port == null) {
			Bukkit.getLogger().warning("Port " + connection + " was not found!");
			return;
		}
		this.departureList.add(port, departure);
		tryEnableDeparture(port);

	}

	public void addDeparture(int departure, Port port) {
		if (port == null) {
			Bukkit.getLogger().warning(
					"[Port.java] addDeparture(int departure, Port port) Really? a null pointer here? you should know better");
			return;
		}
		this.departureList.add(port, departure);
		tryEnableDeparture(port);

	}

	@SuppressWarnings("deprecation")
	private void transportWithBridge(Player player, Pair<Port, Port> ticket) {
		ItemStack is = player.getItemInHand();
    	if (!(is != null && is.getType().equals(Material.PAPER) && is.getItemMeta().getLore() != null
				&& is.getItemMeta().getLore().get(0).indexOf("A Boat Ticket") != -1)) {
    		player.sendMessage(ChatColor.BLUE+"There is a slight delay between the ticket check and the removal, so we check again right before depart and you switched to a hotbar slot without the ticket");
    		player.sendMessage(ChatColor.BLUE+"Please Make Sure you don't accidently/try to switch to a different hand. If you think this is an error contact admin");
    		return;
    	}
		player.teleport(ticket.getValue().getCreateLocation());
		player.sendMessage(ChatColor.BLUE + "Whoosh!");
		ticket.getValue().createParticles(ticket.getValue().getCreateLocation());
		if (player.isOp() || player.getGameMode() == GameMode.CREATIVE) {
			player.sendMessage(player.getItemInHand().toString());
		} else {
			if(player.getItemInHand().getAmount()<=1)
				player.setItemInHand(new ItemStack(Material.AIR, 0));
			else {
				player.getItemInHand().setAmount(player.getItemInHand().getAmount()-1);
			}
		}
		notified.remove(player);
	}

	public ItemStack createTicket(Departure departure, int cost, int interval) {
		return null;
	}

	public void openShop(Player player) {
		Inventory inv = Bukkit.createInventory(player, 54, "Port " + this.getName());
		for (Departure departure : this.departureList.get()) {
			ItemStack is = this.createTicket(departure, 1, 1);
		}
	}

	public void useBridge(Player player) {
		if (player != null) {
			if (permission != null && !player.hasPermission(permission)) {
				if (!notified.contains(player)) {
					player.sendMessage(
							ChatColor.BLUE + "Hold on there. You do not have the permission to use this port!");
					notified.add(player);
				}
				return;
			}
			Pair<Port, Port> ticket = getTicketData(player);
			
			if (this.departureList == null || this.departureList.isEmpty()) {
				if (!notified.contains(player)) {
					String desc = (description == null ? "port" : description);
					player.sendMessage(ChatColor.BLUE + "This " + desc + " doesn't appear to go anywhere.");
					notified.add(player);
				}
			} else if (ticket == null || ticket.getKey().getName() != this.getName()) {
				
				if (notified.contains(player)) {
					;
				} else if (ticket == null) {
					player.sendMessage(ChatColor.RED
							+ "Sorry, you dont have a valid ticket for this departing boat. Make sure you hold it in your hand so I can see");
					notified.add(player);
				}else if(ticket.getKey().getName() != this.getName()){
					player.sendMessage(ChatColor.RED + "Sorry, you are not holding a ticket for this port. This is port "+this.getName());
					notified.add(player);
				}
			} else if (!this.departureList.contains(ticket.getValue())) {
				if (!notified.contains(player))	{
					player.sendMessage(ChatColor.RED + "Sorry, this port does not take people to port "
							+ ticket.getValue().getName());
					notified.add(player);
				}
			} else {
				Departure departure = this.departureList.getDeparture(ticket.getValue());
				if (departure.getDeparture() != 0) {
					if (!notified.contains(player)) {
						String desc = (description == null ? "port" : description);
						long minutes = TimeUnit.MILLISECONDS
								.toMinutes(departure.getTimeOfDeparture() - System.currentTimeMillis());
						player.sendMessage(ChatColor.BLUE + "Welcome! Please wait for the " + desc
								+ " to depart.");
						player.sendMessage(ChatColor.BLUE + "This port will depart in [" + minutes + " minutes].");
						notified.add(player);
					}
				} else {
					transportWithBridge(player, ticket);
				}
			}
		}
	}

	public void useBridge(Player player, ItemStack is) {
		if (player != null) {
			if (permission != null && !player.hasPermission(permission)) {
				if (!notified.contains(player)) {
					player.sendMessage(
							ChatColor.BLUE + "Hold on there. You do not have the permission to use this port!");
					notified.add(player);
				}
				return;
			}
			Pair<Port, Port> ticket = getTicketData(is);
			
			if (this.departureList == null || this.departureList.isEmpty()) {
				if (!notified.contains(player)) {
					String desc = (description == null ? "port" : description);
					player.sendMessage(ChatColor.BLUE + "This " + desc + " doesn't appear to go anywhere.");
					notified.add(player);
				}
			} else if (ticket == null || ticket.getKey().getName() != this.getName()) {
				
				if (notified.contains(player)) {
					;
				} else if (ticket == null) {
					player.sendMessage(ChatColor.RED
							+ "Sorry, you dont have a valid ticket for this departing boat. Make sure you hold it in your hand so I can see");
					notified.add(player);
				}else if(ticket.getKey().getName() != this.getName()){
					player.sendMessage(ChatColor.RED + "Sorry, you are not holding a ticket for this port. This is port "+this.getName());
					notified.add(player);
				}
			} else if (!this.departureList.contains(ticket.getValue())) {
				if (!notified.contains(player))	{
					player.sendMessage(ChatColor.RED + "Sorry, this port does not take people to port "
							+ ticket.getValue().getName());
					notified.add(player);
				}
			} else {
				Departure departure = this.departureList.getDeparture(ticket.getValue());
				if (departure.getDeparture() != 0) {
					if (!notified.contains(player)) {
						String desc = (description == null ? "port" : description);
						long minutes = TimeUnit.MILLISECONDS
								.toMinutes(departure.getTimeOfDeparture() - System.currentTimeMillis());
						player.sendMessage(ChatColor.BLUE + "Welcome! Please wait for the " + desc
								+ " to depart.");
						player.sendMessage(ChatColor.BLUE + "This port will depart in [" + minutes + " minutes].");
						notified.add(player);
					}
				} else {
					transportWithBridge(player, ticket);
				}
			}
		}
	}
	
	private void launchFireworks() {
		if (Settings.fireworks) {
			Location location = getMidpoint();
			FireworkEffect effect = FireworkEffect.builder().trail(false).flicker(false)
					.withColor(Color.WHITE, Color.LIME, Color.AQUA).with(FireworkEffect.Type.BALL).build();
			Firework fw = location.getWorld().spawn(location, Firework.class);
			FireworkMeta meta = fw.getFireworkMeta();
			meta.clearEffects();
			meta.addEffect(effect);
			meta.setPower(0);
			fw.setFireworkMeta(meta);
		}
	}

	private Location getMidpoint() {
		double midX = (pointA.getX() + pointB.getX()) * 0.5d;
		double midZ = (pointA.getZ() + pointB.getZ()) * 0.5d;
		double bottomY = (pointA.getBlockY() < pointB.getBlockY() ? pointA.getBlockY() : pointB.getBlockY());

		return new Location(pointA.getWorld(), midX, bottomY, midZ);
	}

	public void createParticles(Location target) {
		if (Settings.particles) {
			final Location location = target.clone();
			int time = 0;
			int radius = 2;

			for (double y = 0; y <= 5; y += 0.05) {
				final double _y = y;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
					double x = radius * Math.cos(_y);
					double z = radius * Math.sin(_y);
					Location loc = new Location(location.getWorld(), location.getX() + x + 1f, location.getY() + _y,
							location.getZ() + z + 1f);

					loc.getWorld().playEffect(loc, Effect.EXTINGUISH, 0, 16);
				}, time);
				time += 1;
			}
		}
	}

	public void save() throws IOException {
		File file = new File("plugins" + File.separator + Main.plugin.getName() + File.separator + "ports"
				+ File.separator + getFileName());
		if (file.exists())
			file.delete();
		file.createNewFile();
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		config.set("Name", name);
		config.set("World", world);
		if (description != null)
			config.set("Description", description);
		if (permission != null)
			config.set("Description", permission);
		config.set("Creation Location.X", createLocation.getBlockX());
		config.set("Creation Location.Y", createLocation.getBlockY());
		config.set("Creation Location.Z", createLocation.getBlockZ());
		config.set("Creation Location.Pitch", createLocation.getPitch());
		config.set("Creation Location.Yaw", createLocation.getYaw());
		config.set("PointA.X", pointA.getBlockX());
		config.set("PointA.Y", pointA.getBlockY());
		config.set("PointA.Z", pointA.getBlockZ());
		config.set("PointB.X", pointB.getBlockX());
		config.set("PointB.Y", pointB.getBlockY());
		config.set("PointB.Z", pointB.getBlockZ());
		if (departureList != null && !departureList.isEmpty()) {
			config.set("Departure", this.departureList.toJSONString());
		}
		config.save(file);
	}

	public String getFileName() {
		return world + "_" + pointA.getBlockX() + "_" + pointA.getBlockY() + "_" + pointA.getBlockZ() + "_" + name
				+ ".port";
	}

	public void delete() {
		Main.logger.warning("Deleting port in " + world + "..");
		File file = new File("plugins" + File.separator + Main.plugin.getName() + File.separator + "ports"
				+ File.separator + getFileName());
		file.delete();
		removePort(this);
		for (Departure dep : departureList.get()) {
			if (dep.getDepartureEventId() != -1) {
				Bukkit.getServer().getScheduler().cancelTask(dep.getDepartureEventId());
				dep.setDepartureEventId(-1);
			}
			if (dep.getDepartureNotificationEventId() != -1) {
				Bukkit.getServer().getScheduler().cancelTask(dep.getDepartureNotificationEventId());
				dep.setDepartureNotificationEventId(-1);
			}
			if (dep.getDepartureCountdownEventId() != -1) {
				Bukkit.getServer().getScheduler().cancelTask(dep.getDepartureCountdownEventId());
				dep.setDepartureCountdownEventId(-1);
			}
		}

	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Port) {
			Port otherPort = (Port) object;
			return (otherPort.world.equalsIgnoreCase(world) && otherPort.pointA == pointA && otherPort.pointB == pointB
					&& otherPort.name.equalsIgnoreCase(name));
		} else
			return super.equals(object);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDeparture(int departure, Port port) {
		for (Departure dep : this.departureList.get()) {
			if (dep.getConnection().getName().equals(port.getName())) {
				dep.setDeparture(departure);
				if (dep.getDepartureEventId() != -1) {
					Bukkit.getServer().getScheduler().cancelTask(dep.getDepartureEventId());
					dep.setDepartureEventId(-1);
				}
				if (dep.getDepartureNotificationEventId() != -1) {
					Bukkit.getServer().getScheduler().cancelTask(dep.getDepartureNotificationEventId());
					dep.setDepartureNotificationEventId(-1);
				}
				if (dep.getDepartureCountdownEventId() != -1) {
					Bukkit.getServer().getScheduler().cancelTask(dep.getDepartureCountdownEventId());
					dep.setDepartureCountdownEventId(-1);
				}
				tryEnableDeparture(port);
				return;
			}

		}
		Bukkit.getLogger().warning("[DepartureList.java] Unable to set Departure time for " + port.getName());
	}

	private void tryEnableDeparture(Port port) {
		Departure departure = this.departureList.getDeparture(port);
		if (departure != null && departure.getDeparture() > 0 && departure.getDepartureEventId() == -1) {
			long departureTicks = 1200 * departure.getDeparture();
			departure.updateDepartureTime();
			departure.setDepartureEventId(
					Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {

						for (Player player : Bukkit.getWorld(departure.getConnection().getWorld()).getPlayers()) {
							Optional<Port> optionalPort = Port.getPort(player.getLocation());
							if (optionalPort.isPresent() && optionalPort.get().getName().equals(this.getName())) {
								Pair<Port, Port> ticket = Port.getTicketData(player);
								if (ticket == null || ticket.getKey().getName() != this.getName()) {
									if (!this.departureList.contains(ticket.getValue())) {
										player.sendMessage(
												ChatColor.RED + "Sorry, this port does not take people to port "
														+ ticket.getValue().getName());
									} else {
										player.sendMessage(ChatColor.RED
												+ "Sorry, you dont have a valid ticket for this departing boat. Make sure you hold it in your hand so I can see");
									}
								}else {
									transportWithBridge(player, ticket);
								}
							}
						}
						notified.clear();
						departure.updateDepartureTime();
					}, departureTicks, departureTicks));
			departure.setDepartureNotificationEventId(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(
					Main.plugin, this::launchFireworks, departureTicks - 1200, departureTicks));
			// Countdown task for those in the port.
			departure.setDepartureCountdownEventId(
					Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
						long seconds = TimeUnit.MILLISECONDS
								.toSeconds(departure.getTimeOfDeparture() - System.currentTimeMillis());
						if (seconds < 30 && seconds != 0 && seconds % 5 == 0) {
							String desc = (description == null ? "port" : description);
							for (Player player : Bukkit.getWorld(world).getPlayers()) {
								Optional<Port> optionalPort = getPort(player.getLocation());
								if (optionalPort.isPresent() && optionalPort.get() == this) {
									player.sendMessage(ChatColor.BLUE + "This " + desc + " will depart in " + seconds
											+ " seconds.");
								}
							}
						}
					}, 20, 20));
		}
	}

	private void tryEnableAllDeparture() {
		for (Departure departure : this.departureList.get()) {
			if (departure != null && departure.getDeparture() > 0 && departure.getDepartureEventId() == -1) {
				long departureTicks = 1200 * departure.getDeparture();
				departure.updateDepartureTime();
				departure.setDepartureEventId(
						Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
							for (Player player : Bukkit.getWorld(departure.getConnection().getWorld()).getPlayers()) {
								Optional<Port> optionalPort = Port.getPort(player.getLocation());
								if (optionalPort.isPresent() && optionalPort.get().getName().equals(this.getName())) {
									Pair<Port, Port> ticket = Port.getTicketData(player);
									if (ticket == null || ticket.getKey().getName() != this.getName()) {
										if (!this.departureList.contains(ticket.getValue())) {
											player.sendMessage(
													ChatColor.RED + "Sorry, this port does not take people to port "
															+ ticket.getValue().getName());
										} else {
											player.sendMessage(ChatColor.RED
													+ "Sorry, you dont have a valid ticket for this departing boat. Make sure you hold it in your hand so I can see");
										}
									}else {
										transportWithBridge(player, ticket);
									}
								}
							}
							notified.clear();
							departure.updateDepartureTime();
						}, departureTicks, departureTicks));
				departure.setDepartureNotificationEventId(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(
						Main.plugin, this::launchFireworks, departureTicks - 1200, departureTicks));
				// Countdown task for those in the port.
				departure.setDepartureCountdownEventId(
						Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
							long seconds = TimeUnit.MILLISECONDS
									.toSeconds(departure.getTimeOfDeparture() - System.currentTimeMillis());
							if (seconds < 30 && seconds != 0 && seconds % 5 == 0) {
								String desc = (description == null ? "port" : description);
								for (Player player : Bukkit.getWorld(world).getPlayers()) {
									Optional<Port> optionalPort = getPort(player.getLocation());
									if (optionalPort.isPresent() && optionalPort.get() == this) {
										player.sendMessage(ChatColor.BLUE + "This " + desc + " will depart in "
												+ seconds + " seconds.");
									}
								}
							}
						}, 20, 20));
			}
		}
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	
}

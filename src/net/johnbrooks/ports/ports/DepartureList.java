package net.johnbrooks.ports.ports;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DepartureList {

	private HashSet<Departure> departureList;

	public DepartureList() {
		this.departureList = new HashSet<Departure>();
	}

	@SuppressWarnings("unchecked")
	public DepartureList(String JSONString) {
		this.departureList = new HashSet<Departure>();
		JSONParser parser = new JSONParser();
		JSONObject jsono = null;
		try {
			jsono = (JSONObject) parser.parse(JSONString);
		} catch (ParseException e) {
			Bukkit.getLogger().severe("[DepartureList.java]Error Parsing DepartureList JSONString");
			e.printStackTrace();
			return;
		}
		if (jsono == null || jsono.isEmpty()) {
			Bukkit.getLogger().info("[DepartureList.java] Empty DepartureList");
			return;
		} else if (jsono.keySet() instanceof Set<?>) {
			Set<String> keySet = (Set<String>) jsono.keySet();
			for (String s : keySet) {
				JSONObject json = null;
				try {
					json = (JSONObject) parser.parse((String)jsono.get(s));
				} catch (ParseException e) {
					Bukkit.getLogger().severe("[DepartureList.java] Error parsing Departure jsonString");
					e.printStackTrace();
				}
				if (!(json == null || json.isEmpty())) {
					this.departureList.add(new Departure(json));
				}
			}
		} else {
			Bukkit.getLogger().severe("JSONObject Key Not of String type");
			return;
		}

	}

	
	public int length() {
		return this.departureList.size();
	}

	public HashSet<Departure> get() {
		return this.departureList;
	}

	public boolean add(Port connection) {
		return departureList.add(new Departure(connection));

	}

	public boolean add(Port connection, int departure, int departureEventId, int departureNotificationEventId,
			int departureCountdownEventId) {
		return departureList.add(new Departure(connection, departure, departureEventId, departureNotificationEventId,
				departureCountdownEventId));
	}

	public boolean add(Port connection, int departure) {
		if (this.contains(connection)) {
			Departure depart = this.getDeparture(connection);
			depart.setDeparture(departure);
			return true;
		}
		return departureList.add(new Departure(connection, departure, -1, -1, -1));
	}

	public boolean remove(Port port) {
		if (port == null)
			return false;
		Departure depart = new Departure(port);
		return departureList.remove(depart);
	}

	public boolean contains(Port port) {
		return departureList.contains(new Departure(port));
	}

	public boolean isEmpty() {
		return departureList.isEmpty();
	}

	public Departure getDeparture(Port port) {
		for (Departure departure : this.departureList) {
			if (departure.isPort(port))
				return departure;
		}
		Bukkit.getLogger().warning("[DepartureList.java] getDeparture(Port port) port not found!");
		return null;
	}

	public Departure getDeparture(String port) {
		for (Departure departure : this.departureList) {
			if (departure.isPort(port))
				return departure;
		}
		Bukkit.getLogger().warning("[DepartureList.java] getDeparture(Port port) port not found!");
		return null;
	}

	@SuppressWarnings("unchecked")
	public String toJSONString() {
		JSONObject jsono = new JSONObject();
		for (Departure departure : this.get()) {
			jsono.put(departure.getConnection().getName(), departure.toJSONString());
			
		}
		if (!jsono.isEmpty()) {
			return jsono.toJSONString();
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return toJSONString();
	}
}

package net.johnbrooks.ports.ports;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

public class Departure {
	private Port connection;
	private int departure, departureEventId, departureNotificationEventId, departureCountdownEventId;
	private long timeOfDeparture;
	public Departure(Port connection, int departure, int departureEventId, int departureNotificationEventId, int departureCountdownEventId) {
		this.connection = connection;
		this.departure = departure;
		this.departureEventId = departureEventId;
		this.departureNotificationEventId = departureNotificationEventId;
		this.departureCountdownEventId = departureCountdownEventId;
	}
	public Departure(Port connection) {
		this.connection = connection;
		this.departure = 0;
		this.departureEventId = 1;
		this.departureNotificationEventId = -1;
		this.departureCountdownEventId = -1;
	}
	public Departure(JSONObject json) {
		Port port = Port.getPort((String)json.get("connection"));
		if (port==null) {
			return;
		}
		this.connection = port;
		this.departure = (new Long((long)json.get("departure"))).intValue();
		this.departureEventId = -1;
		this.departureNotificationEventId = -1;
		this.departureCountdownEventId = -1;
	}
	public Port getConnection() {
		return this.connection;
	}
	public int getDeparture() {
		return this.departure;
	}
	public void setDeparture(int departure) {
		this.departure = departure;
        if (departureEventId != -1) {
            Bukkit.getServer().getScheduler().cancelTask(departureEventId);
            departureEventId = -1;
        }
        if (departureNotificationEventId != -1) {
            Bukkit.getServer().getScheduler().cancelTask(departureNotificationEventId);
            departureNotificationEventId = -1;
        }
        if (departureCountdownEventId != -1) {
            Bukkit.getServer().getScheduler().cancelTask(departureCountdownEventId);
            departureCountdownEventId = -1;
        }
	}
	public int getDepartureEventId() {
		return this.departureEventId;
	}
	public void setDepartureEventId(int departureEventId) {
		this.departureEventId = departureEventId;
	}
	public int getDepartureNotificationEventId() {
		return departureNotificationEventId;
	}
	public void setDepartureNotificationEventId(int departureNotificationEventId) {
		this.departureNotificationEventId = departureNotificationEventId;
	}
	public int getDepartureCountdownEventId() {
		return this.departureCountdownEventId;
	}
	public void setDepartureCountdownEventId(int departureCountdownEventId) {
		this.departureCountdownEventId = departureCountdownEventId;
	}
	public long getTimeOfDeparture() {
		return this.timeOfDeparture;
	}
	public void setTimeOfDeparture(long timeOfDeparture) {
		this.timeOfDeparture = timeOfDeparture;
	}
	public void updateDepartureTime() {
		this.timeOfDeparture = System.currentTimeMillis() + (60000 * departure);
	}
	@Override
	public boolean equals(Object dep) {
		if(dep.getClass().equals(this.getClass()) && ((Departure)dep).getConnection().getName().equals(this.getConnection().getName()))
			return true;
		return false;
	}
	@Override
	public int hashCode() {
		return connection.getName().hashCode();
	}
	public boolean isPort(Port port) {
		return this.getConnection().getName().equals(port.getName());
	}
	public boolean isPort(String port) {
		return port.equals(this.getConnection().getName());
	}
	@SuppressWarnings("unchecked")
	public String toJSONString() {
		JSONObject jsono = new JSONObject();
		jsono.put("connection", this.getConnection().getName());
		jsono.put("departure", departure);
		return jsono.toJSONString();
	}
	@Override
	public String toString() {
		return toJSONString();
	}
}

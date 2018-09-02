package me.badbones69.crazycrates.api.enums;

import me.badbones69.crazycrates.api.objects.Crate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BrokeLocation {
	
	private int x, y, z;
	private String world, locationName;
	private Crate crate;
	
	public BrokeLocation(String locationName, Crate crate, int x, int y, int z, String world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
		this.crate = crate;
		this.locationName = locationName;
	}
	
	public String getLocationName() {
		return locationName;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getX() {
		return x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getY() {
		return y;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public int getZ() {
		return z;
	}
	
	public void setWorld(String world) {
		this.world = world;
	}
	
	public String getWorld() {
		return world;
	}
	
	public void setCrate(Crate crate) {
		this.crate = crate;
	}
	
	public Crate getCrate() {
		return crate;
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
	
}
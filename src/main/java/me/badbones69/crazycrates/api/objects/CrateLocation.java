package me.badbones69.crazycrates.api.objects;

import me.badbones69.crazycrates.api.enums.CrateType;
import org.bukkit.Location;

public class CrateLocation {
	
	private String id;
	private Crate crate;
	private Location loc;
	
	public CrateLocation(String id, Crate crate, Location loc) {
		this.id = id;
		this.crate = crate;
		this.loc = loc;
	}
	
	/**
	 * Get the ID of the location.
	 * @return The location's ID.
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Get the crate that this location is set to.
	 * @return The crate that the block is set to.
	 */
	public Crate getCrate() {
		return crate;
	}
	
	/**
	 * Get the crate type of the crate.
	 * @return The type of crate the crate is.
	 */
	public CrateType getCrateType() {
		return crate.getCrateType();
	}
	
	/**
	 * Get the physical location of the crate.
	 * @return The location of the crate.
	 */
	public Location getLocation() {
		return loc;
	}
	
}
package me.badbones69.crazycrates.api;

import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.enums.BrokeLocation;
import me.badbones69.crazycrates.api.enums.CrateType;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.enums.Messages;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.CrateLocation;
import me.badbones69.crazycrates.api.objects.ItemBuilder;
import me.badbones69.crazycrates.api.objects.Prize;
import me.badbones69.crazycrates.controllers.CrateControl;
import me.badbones69.crazycrates.controllers.FileManager;
import me.badbones69.crazycrates.controllers.FileManager.Files;
import me.badbones69.crazycrates.controllers.GUIMenu;
import me.badbones69.crazycrates.cratetypes.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.github.wulf.xmaterial.IMaterial;
import com.github.wulf.xmaterial.XMaterial;

import java.util.*;
import java.util.logging.Level;

public class CrazyCrates {
	
	private static FileManager fileManager = FileManager.getInstance();
	/**
	 * All the crates that have been loaded.
	 */
	private ArrayList<Crate> crates = new ArrayList<>();
	/**
	 * A list of all the physical crate locations.
	 */
	private ArrayList<CrateLocation> crateLocations = new ArrayList<>();
	/**
	 * The instance of this class.
	 */
	private static CrazyCrates instance = new CrazyCrates();
	/**
	 * List of all the broken crates.
	 */
	private ArrayList<String> brokecrates = new ArrayList<>();
	/**
	 * List of broken physical crate locations.
	 */
	private List<BrokeLocation> brokeLocations = new ArrayList<>();
	/**
	 * The crate that the player is opening.
	 */
	private HashMap<UUID, Crate> playerOpeningCrates = new HashMap<>();
	/**
	 * Keys that are being used in crates. Only needed in cosmic due to it taking the key after the player picks a pize and not in a start method.
	 */
	private HashMap<UUID, KeyType> playerKeys = new HashMap<>();
	/**
	 * A list of all current crate tasks that are running that a time. Used to force stop any crates it needs to.
	 */
	private HashMap<UUID, BukkitTask> currentTasks = new HashMap<>();
	/**
	 * A list of tasks being ran by the QuadCrate type.
	 */
	private HashMap<UUID, ArrayList<BukkitTask>> currentQuadTasks = new HashMap<>();
	/**
	 * If the player's inventory is full when given a physical key it will instead give them virtual keys. If false it will drop the keys on the ground.
	 */
	private Boolean giveVirtualKeysWhenInventoryFull;
	/**
	 * True if at least one crate gives new players keys and false if none give new players keys.
	 */
	private Boolean giveNewPlayersKeys;
	
	/**
	 * Gets the instance of the CrazyCrates class.
	 * @return Instance of this class.
	 */
	public static CrazyCrates getInstance() {
		return instance;
	}
	
	/**
	 * Get the file manager that controls all yml files.
	 * @return The FileManager that controls all yml files.
	 */
	public static FileManager getFileManager() {
		return fileManager;
	}
	
	/**
	 * Loads all the information the plugin needs to run.
	 */
	public void loadCrates() {
		giveNewPlayersKeys = false;
		crates.clear();
		brokecrates.clear();
		crateLocations.clear();
		giveVirtualKeysWhenInventoryFull = Files.CONFIG.getFile().getBoolean("Settings.Give-Virtual-Keys-When-Inventory-Full");
		if(fileManager.isLogging()) System.out.println(fileManager.getPrefix() + "Loading all crate information...");
		for(String crateName : fileManager.getAllCratesNames()) {
			//			if(fileManager.isLogging()) System.out.println(fileManager.getPrefix() + "Loading " + crateName + ".yml information....");
			try {
				FileConfiguration file = fileManager.getFile(crateName).getFile();
				ArrayList<Prize> prizes = new ArrayList<>();
				String previewName = file.contains("Crate.Preview-Name") ? file.getString("Crate.Preview-Name") : file.getString("Crate.Name");
				for(String prize : file.getConfigurationSection("Crate.Prizes").getKeys(false)) {
					Prize altPrize = null;
					String path = "Crate.Prizes." + prize;
					if(file.contains(path + ".Alternative-Prize")) {
						if(file.getBoolean(path + ".Alternative-Prize.Toggle")) {
							altPrize = new Prize("Alternative-Prize",
							file.getStringList(path + ".Alternative-Prize.Messages"),
							file.getStringList(path + ".Alternative-Prize.Commands"),
							getItems(file, prize + ".Alternative-Prize"));
						}
					}
					ArrayList<ItemStack> itemPrizes = new ArrayList<>(getItems(file, prize));
					if(file.contains(path + ".Editor-Items")) {
						for(Object list : file.getList(path + ".Editor-Items")) {
							itemPrizes.add((ItemStack) list);
						}
					}
					prizes.add(new Prize(prize, getDisplayItem(file, prize),
					file.getStringList(path + ".Messages"),
					file.getStringList(path + ".Commands"),
					itemPrizes,
					crateName,
					file.getInt(path + ".Chance"),
					file.getInt(path + ".MaxRange"),
					file.getBoolean(path + ".Firework"),
					file.getStringList(path + ".BlackListed-Permissions"),
					file.getStringList(path + ".Tiers"),
					altPrize));
				}
				Integer newPlayersKeys = file.getInt("Crate.StartingKeys");
				if(giveNewPlayersKeys = false) {
					if(newPlayersKeys > 0) {
						giveNewPlayersKeys = true;
					}
				}
				crates.add(new Crate(crateName, previewName, CrateType.getFromName(file.getString("Crate.CrateType")), getKey(file), prizes, file, newPlayersKeys));
				//				if(fileManager.isLogging()) System.out.println(fileManager.getPrefix() + "" + crateName + ".yml has been loaded.");
			}catch(Exception e) {
				brokecrates.add(crateName);
				Bukkit.getLogger().log(Level.WARNING, fileManager.getPrefix() + "There was an error while loading the " + crateName + ".yml file.");
				e.printStackTrace();
			}
		}
		crates.add(new Crate("Menu", "Menu", CrateType.MENU, new ItemStack(Material.AIR), new ArrayList<>(), null, 0));
		if(fileManager.isLogging()) System.out.println(fileManager.getPrefix() + "All crate information has been loaded.");
		if(fileManager.isLogging()) System.out.println(fileManager.getPrefix() + "Loading all the physical crate locations.");
		FileConfiguration locations = Files.LOCATIONS.getFile();
		int loadedAmount = 0;
		int brokeAmount = 0;
		if(locations.getConfigurationSection("Locations") != null) {
			for(String locationName : locations.getConfigurationSection("Locations").getKeys(false)) {
				try {
					String worldName = locations.getString("Locations." + locationName + ".World");
					World world = Bukkit.getWorld(worldName);
					int x = locations.getInt("Locations." + locationName + ".X");
					int y = locations.getInt("Locations." + locationName + ".Y");
					int z = locations.getInt("Locations." + locationName + ".Z");
					Location location = new Location(world, x, y, z);
					Crate crate = getCrateFromName(locations.getString("Locations." + locationName + ".Crate"));
					if(world != null) {
						crateLocations.add(new CrateLocation(locationName, crate, location));
						loadedAmount++;
					}else {
						brokeLocations.add(new BrokeLocation(locationName, crate, x, y, z, worldName));
						brokeAmount++;
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(fileManager.isLogging()) {
			if(loadedAmount > 0 || brokeAmount > 0) {
				if(brokeAmount <= 0) {
					System.out.println(fileManager.getPrefix() + "All physical crate locations have been loaded.");
				}else {
					System.out.println(fileManager.getPrefix() + "Loaded " + loadedAmount + " physical crate locations.");
					System.out.println(fileManager.getPrefix() + "Failed to load " + brokeAmount + " physical crate locations.");
				}
			}
		}
		cleanDataFile();
	}
	
	/**
	 * If the player's inventory is full when given a physical key it will instead give them virtual keys. If false it will drop the keys on the ground.
	 * @return True if the player will get a virtual key and false if it drops on the floor.
	 */
	public Boolean getGiveVirtualKeysWhenInventoryFull() {
		return giveVirtualKeysWhenInventoryFull;
	}
	
	/**
	 * This method is disgned to help clean the data.yml file of any usless info that it may have.
	 */
	public void cleanDataFile() {
		FileConfiguration data = Files.DATA.getFile();
		if(data.contains("Players")) {
			Boolean logging = fileManager.isLogging();
			if(logging) System.out.println(fileManager.getPrefix() + "Cleaning up the data.yml file.");
			List<String> removePlayers = new ArrayList<>();
			for(String uuid : data.getConfigurationSection("Players").getKeys(false)) {
				Boolean hasKeys = false;
				List<String> noKeys = new ArrayList<>();
				for(Crate crate : getCrates()) {
					if(data.getInt("Players." + uuid + "." + crate.getName()) <= 0) {
						noKeys.add(crate.getName());
					}else {
						hasKeys = true;
					}
				}
				if(hasKeys) {
					for(String crate : noKeys) {
						data.set("Players." + uuid + "." + crate, null);
					}
				}else {
					removePlayers.add(uuid);
				}
			}
			if(removePlayers.size() > 0) {
				if(logging) System.out.println(fileManager.getPrefix() + removePlayers.size() + " player's data has been marked to be removed.");
				for(String uuid : removePlayers) {
					//				if(logging) System.out.println(fileManager.getPrefix() + "Removed " + data.getString("Players." + uuid + ".Name") + "'s empty data from the data.yml.");
					data.set("Players." + uuid, null);
				}
				if(logging) System.out.println(fileManager.getPrefix() + "All empty player data has been removed.");
			}
			if(logging) System.out.println(fileManager.getPrefix() + "The data.yml file has been cleaned.");
			Files.DATA.saveFile();
		}
	}
	
	/**
	 * Opens a crate for a player.
	 * @param player The player that is having the crate opened for them.
	 * @param crate The crate that is being used.
	 * @param location The location that may be needed for some crate types.
	 */
	public void openCrate(Player player, Crate crate, KeyType key, Location location) {
		addPlayerToOpeningList(player, crate);
		Boolean broadcast = crate.getFile() != null && crate.getFile().getBoolean("Crate.OpeningBroadCast");
		if(broadcast && crate.getCrateType() != CrateType.QUAD_CRATE) {
			Bukkit.broadcastMessage(Methods.color(crate.getFile().getString("Crate.BroadCast").replaceAll("%Prefix%", Methods.getPrefix()).replaceAll("%prefix%", Methods.getPrefix()).replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName())));
			broadcast = false;
		}
		switch(crate.getCrateType()) {
			case MENU:
				GUIMenu.openGUI(player);
				break;
			case COSMIC:
				Cosmic.openCosmic(player, crate, key);
				break;
			case CSGO:
				CSGO.openCSGO(player, crate, key);
				break;
			case ROULETTE:
				Roulette.openRoulette(player, crate, key);
				break;
			case WHEEL:
				Wheel.startWheel(player, crate, key);
				break;
			case WONDER:
				Wonder.startWonder(player, crate, key);
				break;
			case WAR:
				War.openWarCrate(player, crate, key);
				break;
			case QUAD_CRATE:
				Location last = player.getLocation();
				last.setPitch(0F);
				CrateControl.lastLocation.put(player, last);
				if(broadcast) {
					broadcast = QuadCrate.startBuild(player, location, crate, key, Material.CHEST);
				}else {
					QuadCrate.startBuild(player, location, crate, key, Material.CHEST);
				}
				break;
			case FIRE_CRACKER:
				if(CrateControl.inUse.containsValue(location)) {
					player.sendMessage(Messages.QUICK_CRATE_IN_USE.getMessage());
					removePlayerFromOpeningList(player);
					return;
				}else {
					if(key == KeyType.VIRTUAL_KEY) {
						player.sendMessage(Messages.CANT_BE_A_VIRTUAL_CRATE.getMessage());
						removePlayerFromOpeningList(player);
						return;
					}else {
						CrateControl.inUse.put(player, location);
						FireCracker.startFireCracker(player, crate, key, location);
					}
				}
				break;
			case QUICK_CRATE:
				if(CrateControl.inUse.containsValue(location)) {
					player.sendMessage(Messages.QUICK_CRATE_IN_USE.getMessage());
					removePlayerFromOpeningList(player);
					return;
				}else {
					if(key == KeyType.VIRTUAL_KEY && location.equals(player.getLocation())) {
						player.sendMessage(Messages.CANT_BE_A_VIRTUAL_CRATE.getMessage());
						removePlayerFromOpeningList(player);
						return;
					}else {
						CrateControl.inUse.put(player, location);
						QuickCrate.openCrate(player, location, crate, key, true);
					}
				}
				break;
			case CRATE_ON_THE_GO:
				if(key == KeyType.VIRTUAL_KEY) {
					player.sendMessage(Messages.CANT_BE_A_VIRTUAL_CRATE.getMessage());
					removePlayerFromOpeningList(player);
					return;
				}else {
					takeKeys(1, player, crate, key);
					Prize prize = crate.pickPrize(player);
					givePrize(player, prize);
					if(prize.useFireworks()) {
						Methods.fireWork(player.getLocation().add(0, 1, 0));
					}
					removePlayerFromOpeningList(player);
				}
				break;
		}
		if(broadcast) {
			Bukkit.broadcastMessage(Methods.color(crate.getFile().getString("Crate.BroadCast").replaceAll("%Prefix%", Methods.getPrefix()).replaceAll("%prefix%", Methods.getPrefix()).replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName())));
		}
	}
	
	/**
	 * This forces a crate to end and will not give out a pirze. This is ment for people who leave the server to stop any errors or lag from happening.
	 * @param player The player that the crate is being ended for.
	 */
	public void endCrate(Player player) {
		if(currentTasks.containsKey(player.getUniqueId())) {
			currentTasks.get(player.getUniqueId()).cancel();
			removeCrateTask(player);
		}
	}
	
	/**
	 * Ends the tasks running by a player.
	 * @param player The player using the crate.
	 */
	public void endQuadCrate(Player player) {
		if(currentQuadTasks.containsKey(player.getUniqueId())) {
			for(BukkitTask task : currentQuadTasks.get(player.getUniqueId())) {
				task.cancel();
			}
			currentQuadTasks.remove(player.getUniqueId());
		}
	}
	
	/**
	 * Add a quad crate task that is going on for a player.
	 * @param player The player opening the crate.
	 * @param task The task of the quad crate.
	 */
	public void addQuadCrateTask(Player player, BukkitTask task) {
		if(currentQuadTasks.containsKey(player.getUniqueId())) {
			currentQuadTasks.get(player.getUniqueId()).add(task);
		}else {
			currentQuadTasks.put(player.getUniqueId(), new ArrayList<>());
			currentQuadTasks.get(player.getUniqueId()).add(task);
		}
	}
	
	/**
	 * Checks to see if the player has a quad crate task going on.
	 * @param player The player that is being chacked.
	 * @return True if they do have a task and false if not.
	 */
	public Boolean hasQuadCrateTask(Player player) {
		return currentQuadTasks.containsKey(player.getUniqueId());
	}
	
	/**
	 * Add a crate task that is going on for a player.
	 * @param player The player opening the crate.
	 * @param task The task of the crate.
	 */
	public void addCrateTask(Player player, BukkitTask task) {
		currentTasks.put(player.getUniqueId(), task);
	}
	
	/**
	 * Renove a task from the list of current tasks.
	 * @param player The player using the crate.
	 */
	public void removeCrateTask(Player player) {
		currentTasks.remove(player.getUniqueId());
	}
	
	/**
	 * Checks to see if the player has a crate task going on.
	 * @param player The player that is being chacked.
	 * @return True if they do have a task and false if not.
	 */
	public Boolean hasCrateTask(Player player) {
		return currentTasks.containsKey(player.getUniqueId());
	}
	
	/**
	 * A list of all the physical crate locations.
	 * @return List of locations.
	 */
	public ArrayList<CrateLocation> getCrateLocations() {
		return crateLocations;
	}
	
	/**
	 * Checks to see if the a location is a physical crate.
	 * @param loc The location you are checking.
	 * @return True if it is a physical crate and false if not.
	 */
	public Boolean isCrateLocation(Location loc) {
		for(CrateLocation crateLocation : getCrateLocations()) {
			if(crateLocation.getLocation().equals(loc)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the physical crate of the location.
	 * @param loc The location you are checking.
	 * @return A CrateLocation if the location is a physical crate otherwise null if not.
	 */
	public CrateLocation getCrateLocation(Location loc) {
		for(CrateLocation crateLocation : getCrateLocations()) {
			if(crateLocation.getLocation().equals(loc)) {
				return crateLocation;
			}
		}
		return null;
	}
	
	/**
	 * Get a list of all the broke physical crate locations.
	 * @return List of broken crate locations.
	 */
	public List<BrokeLocation> getBrokeCrateLocations() {
		return brokeLocations;
	}
	
	public void addCrateLocation(Location loc, Crate crate) {
		FileConfiguration locations = Files.LOCATIONS.getFile();
		String id = "1"; //Location ID
		for(int i = 1; locations.contains("Locations." + i); i++) {
			id = (i + 1) + "";
		}
		for(CrateLocation crateLocation : getCrateLocations()) {
			if(crateLocation.getLocation().equals(loc)) {
				id = crateLocation.getID();
				break;
			}
		}
		locations.set("Locations." + id + ".Crate", crate.getName());
		locations.set("Locations." + id + ".World", loc.getWorld().getName());
		locations.set("Locations." + id + ".X", loc.getBlockX());
		locations.set("Locations." + id + ".Y", loc.getBlockY());
		locations.set("Locations." + id + ".Z", loc.getBlockZ());
		Files.LOCATIONS.saveFile();
		crateLocations.add(new CrateLocation(id, crate, loc));
	}
	
	public void removeCrateLocation(String id) {
		Files.LOCATIONS.getFile().set("Locations." + id, null);
		Files.LOCATIONS.saveFile();
		CrateLocation loc = null;
		for(CrateLocation crateLocation : getCrateLocations()) {
			if(crateLocation.getID().equalsIgnoreCase(id)) {
				loc = crateLocation;
				break;
			}
		}
		if(loc != null) {
			crateLocations.remove(loc);
		}
	}
	
	public ArrayList<String> getBrokeCrates() {
		return brokecrates;
	}
	
	public ArrayList<Crate> getCrates() {
		return crates;
	}
	
	public Crate getCrateFromName(String name) {
		for(Crate crate : getCrates()) {
			if(crate.getName().equalsIgnoreCase(name)) {
				return crate;
			}
		}
		return null;
	}
	
	public Inventory loadPreview(Crate crate) {
		FileConfiguration file = crate.getFile();
		int slots = 9;
		for(int size = file.getConfigurationSection("Crate.Prizes").getKeys(false).size(); size > 9 && slots < 54; size -= 9) {
			slots += 9;
		}
		Inventory inv = Bukkit.createInventory(null, slots, Methods.color(file.getString("Crate.Name")));
		for(String reward : file.getConfigurationSection("Crate.Prizes").getKeys(false)) {
			String id = file.getString("Crate.Prizes." + reward + ".DisplayItem");
			String name = file.getString("Crate.Prizes." + reward + ".DisplayName");
			List<String> lore = file.getStringList("Crate.Prizes." + reward + ".Lore");
			HashMap<Enchantment, Integer> enchantments = new HashMap<>();
			Boolean glowing = false;
			int amount = 1;
			if(file.contains("Crate.Prizes." + reward + ".Glowing")) {
				glowing = file.getBoolean("Crate.Prizes." + reward + ".Glowing");
			}
			if(file.contains("Crate.Prizes." + reward + ".DisplayAmount")) {
				amount = file.getInt("Crate.Prizes." + reward + ".DisplayAmount");
			}
			if(file.contains("Crate.Prizes." + reward + ".DisplayEnchantments")) {
				for(String enchant : file.getStringList("Crate.Prizes." + reward + ".DisplayEnchantments")) {
					String[] b = enchant.split(":");
					enchantments.put(Enchantment.getByName(b[0]), Integer.parseInt(b[1]));
				}
			}
			try {
				if(enchantments.size() > 0) {
					inv.setItem(inv.firstEmpty(), Methods.makeItem(id, amount, name, lore, enchantments, glowing));
				}else {
					inv.setItem(inv.firstEmpty(), Methods.makeItem(id, amount, name, lore, glowing));
				}
			}catch(Exception e) {
				inv.addItem(Methods.makeItem(XMaterial.RED_TERRACOTTA, 1, 0, "&c&lERROR", Arrays.asList("&cThere is an error", "&cFor the reward: &c" + reward)));
				//inv.addItem(Methods.makeItem(Material.STAINED_CLAY, 1, 14, "&c&lERROR", Arrays.asList("&cThere is an error", "&cFor the reward: &c" + reward)));
			}
		}
		return inv;
	}
	
	public void givePrize(Player player, Prize prize) {
		if(prize != null) {
			prize = prize.hasBlacklistPermission(player) ? prize.getAltPrize() : prize;
			for(ItemStack i : prize.getItems()) {
				if(!Methods.isInventoryFull(player)) {
					player.getInventory().addItem(i);
				}else {
					player.getWorld().dropItemNaturally(player.getLocation(), i);
				}
			}
			for(String command : prize.getCommands()) {// /give %player% iron %random%:1-64
				if(command.contains("%random%:")) {
					String cmd = command;
					command = "";
					for(String word : cmd.split(" ")) {
						if(word.startsWith("%random%:")) {
							word = word.replace("%random%:", "");
							try {
								int min = Integer.parseInt(word.split("-")[0]);
								int max = Integer.parseInt(word.split("-")[1]);
								command += pickNumber(min, max) + " ";
							}catch(Exception e) {
								command += "1 ";
								Bukkit.getLogger().log(Level.WARNING, "[CrazyCrates]>> The prize " + prize.getName() + " in the " + prize.getCrate() + " crate has errored when trying to run a command.");
								Bukkit.getLogger().log(Level.WARNING, "[CrazyCrates]>> Command: " + cmd);
							}
						}else {
							command += word + " ";
						}
					}
					command = command.substring(0, command.length() - 1);
				}
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Methods.color(command.replace("%Player%", player.getName()).replace("%player%", player.getName())));
			}
			for(String msg : prize.getMessages()) {
				player.sendMessage(Methods.color(msg).replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName())
				.replace("%displayname%", prize.getDisplayItemBuilder().getName()).replace("%DisplayName%", prize.getDisplayItemBuilder().getName()));
			}
		}else {
			Bukkit.getLogger().log(Level.WARNING, "[CrazyCrates]>> No prize was found when giving " + player.getName() + " a prize.");
		}
	}
	
	public Boolean addOfflineKeys(String player, Crate crate, int keys) {
		try {
			FileConfiguration data = Files.DATA.getFile();
			player = player.toLowerCase();
			if(data.contains("Offline-Players." + player + "." + crate.getName())) {
				keys += data.getInt("Offline-Players." + player + "." + crate.getName());
			}
			data.set("Offline-Players." + player + "." + crate.getName(), keys);
			Files.DATA.saveFile();
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean takeOfflineKeys(String player, Crate crate, int keys) {
		try {
			FileConfiguration data = Files.DATA.getFile();
			player = player.toLowerCase();
			int playerKeys = 0;
			if(data.contains("Offline-Players." + player + "." + crate.getName())) {
				playerKeys = data.getInt("Offline-Players." + player + "." + crate.getName());
			}
			data.set("Offline-Players." + player + "." + crate.getName(), playerKeys - keys);
			Files.DATA.saveFile();
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void loadOfflinePlayersKeys(Player player) {
		FileConfiguration data = Files.DATA.getFile();
		String name = player.getName().toLowerCase();
		if(data.contains("Offline-Players." + name)) {
			for(Crate crate : getCrates()) {
				if(data.contains("Offline-Players." + name + "." + crate.getName())) {
					addKeys(data.getInt("Offline-Players." + name + "." + crate.getName()), player, crate, KeyType.VIRTUAL_KEY);
				}
			}
			data.set("Offline-Players." + name, null);
			Files.DATA.saveFile();
		}
	}
	
	public void addPlayerToOpeningList(Player player, Crate crate) {
		playerOpeningCrates.put(player.getUniqueId(), crate);
	}
	
	public void removePlayerFromOpeningList(Player player) {
		playerOpeningCrates.remove(player.getUniqueId());
	}
	
	public Boolean isInOpeningList(Player player) {
		return playerOpeningCrates.containsKey(player.getUniqueId());
	}
	
	public Crate getOpeningCrate(Player player) {
		return playerOpeningCrates.get(player.getUniqueId());
	}
	
	public Boolean isKey(ItemStack item) {
		for(Crate crate : getCrates()) {
			if(crate.getCrateType() != CrateType.MENU) {
				if(Methods.isSimilar(item, crate.getKey())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Crate getCrateFromKey(ItemStack item) {
		for(Crate crate : getCrates()) {
			if(crate.getCrateType() != CrateType.MENU) {
				if(Methods.isSimilar(item, crate.getKey())) {
					return crate;
				}
			}
		}
		return null;
	}
	
	public void addPlayerKeyType(Player player, KeyType key) {
		playerKeys.put(player.getUniqueId(), key);
	}
	
	public void removePlayerKeyType(Player player) {
		playerKeys.remove(player.getUniqueId());
	}
	
	public Boolean hasPlayerKeyType(Player player) {
		return playerKeys.containsKey(player.getUniqueId());
	}
	
	/**
	 * The key type the player's current crate is using.
	 * @param player The player that is using the crate.
	 * @return The key type of the crate the player is using.
	 */
	public KeyType getPlayerKeyType(Player player) {
		return playerKeys.get(player.getUniqueId());
	}
	
	/**
	 * Checks to see if the player has a physical key of the crate.
	 * @param player The player being checked.
	 * @param crate The crate that has the key you are checking.
	 * @return True if they have the key and false if not.
	 */
	public Boolean hasPhysicalKey(Player player, Crate crate) {
		for(ItemStack item : player.getOpenInventory().getBottomInventory().getContents()) {
			if(Methods.isSimilar(item, crate.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public ItemStack getPhysicalKey(Player player, Crate crate) {
		for(ItemStack item : player.getOpenInventory().getBottomInventory().getContents()) {
			if(Methods.isSimilar(item, crate.getKey())) {
				return item;
			}
		}
		return null;
	}
	
	public HashMap<Crate, Integer> getVirtualKeys(Player player) {
		HashMap<Crate, Integer> keys = new HashMap<>();
		for(Crate crate : getCrates()) {
			keys.put(crate, getVirtualKeys(player, crate));
		}
		return keys;
	}
	
	public HashMap<Crate, Integer> getVirtualKeys(String playerName) {
		HashMap<Crate, Integer> keys = new HashMap<>();
		FileConfiguration data = Files.DATA.getFile();
		for(String uuid : data.getConfigurationSection("Players").getKeys(false)) {
			if(playerName.equalsIgnoreCase(data.getString("Players." + uuid + ".Name"))) {
				for(Crate crate : getCrates()) {
					keys.put(crate, data.getInt("Players." + uuid + "." + crate.getName()));
				}
			}
		}
		return keys;
	}
	
	/**
	 * Get the amount of virtual keys a player has.
	 */
	public int getVirtualKeys(Player player, Crate crate) {
		return Files.DATA.getFile().getInt("Players." + player.getUniqueId() + "." + crate.getName());
	}
	
	/**
	 * Get the amount of physical keys a player has.
	 */
	public Integer getPhysicalKeys(Player player, Crate crate) {
		int keys = 0;
		for(ItemStack item : player.getOpenInventory().getBottomInventory().getContents()) {
			if(Methods.isSimilar(item, crate.getKey())) {
				keys += item.getAmount();
			}
		}
		return keys;
	}
	
	/**
	 * Get the total amount of keys a player has.
	 */
	public Integer getTotalKeys(Player player, Crate crate) {
		return getVirtualKeys(player, crate) + getPhysicalKeys(player, crate);
	}
	
	public void takeKeys(int amount, Player player, Crate crate, KeyType key) {
		switch(key) {
			case PHYSICAL_KEY:
				Methods.removeItem(getPhysicalKey(player, crate), player, amount);
				break;
			case VIRTUAL_KEY:
				String uuid = player.getUniqueId().toString();
				int keys = getVirtualKeys(player, crate);
				Files.DATA.getFile().set("Players." + uuid + ".Name", player.getName());
				int newAmount = (keys - amount) > 0 ? (keys - amount) : 0;
				if(newAmount <= 0) {
					Files.DATA.getFile().set("Players." + uuid + "." + crate.getName(), null);
				}else {
					Files.DATA.getFile().set("Players." + uuid + "." + crate.getName(), ((keys - amount) > 0 ? (keys - amount) : 0));
				}
				Files.DATA.saveFile();
				break;
		}
	}
	
	public void addKeys(int amount, Player player, Crate crate, KeyType key) {
		switch(key) {
			case PHYSICAL_KEY:
				if(Methods.isInventoryFull(player)) {
					if(giveVirtualKeysWhenInventoryFull) {
						addKeys(amount, player, crate, KeyType.VIRTUAL_KEY);
					}else {
						player.getWorld().dropItem(player.getLocation(), crate.getKey(amount));
					}
				}else {
					player.getInventory().addItem(crate.getKey(amount));
				}
				break;
			case VIRTUAL_KEY:
				String uuid = player.getUniqueId().toString();
				int keys = getVirtualKeys(player, crate);
				Files.DATA.getFile().set("Players." + uuid + ".Name", player.getName());
				Files.DATA.getFile().set("Players." + uuid + "." + crate.getName(), ((keys + amount) >= 0 ? (keys + amount) : 0));
				Files.DATA.saveFile();
				break;
		}
	}
	
	public void setKeys(int amount, Player player, Crate crate) {
		String uuid = player.getUniqueId().toString();
		Files.DATA.getFile().set("Players." + uuid + ".Name", player.getName());
		Files.DATA.getFile().set("Players." + uuid + "." + crate.getName(), amount);
		Files.DATA.saveFile();
	}
	
	public void checkNewPlayer(Player player) {
		if(giveNewPlayersKeys) {// Checks if any crate gives new players keys and if not then no need to do all this stuff.
			String uuid = player.getUniqueId().toString();
			if(player.hasPlayedBefore()) {
				for(Crate crate : getCrates()) {
					if(crate.doNewPlayersGetKeys()) {
						Files.DATA.getFile().set("Players." + uuid + "." + crate, crate.getNewPlayerKeys());
					}
				}
				Files.DATA.saveFile();
			}
		}
	}
	
	private ItemStack getKey(FileConfiguration file) {
		String name = file.getString("Crate.PhysicalKey.Name");
		List<String> lore = file.getStringList("Crate.PhysicalKey.Lore");
		String id = file.getString("Crate.PhysicalKey.Item");
		Boolean enchanted = false;
		if(file.contains("Crate.PhysicalKey.Glowing")) {
			enchanted = file.getBoolean("Crate.PhysicalKey.Glowing");
		}
		return Methods.makeItem(id, 1, name, lore, enchanted);
	}
	
	private ItemBuilder getDisplayItem(FileConfiguration file, String prize) {
		String path = "Crate.Prizes." + prize + ".";
		ItemBuilder itemBuilder = new ItemBuilder();
		try {
			itemBuilder.setMaterial(file.getString(path + "DisplayItem"))
			.setAmount(file.contains(path + "DisplayAmount") ? file.getInt(path + "DisplayAmount") : 1)
			.setName(file.getString(path + "DisplayName"))
			.setLore(file.getStringList(path + "Lore"))
			.setGlowing(file.getBoolean(path + "Glowing"))
			.setUnbreakable(file.getBoolean(path + "Unbreakable"))
			.setPlayer(file.getString(path + "Player"));
			HashMap<Enchantment, Integer> enchants = new HashMap<>();
			if(file.contains(path + "DisplayEnchantments")) {
				for(String enchant : file.getStringList(path + "DisplayEnchantments")) {
					for(Enchantment enc : Enchantment.values()) {
						if(Methods.getEnchantments().contains(enc.getName())) {
							enchant = enchant.toLowerCase();
							if(enchant.startsWith(enc.getName().toLowerCase() + ":") || enchant.startsWith(Methods.getEnchantmentName(enc).toLowerCase() + ":")) {
								itemBuilder.addEnchantments(enc, Integer.parseInt(enchant.split(":")[1]));
							}
						}
					}
				}
			}
			return itemBuilder;
		}catch(Exception e) {
			return itemBuilder.setMaterial(XMaterial.RED_TERRACOTTA.parseMaterial()).setName("&c&lERROR").addLore("&cThere is an error").addLore("&cFor the reward: &c" + prize);
		}
	}
	
	private ArrayList<ItemStack> getItems(FileConfiguration file, String prize) {
		ArrayList<ItemStack> items = new ArrayList<>();
		for(String l : file.getStringList("Crate.Prizes." + prize + ".Items")) {
			ArrayList<String> lore = new ArrayList<>();
			HashMap<Enchantment, Integer> enchants = new HashMap<>();
			String name = "";
			int amount = 1;
			String id = "Stone";
			String player = "";
			Boolean unbreaking = false;
			for(String i : l.split(", ")) {
				if(i.startsWith("Item:")) {
					id = i.replaceAll("Item:", "");
				}else if(i.startsWith("Name:")) {
					name = Methods.color(i.replaceAll("Name:", ""));
				}else if(i.startsWith("Amount:")) {
					amount = Integer.parseInt(i.replaceAll("Amount:", ""));
				}else if(i.startsWith("Lore:")) {
					for(String L : i.replaceAll("Lore:", "").split(",")) {
						L = Methods.color(L);
						lore.add(L);
					}
				}else if(i.startsWith("Player:")) {
					player = i.replaceAll("Player:", "");
				}else if(i.startsWith("Unbreakable-Item:")) {
					if(i.replaceAll("Unbreakable-Item:", "").equalsIgnoreCase("true")) {
						unbreaking = true;
					}
				}else {
					for(Enchantment enc : Enchantment.values()) {
						if(enc.getName() != null) {
							if(i.toLowerCase().startsWith(enc.getName().toLowerCase() + ":") || i.toLowerCase().startsWith(Methods.getEnchantmentName(enc).toLowerCase() + ":")) {
								String[] breakdown = i.split(":");
								int lvl = Integer.parseInt(breakdown[1]);
								enchants.put(enc, lvl);
							}
						}
					}
				}
			}
			try {
				//if(Methods.makeItem(id, amount, "").getType() == Material.SKULL_ITEM && Methods.makeItem(id, amount, "").getDurability() == 3) {
				if(XMaterial.XfromString(id).parseIMaterial() == IMaterial.SKULL_ITEM) {
					if(unbreaking) {
						items.add(Methods.addUnbreaking(Methods.makePlayerHead(player, amount, name, lore, enchants, false)));
					}else {
						items.add(Methods.makePlayerHead(player, amount, name, lore, enchants, false));
					}
				}else {
					if(unbreaking) {
						items.add(Methods.addUnbreaking(Methods.makeItem(id, amount, name, lore, enchants)));
					}else {
						items.add(Methods.makeItem(id, amount, name, lore, enchants));
					}
				}
			}catch(Exception e) {
				//items.add(Methods.makeItem(Material.STAINED_CLAY, 1, 14, "&c&lERROR", Arrays.asList("&cThere is an error", "&cFor the reward: &c" + prize)));
				items.add(Methods.makeItem(XMaterial.RED_TERRACOTTA, 1, 0, "&c&lERROR", Arrays.asList("&cThere is an error", "&cFor the reward: &c" + prize)));
			}
		}
		return items;
	}
	
	private Integer pickNumber(int min, int max) {
		max++;
		return min + new Random().nextInt(max - min);
	}
	
}
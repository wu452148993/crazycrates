package me.badbones69.crazycrates.cratetypes;

import me.badbones69.crazycrates.Main;
import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.enums.Messages;
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.Prize;
import me.badbones69.crazycrates.controllers.CrateControl;
import me.badbones69.crazycrates.controllers.FileManager.Files;
import me.badbones69.crazycrates.multisupport.reflectionapi.ReflectionUtil;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.inventivetalent.particle.ParticleEffect;

import com.github.wulf.xmaterial.XMaterial;

import java.util.*;

public class QuadCrate implements Listener { // Quad Crate Control.
	
	private static CrazyCrates cc = CrazyCrates.getInstance();
	
	public static HashMap<Player, HashMap<Location, BlockState>> crates = new HashMap<>();
	public static HashMap<Player, HashMap<Location, BlockState>> All = new HashMap<>();
	public static HashMap<Player, HashMap<Location, Boolean>> opened = new HashMap<>();
	public static HashMap<Player, ArrayList<Location>> chests = new HashMap<>();
	public static HashMap<Player, ArrayList<Location>> Rest = new HashMap<>();
	public static HashMap<Player, ArrayList<Entity>> Rewards = new HashMap<>();
	public static HashMap<UUID, BukkitTask> timer = new HashMap<>();
	public static ArrayList<Player> inBreakDown = new ArrayList<>();
	
	/**
	 * Starts building the Crate Setup.
	 * @param player Player that is opening the chest.
	 * @param loc Location it opens at.
	 * @param Chest Chest Type.
	 */
	public static Boolean startBuild(final Player player, final Location loc, Crate crate, KeyType key, Material Chest) {
		Location Lo = loc.clone();
		final ArrayList<Location> Ch = getChests(loc);
		String schem = Methods.pickRandomSchem();
		List<Location> Check = Methods.getLocations(schem, Lo.clone());
		ArrayList<Location> rest = new ArrayList<>();
		rest.add(loc.clone().add(0, -1, 0));
		HashMap<Location, Boolean> checks = new HashMap<>();
		for(Location l : Ch) {
			checks.put(l, false);
		}
		opened.put(player, checks);
		ArrayList<Material> BlockList = new ArrayList<>();
		BlockList.add(XMaterial.SIGN.parseMaterial());
		//BlockList.add(Material.SIGN_POST);
		BlockList.add(Material.WALL_SIGN);
		BlockList.add(Material.STONE_BUTTON);
		BlockList.add(XMaterial.OAK_BUTTON.parseMaterial());
		//BlockList.add(Material.WOOD_BUTTON);
		for(Location l : Check) {
			if(BlockList.contains(l.getBlock().getType())) {
				player.sendMessage(Messages.NEEDS_MORE_ROOM.getMessage());
				cc.removePlayerFromOpeningList(player);
				return false;
			}
			if(l.getBlockY() != Lo.clone().getBlockY() - 1 && l.getBlock().getType() != Material.AIR) {
				if(!l.equals(Lo.clone())) {
					player.sendMessage(Messages.NEEDS_MORE_ROOM.getMessage());
					cc.removePlayerFromOpeningList(player);
					return false;
				}
			}
		}
		if(Lo.clone().subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
			player.sendMessage(Methods.color(Methods.getPrefix() + "&cYou must be standing on a block."));
			cc.removePlayerFromOpeningList(player);
			return false;
		}
		for(Entity en : player.getNearbyEntities(3, 3, 3)) {
			if(en instanceof Player) {
				Player p = (Player) en;
				if(crates.containsKey(p)) {
					HashMap<String, String> placeholders = new HashMap<>();
					placeholders.put("%Player%", p.getName());
					placeholders.put("%player%", p.getName());
					player.sendMessage(Messages.TO_CLOSE_TO_ANOTHER_PLAYER.getMessage(placeholders));
					cc.removePlayerFromOpeningList(player);
					return false;
				}
			}
		}
		player.teleport(loc.clone().add(.5, 0, .5));
		for(Entity en : player.getNearbyEntities(2, 2, 2)) {
			if(en instanceof Player) {
				Player p = (Player) en;
				Vector v = p.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().setY(1);
				p.setVelocity(v);
			}
		}
		chests.put(player, Ch);
		cc.takeKeys(1, player, crate, key);
		rest.clear();
		HashMap<Location, BlockState> locs = new HashMap<>();
		HashMap<Location, BlockState> A = new HashMap<>();
		for(Location l : Methods.getLocations(schem, Lo.clone())) {
			boolean found = false;
			for(Location L : chests.get(player)) {
				if(l.getBlockX() == L.getBlockX() && l.getBlockY() == L.getBlockY() && l.getBlockZ() == L.getBlockZ()) {
					found = true;
					break;
				}
			}
			if(!found) {
				locs.put(l, l.getBlock().getState());
				rest.add(l);
			}
			A.put(l, l.getBlock().getState());
		}
		Methods.pasteSchem(schem, Lo.clone());
		All.put(player, A);
		crates.put(player, locs);
		Rest.put(player, rest);
		Location L = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		player.teleport(L.add(.5, 0, .5));
		spawnChest(Ch, player, Chest);
		return true;
	}
	
	private static void spawnChest(final ArrayList<Location> locs, final Player player, final Material Chest) {
		ArrayList<ParticleEffect> particles = new ArrayList<>();
		particles.add(ParticleEffect.FLAME);
		particles.add(ParticleEffect.VILLAGER_HAPPY);
		particles.add(ParticleEffect.SPELL_WITCH);
		particles.add(ParticleEffect.FIREWORKS_SPARK);
		Random r = new Random();
		final ParticleEffect particle = particles.get(r.nextInt(particles.size()));
		cc.addQuadCrateTask(player, new BukkitRunnable() {
			double r = 0;
			int i = 0;
			int e = 0;
			int f = 0;
			Location l = new Location(locs.get(0).getWorld(), locs.get(0).getBlockX(), locs.get(0).getBlockY(), locs.get(0).getBlockZ()).add(.5, 3, .5);
			Location loc = locs.get(0).clone();
			
			public void run() {
				ArrayList<Location> L = getCircle(l, r, 10);
				ArrayList<Location> L2 = getCircleReverse(l, r, 10);
				particle.display(0, 0, 0, 0, 1, L.get(i), 100);
				particle.display(0, 0, 0, 0, 1, L2.get(i), 100);
				i++;
				f++;
				e++;
				l.add(0, -.05, 0);
				if(i == 10) i = 0;
				if(e == 6) {
					e = 0;
					r = r + .08;
				}
				if(f == 60) {
					//ParticleEffect.BLOCK_DUST.display(new BlockData(Chest, (byte) 0), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
					ParticleEffect.BLOCK_DUST.display(Material.CHEST.createBlockData(), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
					
						player.playSound(player.getLocation(), Sound.valueOf("BLOCK_STONE_STEP"), 1, 1);
					
					loc.getBlock().setType(Chest);
					//MethodUtil.invokeMethod(ReflectionUtil.method_CraftBlock_setData,loc.getBlock(),(byte) 4);
					loc.getBlock().setBlockData(ReflectionUtil.SetChestFace(loc.getBlock().getBlockData(), 4));
					cc.endCrate(player);
				}
			}
		}.runTaskTimer(Main.getPlugin(), 0, 1));
		cc.addQuadCrateTask(player, new BukkitRunnable() {
			public void run() {
				cc.addQuadCrateTask(player, new BukkitRunnable() {
					double r = 0;
					int i = 0;
					int e = 0;
					int f = 0;
					Location l = new Location(locs.get(1).getWorld(), locs.get(1).getBlockX(), locs.get(1).getBlockY(), locs.get(1).getBlockZ()).add(.5, 3, .5);
					Location loc = locs.get(1).clone();
					
					@SuppressWarnings("deprecation")
					public void run() {
						ArrayList<Location> L = getCircle(l, r, 10);
						ArrayList<Location> L2 = getCircleReverse(l, r, 10);
						particle.display(0, 0, 0, 0, 1, L.get(i), 100);
						particle.display(0, 0, 0, 0, 1, L2.get(i), 100);
						i++;
						f++;
						e++;
						l.add(0, -.05, 0);
						if(i == 10) i = 0;
						if(e == 6) {
							e = 0;
							r = r + .08;
						}
						if(f == 60) {
							//ParticleEffect.BLOCK_DUST.display(new BlockData(Chest, (byte) 0), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
							ParticleEffect.BLOCK_DUST.display(Material.CHEST.createBlockData(), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
							
							player.playSound(player.getLocation(), Sound.valueOf("BLOCK_STONE_STEP"), 1, 1);
		
							loc.getBlock().setType(Chest);
							//MethodUtil.invokeMethod(ReflectionUtil.method_CraftBlock_setData,loc.getBlock(),(byte) 2);
							loc.getBlock().setBlockData(ReflectionUtil.SetChestFace(loc.getBlock().getBlockData(), 2));
							//loc.getBlock().setData((byte) 2);
							cc.endCrate(player);
						}
					}
				}.runTaskTimer(Main.getPlugin(), 0, 1));
			}
		}.runTaskLater(Main.getPlugin(), 61));
		cc.addQuadCrateTask(player, new BukkitRunnable() {
			public void run() {
				cc.addQuadCrateTask(player, new BukkitRunnable() {
					double r = 0;
					int i = 0;
					int e = 0;
					int f = 0;
					Location l = new Location(locs.get(2).getWorld(), locs.get(2).getBlockX(), locs.get(2).getBlockY(), locs.get(2).getBlockZ()).add(.5, 3, .5);
					Location loc = locs.get(2).clone();
					
					@SuppressWarnings("deprecation")
					public void run() {
						ArrayList<Location> L = getCircle(l, r, 10);
						ArrayList<Location> L2 = getCircleReverse(l, r, 10);
						particle.display(0, 0, 0, 0, 1, L.get(i), 100);
						particle.display(0, 0, 0, 0, 1, L2.get(i), 100);
						i++;
						f++;
						e++;
						l.add(0, -.05, 0);
						if(i == 10) i = 0;
						if(e == 6) {
							e = 0;
							r = r + .08;
						}
						if(f == 60) {
							//ParticleEffect.BLOCK_DUST.display(new BlockData(Chest, (byte) 0), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
							ParticleEffect.BLOCK_DUST.display(Material.CHEST.createBlockData(), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
							player.playSound(player.getLocation(), Sound.valueOf("BLOCK_STONE_STEP"), 1, 1);
							loc.getBlock().setType(Chest);
							//MethodUtil.invokeMethod(ReflectionUtil.method_CraftBlock_setData,loc.getBlock(),(byte) 5);
							loc.getBlock().setBlockData(ReflectionUtil.SetChestFace(loc.getBlock().getBlockData(), 5));
							//loc.getBlock().setData((byte) 5);
							cc.endCrate(player);
						}
					}
				}.runTaskTimer(Main.getPlugin(), 0, 1));
			}
		}.runTaskLater(Main.getPlugin(), 121));
		cc.addQuadCrateTask(player, new BukkitRunnable() {
			public void run() {
				cc.addQuadCrateTask(player, new BukkitRunnable() {
					double r = 0;
					int i = 0;
					int e = 0;
					int f = 0;
					Location l = new Location(locs.get(3).getWorld(), locs.get(3).getBlockX(), locs.get(3).getBlockY(), locs.get(3).getBlockZ()).add(.5, 3, .5);
					Location loc = locs.get(3).clone();
					
					@SuppressWarnings("deprecation")
					public void run() {
						ArrayList<Location> L = getCircle(l, r, 10);
						ArrayList<Location> L2 = getCircleReverse(l, r, 10);
						particle.display(0, 0, 0, 0, 1, L.get(i), 100);
						particle.display(0, 0, 0, 0, 1, L2.get(i), 100);
						i++;
						f++;
						e++;
						l.add(0, -.05, 0);
						if(i == 10) i = 0;
						if(e == 6) {
							e = 0;
							r = r + .08;
						}
						if(f == 60) {
							//ParticleEffect.BLOCK_DUST.display(new BlockData(Chest, (byte) 0), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
							ParticleEffect.BLOCK_DUST.display(Material.CHEST.createBlockData(), .5F, .5F, .5F, 0, 10, loc.clone().add(.5, .3, .5), 100);
							player.playSound(player.getLocation(), Sound.valueOf("BLOCK_STONE_STEP"), 1, 1);
							loc.getBlock().setType(Chest);
							//MethodUtil.invokeMethod(ReflectionUtil.method_CraftBlock_setData,loc.getBlock(),(byte) 3);
							loc.getBlock().setBlockData(ReflectionUtil.SetChestFace(loc.getBlock().getBlockData(), 3));
							//loc.getBlock().setData((byte) 3);
							cc.endCrate(player);
						}
					}
				}.runTaskTimer(Main.getPlugin(), 0, 1));
			}
		}.runTaskLater(Main.getPlugin(), 181));
		cc.addQuadCrateTask(player, new BukkitRunnable() {
			public void run() {
				timer.put(player.getUniqueId(), new BukkitRunnable() {
					public void run() {
						undoBuild(player);
						player.sendMessage(Messages.OUT_OF_TIME.getMessage());
					}
				}.runTaskLater(Main.getPlugin(), Files.CONFIG.getFile().getInt("Settings.QuadCrate.Timer") * 20));
			}
		}.runTaskLater(Main.getPlugin(), 241));
	}
	
	/**
	 * Undoes the Crate Build.
	 * @param player The player that opened the Crate.
	 */
	@SuppressWarnings("deprecation")
	public static void undoBuild(Player player) {
		HashMap<Location, BlockState> locs = All.get(player);
		for(Location loc : locs.keySet()) {
			if(locs.get(loc) != null) {
				loc.getBlock().setType(locs.get(loc).getType());
				//MethodUtil.invokeMethod(ReflectionUtil.method_CraftBlock_setData, loc.getBlock(), locs.get(loc).getRawData());
				//loc2.getBlock().setTypeIdAndData(locs.get(loc2).getTypeId(), locs.get(loc2).getRawData(), true);
			}
		}
		crates.remove(player);
		chests.remove(player);
		cc.removePlayerFromOpeningList(player);
		cc.endCrate(player);
		cc.endQuadCrate(player);
		if(timer.containsKey(player.getUniqueId())) {
			timer.get(player.getUniqueId()).cancel();
			timer.remove(player.getUniqueId());
		}
		if(Rewards.containsKey(player)) {
			for(Entity h : Rewards.get(player)) {
				h.remove();
			}
			Rewards.remove(player);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Location loc = e.getBlock().getLocation();
		for(Player player : crates.keySet()) {
			for(Location l : crates.get(player).keySet()) {
				if(l.getBlockX() == loc.getBlockX() && l.getBlockY() == loc.getBlockY() && l.getBlockZ() == loc.getBlockZ()) {
					e.setCancelled(true);
					return;
				}
			}
		}
		if(crates.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChestClick(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(e.getClickedBlock().getType() == Material.CHEST) {
				for(final Player player : chests.keySet()) {
					for(final Location l : chests.get(player)) {
						Location B = e.getClickedBlock().getLocation();
						if(l.getBlockX() == B.getBlockX() && l.getBlockY() == B.getBlockY() && l.getBlockZ() == B.getBlockZ()) {
							e.setCancelled(true);
							if(player == e.getPlayer()) {
								Methods.playChestAction(e.getClickedBlock(), true);
								if(!opened.get(player).get(l)) {
									ArrayList<Entity> rewards = new ArrayList<>();
									Prize prize = cc.getOpeningCrate(player).pickPrize(player, B.clone().add(.5, 1.3, .5));
									cc.givePrize(player, prize);
									Bukkit.getPluginManager().callEvent(new PlayerPrizeEvent(player, cc.getOpeningCrate(player), cc.getOpeningCrate(player).getName(), prize));
									final Item reward = player.getWorld().dropItem(B.clone().add(.5, 1, .5), Methods.addLore(prize.getDisplayItem().clone(), new Random().nextInt(Integer.MAX_VALUE) + ""));
									reward.setVelocity(new Vector(0, .2, 0));
									reward.setCustomName(prize.getDisplayItem().getItemMeta().getDisplayName());
									reward.setCustomNameVisible(true);
									reward.setPickupDelay(Integer.MAX_VALUE);
									if(Rewards.containsKey(player)) {
										rewards.addAll(Rewards.get(player));
									}
									rewards.add(reward);
									Rewards.put(player, rewards);
									if(prize.useFireworks()) {
										Methods.fireWork(B.clone().add(.5, 1, .5));
									}
								}
								boolean trigger = true;
								for(Location loc : opened.get(player).keySet()) {
									if(loc.getBlockX() == l.getBlockX() && loc.getBlockY() == l.getBlockY() && loc.getBlockZ() == l.getBlockZ()) {
										opened.get(player).put(loc, true);
									}
									if(opened.get(player).get(loc)) {
									}else {
										trigger = false;
									}
								}
								if(inBreakDown.contains(player)) {
									trigger = false;
								}
								if(trigger) {
									inBreakDown.add(player);
									Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
										if(Rest.get(player) != null) {
											for(Location loc : Rest.get(player)) { // Rest is all except Chests.
												HashMap<Location, BlockState> locs = crates.get(player); // Crates holds the Data.
												if(locs != null) {
													for(Location loc2 : locs.keySet()) { // Looping though the locations.
														if(locs.get(loc) != null) { // Checking to make sure a location isn't null.
															if(loc.equals(loc2)) {
																loc2.getBlock().setType(locs.get(loc2).getType());
																//MethodUtil.invokeMethod(ReflectionUtil.method_CraftBlock_setData, loc2.getBlock(), locs.get(loc2).getRawData());
																//loc2.getBlock().setTypeIdAndData(locs.get(loc2).getTypeId(), locs.get(loc2).getRawData(), true);
															}
														}
													}
												}
											}
										}
										
										player.playSound(player.getLocation(), Sound.valueOf("BLOCK_STONE_STEP"), 1, 1);
				
									}, 3 * 20);
									Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
										if(Rest.get(player) != null) {
											for(Location loc : Rest.get(player)) {
												HashMap<Location, BlockState> locs = crates.get(player);
												if(chests.get(player) != null) {
													for(Location loc2 : chests.get(player)) {
														if(locs.get(loc) != null) {
															loc2.getBlock().setType(Material.AIR);
														}
													}
												}
											}
										}
										if(Rewards.get(player) != null) {
											for(Entity h : Rewards.get(player)) {
												h.remove();
											}
										}
										Rewards.remove(player);
									
										player.playSound(player.getLocation(), Sound.valueOf("BLOCK_STONE_STEP"), 1, 1);
		
										crates.remove(player);
										chests.remove(player);
										Rest.remove(player);
										cc.removePlayerFromOpeningList(player);
										opened.remove(player);
										if(timer.containsKey(player.getUniqueId())) {
											timer.get(player.getUniqueId()).cancel();
											timer.remove(player.getUniqueId());
										}
										if(CrateControl.lastLocation.containsKey(player)) {
											player.teleport(CrateControl.lastLocation.get(player));
											CrateControl.lastLocation.remove(player);
										}
										inBreakDown.remove(player);
									}, 6 * 20);
								}
							}
							return;
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		Location from = e.getFrom();
		Location to = e.getTo();
		if(crates.containsKey(player)) {
			if(from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
				e.setCancelled(true);
				player.teleport(from);
				return;
			}
		}
		for(Entity en : player.getNearbyEntities(2, 2, 2)) {
			if(en instanceof Player) {
				Player p = (Player) en;
				if(crates.containsKey(p)) {
					Vector v = player.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().setY(1);
					if(player.isInsideVehicle()) {
						player.getVehicle().setVelocity(v);
						return;
					}
					player.setVelocity(v);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(crates.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
		for(Player p : Rest.keySet()) {
			for(Location l : Rest.get(p)) {
				Location P = e.getBlockPlaced().getLocation();
				if(P.getBlockX() == l.getBlockX() && P.getBlockY() == l.getBlockY() && P.getBlockZ() == l.getBlockZ()) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onCMD(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		if(crates.containsKey(player)) {
			if(!e.getMessage().toLowerCase().contains("plugman")) {
				e.setCancelled(true);
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%Player%", player.getName());
				placeholders.put("%player%", player.getName());
				player.sendMessage(Messages.NO_COMMANDS_WHILE_CRATE_OPENED.getMessage(placeholders));
			}
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		Player player = e.getPlayer();
		if(crates.containsKey(player)) {
			if(e.getCause() == TeleportCause.ENDER_PEARL) {
				e.setCancelled(true);
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%Player%", player.getName());
				placeholders.put("%player%", player.getName());
				player.sendMessage(Messages.NO_TELEPORTING.getMessage(placeholders));
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if(crates.containsKey(player)) {
			undoBuild(player);
		}
	}
	
	@EventHandler
	public void onHopperPickUp(InventoryPickupItemEvent e) {
		for(Player player : Rewards.keySet()) {
			if(Rewards.get(player).contains(e.getItem())) {
				e.setCancelled(true);
			}
		}
	}
	
	private static ArrayList<Location> getChests(Location loc) {
		ArrayList<Location> U = new ArrayList<>();
		U.add(loc.clone().add(2, 0, 0));
		U.add(loc.clone().add(0, 0, 2));
		U.add(loc.clone().add(-2, 0, 0));
		U.add(loc.clone().add(0, 0, -2));
		return U;
	}
	
	private static ArrayList<Location> getCircle(Location center, double radius, int amount) {
		World world = center.getWorld();
		double increment = (2 * Math.PI) / amount;
		ArrayList<Location> locations = new ArrayList<>();
		for(int i = 0; i < amount; i++) {
			double angle = i * increment;
			double x = center.getX() + (radius * Math.cos(angle));
			double z = center.getZ() + (radius * Math.sin(angle));
			locations.add(new Location(world, x, center.getY(), z));
		}
		return locations;
	}
	
	public static ArrayList<Location> getCircleReverse(Location center, double radius, int amount) {
		World world = center.getWorld();
		double increment = (2 * Math.PI) / amount;
		ArrayList<Location> locations = new ArrayList<>();
		for(int i = 0; i < amount; i++) {
			double angle = i * increment;
			double x = center.getX() - (radius * Math.cos(angle));
			double z = center.getZ() - (radius * Math.sin(angle));
			locations.add(new Location(world, x, center.getY(), z));
		}
		return locations;
	}
	
}
package me.badbones69.crazycrates.cratetypes;

import me.badbones69.crazycrates.Main;
import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.Prize;
import me.badbones69.crazycrates.controllers.CrateControl;
import me.badbones69.crazycrates.multisupport.reflectionapi.ReflectionUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class QuickCrate implements Listener {
	
	private static CrazyCrates cc = CrazyCrates.getInstance();
	public static ArrayList<Entity> allRewards = new ArrayList<>();
	public static HashMap<Player, Entity> rewards = new HashMap<>();
	private static HashMap<Player, BukkitTask> tasks = new HashMap<>();
	
	public static void openCrate(final Player player, final Location loc, Crate crate, KeyType keyType, boolean remove) {
		if(player.isSneaking()) {
			if(keyType == KeyType.PHYSICAL_KEY) {
				int keys = Methods.getItemInHand(player).getAmount();
				int keysUsed = 0;
				for(; keys > 0; keys--) {
					if(!Methods.isInventoryFull(player)) {
						Prize prize = crate.pickPrize(player);
						cc.givePrize(player, prize);
						if(prize.useFireworks()) {
							Methods.fireWork(loc.clone().add(.5, 1, .5));
						}
						keysUsed++;
					}else {
						break;
					}
				}
				cc.takeKeys(keysUsed, player, crate, keyType);
				endQuickCrate(player, loc);
			}else if(keyType == KeyType.VIRTUAL_KEY) {
			
			}
		}else {
			if(remove) {
				cc.takeKeys(1, player, crate, keyType);
			}
			Prize prize = crate.pickPrize(player, loc.clone().add(.5, 1.3, .5));
			cc.givePrize(player, prize);
			Bukkit.getPluginManager().callEvent(new PlayerPrizeEvent(player, crate, crate.getName(), prize));
			ItemStack displayItem = prize.getDisplayItem();
			//NBTItem nbtItem = new NBTItem(displayItem);
			ReflectionUtil.setBoolean(displayItem,"crazycrates-item", true);
			//nbtItem.setBoolean("crazycrates-item", true);
			//displayItem = nbtItem.getItem();
			Item reward = player.getWorld().dropItem(loc.clone().add(.5, 1, .5), displayItem);
			reward.setVelocity(new Vector(0, .2, 0));
			reward.setCustomName(displayItem.getItemMeta().getDisplayName());
			reward.setCustomNameVisible(true);
			reward.setPickupDelay(Integer.MAX_VALUE);
			rewards.put(player, reward);
			allRewards.add(reward);
			Methods.playChestAction(loc.getBlock(), true);
			if(prize.useFireworks()) {
				Methods.fireWork(loc.clone().add(.5, 1, .5));
			}
			tasks.put(player, new BukkitRunnable() {
				@Override
				public void run() {
					endQuickCrate(player, loc);
				}
			}.runTaskLater(Main.getPlugin(), 5 * 20));
		}
	}
	
	public static void endQuickCrate(Player player, Location loc) {
		if(tasks.containsKey(player)) {
			tasks.get(player).cancel();
			tasks.remove(player);
		}
		if(rewards.get(player) != null) {
			allRewards.remove(rewards.get(player));
			rewards.get(player).remove();
			rewards.remove(player);
		}
		Methods.playChestAction(loc.getBlock(), false);
		CrateControl.inUse.remove(player);
		cc.removePlayerFromOpeningList(player);
	}
	
	public static void removeAllRewards() {
		for(Entity entity : allRewards) {
			if(entity != null) {
				entity.remove();
			}
		}
	}
	
	@EventHandler
	public void onHopperPickUp(InventoryPickupItemEvent e) {
		if(isReward(e.getItem())) {
			e.setCancelled(true);
		}
	}
	
	public static Boolean isReward(Entity entity) {
		if(entity instanceof Item) {
			//NBTItem item = new NBTItem(((Item) entity).getItemStack());
			return ReflectionUtil.hasKey(((Item) entity).getItemStack(), "crazycrates-item");
			//return item.hasKey("crazycrates-item");
		}
		return false;
	}
	
}
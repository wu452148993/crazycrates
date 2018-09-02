package me.badbones69.crazycrates.cratetypes;

import me.badbones69.crazycrates.Main;
import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.Prize;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.wulf.xmaterial.XMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Wheel implements Listener {
	
	private static CrazyCrates cc = CrazyCrates.getInstance();
	public static HashMap<Player, HashMap<Integer, ItemStack>> Rewards = new HashMap<>();
	
	public static void startWheel(final Player player, Crate crate, KeyType key) {
		final Inventory inv = Bukkit.createInventory(null, 54, Methods.color(crate.getFile().getString("Crate.CrateName")));
		for(int i = 0; i < 54; i++) {
			inv.setItem(i, Methods.makeItem("160:15", 1, " "));
		}
		HashMap<Integer, ItemStack> items = new HashMap<>();
		for(int i : getBorder()) {
			Prize prize = crate.pickPrize(player);
			inv.setItem(i, prize.getDisplayItem());
			items.put(i, prize.getDisplayItem());
		}
		Rewards.put(player, items);
		cc.takeKeys(1, player, crate, key);
		player.openInventory(inv);
		cc.addCrateTask(player, new BukkitRunnable() {
			ArrayList<Integer> slots = getBorder();
			int i = 0;
			int f = 17;
			int full = 0;
			int timer = Methods.randomNumber(42, 68);
			int slower = 0;
			int open = 0;
			int slow = 0;
			
			@Override
			public void run() {
				if(i >= 18) {
					i = 0;
				}
				if(f >= 18) {
					f = 0;
				}
				if(full < timer) {
					if(Rewards.get(player).get(slots.get(i)).getItemMeta().hasLore()) {
						inv.setItem(slots.get(i), Methods.makeItem("160:5", 1, Rewards.get(player).get(slots.get(i)).getItemMeta().getDisplayName(), Rewards.get(player).get(slots.get(i)).getItemMeta().getLore()));
					}else {
						inv.setItem(slots.get(i), Methods.makeItem("160:5", 1, Rewards.get(player).get(slots.get(i)).getItemMeta().getDisplayName()));
					}
					inv.setItem(slots.get(f), Rewards.get(player).get(slots.get(f)));
					
					player.playSound(player.getLocation(), Sound.valueOf("UI_BUTTON_CLICK"), 1, 1);
			
					i++;
					f++;
				}
				if(full >= timer) {
					if(slowSpin().contains(slower)) {
						if(Rewards.get(player).get(slots.get(i)).getItemMeta().hasLore()) {
							inv.setItem(slots.get(i), Methods.makeItem("160:5", 1, Rewards.get(player).get(slots.get(i)).getItemMeta().getDisplayName(), Rewards.get(player).get(slots.get(i)).getItemMeta().getLore()));
						}else {
							inv.setItem(slots.get(i), Methods.makeItem("160:5", 1, Rewards.get(player).get(slots.get(i)).getItemMeta().getDisplayName()));
						}
						inv.setItem(slots.get(f), Rewards.get(player).get(slots.get(f)));
						
						player.playSound(player.getLocation(), Sound.valueOf("UI_BUTTON_CLICK"), 1, 1);
				
						i++;
						f++;
					}
					if(full == timer + 47) {
						
						player.playSound(player.getLocation(), Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1, 1);
				
					}
					if(full >= timer + 47) {
						slow++;
						if(slow >= 2) {
							Random r = new Random();
							int color = r.nextInt(15);
							if(color == 8)
								color = 0;
							for(int slot = 0; slot < 54; slot++) {
								if(!getBorder().contains(slot)) {		
									inv.setItem(slot, Methods.makeItem(XMaterial.XfromString("STAINED_GLASS_PANE:"+color), 1, 0, " "));
									//inv.setItem(slot, Methods.makeItem(Material.STAINED_GLASS_PANE, 1, color, " "));
								}
							}
							slow = 0;
						}
					}
					if(full >= (timer + 55 + 47)) {
						Prize prize = null;
						if(cc.isInOpeningList(player)) {
							for(Prize p : crate.getPrizes()) {
								if(Rewards.get(player).get(slots.get(f)).isSimilar(p.getDisplayItem())) {
									prize = p;
								}
							}
						}
						if(prize != null) {
							cc.givePrize(player, prize);
							if(prize.useFireworks()) {
								Methods.fireWork(player.getLocation().add(0, 1, 0));
							}
							Bukkit.getPluginManager().callEvent(new PlayerPrizeEvent(player, crate, crate.getName(), prize));
							player.closeInventory();
						}
						cc.removePlayerFromOpeningList(player);
						cc.endCrate(player);
					}
					slower++;
				}
				full++;
				open++;
				if(open > 5) {
					player.openInventory(inv);
					open = 0;
				}
			}
		}.runTaskTimer(Main.getPlugin(), 1, 1));
	}
	
	private static ArrayList<Integer> slowSpin() {
		ArrayList<Integer> slow = new ArrayList<>();
		int full = 46;
		int cut = 9;
		for(int i = 46; cut > 0; full--) {
			if(full <= i - cut || full >= i - cut) {
				slow.add(i);
				i = i - cut;
				cut--;
			}
		}
		return slow;
	}
	
	private static ArrayList<Integer> getBorder() {
		ArrayList<Integer> slots = new ArrayList<>();
		slots.add(13);
		slots.add(14);
		slots.add(15);
		slots.add(16);
		slots.add(25);
		slots.add(34);
		slots.add(43);
		slots.add(42);
		slots.add(41);
		slots.add(40);
		slots.add(39);
		slots.add(38);
		slots.add(37);
		slots.add(28);
		slots.add(19);
		slots.add(10);
		slots.add(11);
		slots.add(12);
		return slots;
	}
}
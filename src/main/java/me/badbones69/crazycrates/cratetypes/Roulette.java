package me.badbones69.crazycrates.cratetypes;

import me.badbones69.crazycrates.Main;
import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.Prize;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.wulf.xmaterial.XMaterial;

import java.util.ArrayList;
import java.util.Random;

public class Roulette implements Listener {
	
	private static CrazyCrates cc = CrazyCrates.getInstance();
	
	private static void setGlass(Inventory inv) {
		Random r = new Random();
		for(int i = 0; i < 27; i++) {
			if(i != 13) {
				int color = r.nextInt(15);
				if(color == 8) color = 1;
				//inv.setItem(i, Methods.makeItem(Material.STAINED_GLASS_PANE, 1, color, " "));		
				inv.setItem(i, Methods.makeItem(XMaterial.XfromString("STAINED_GLASS_PANE:" + color), 1, 0, " "));
			}
		}
	}
	
	public static void openRoulette(Player player, Crate crate, KeyType key) {
		Inventory inv = Bukkit.createInventory(null, 27, Methods.color(crate.getFile().getString("Crate.CrateName")));
		setGlass(inv);
		inv.setItem(13, crate.pickPrize(player).getDisplayItem());
		player.openInventory(inv);
		cc.takeKeys(1, player, crate, key);
		startRoulette(player, inv, crate);
	}
	
	private static void startRoulette(final Player player, final Inventory inv, final Crate crate) {
		cc.addCrateTask(player, new BukkitRunnable() {
			int time = 1;
			int even = 0;
			int full = 0;
			int open = 0;
			
			@Override
			public void run() {
				if(full <= 15) {
					inv.setItem(13, crate.pickPrize(player).getDisplayItem());
					setGlass(inv);
					
					player.playSound(player.getLocation(), Sound.valueOf("UI_BUTTON_CLICK"), 1, 1);
		
					even++;
					if(even >= 4) {
						even = 0;
						inv.setItem(13, crate.pickPrize(player).getDisplayItem());
					}
				}
				open++;
				if(open >= 5) {
					player.openInventory(inv);
					open = 0;
				}
				full++;
				if(full > 16) {
					if(slowSpin().contains(time)) {
						setGlass(inv);
						inv.setItem(13, crate.pickPrize(player).getDisplayItem());
					
						player.playSound(player.getLocation(), Sound.valueOf("UI_BUTTON_CLICK"), 1, 1);

					}
					time++;
					if(time >= 23) {
						
						player.playSound(player.getLocation(), Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1, 1);
			
						cc.endCrate(player);
						Prize prize = null;
						for(Prize p : crate.getPrizes()) {
							if(inv.getItem(13).isSimilar(p.getDisplayItem())) {
								prize = p;
							}
						}
						cc.givePrize(player, prize);
						if(prize.useFireworks()) {
							Methods.fireWork(player.getLocation().add(0, 1, 0));
						}
						Bukkit.getPluginManager().callEvent(new PlayerPrizeEvent(player, crate, crate.getName(), prize));
						cc.removePlayerFromOpeningList(player);
					}
				}
			}
		}.runTaskTimer(Main.getPlugin(), 2, 2));
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
	
}
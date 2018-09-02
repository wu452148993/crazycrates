package me.badbones69.crazycrates.cratetypes;

import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.enums.CrateType;
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.Prize;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CrateOnTheGo implements Listener {
	
	private static CrazyCrates cc = CrazyCrates.getInstance();
	
	@EventHandler
	public void onCrateOpen(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = Methods.getItemInHand(player);
			if(item != null) {
				for(Crate crate : cc.getCrates()) {
					if(crate.getCrateType() == CrateType.CRATE_ON_THE_GO) {
						if(Methods.isSimilar(crate.getKey(), item)) {
							e.setCancelled(true);
							cc.addPlayerToOpeningList(player, crate);
							Methods.removeItem(item, player);
							Prize prize = crate.pickPrize(player);
							cc.givePrize(player, prize);
							Bukkit.getPluginManager().callEvent(new PlayerPrizeEvent(player, crate, cc.getOpeningCrate(player).getName(), prize));
							if(prize.useFireworks()) {
								Methods.fireWork(player.getLocation().add(0, 1, 0));
							}
							cc.removePlayerFromOpeningList(player);
						}
					}
				}
			}
		}
	}
	
}
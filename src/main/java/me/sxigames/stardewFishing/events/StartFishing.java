package me.sxigames.stardewFishing.events;

import me.sxigames.stardewFishing.StardewFishing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Arrays;

public class StartFishing implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        switch (event.getState()) {
            case CAUGHT_FISH -> {
                Item caughtEntity = (Item) event.getCaught();
                assert caughtEntity != null;
                ItemStack caughtItem = caughtEntity.getItemStack();
                Material[] fishMaterials = new Material[]{
                        Material.COD,
                        Material.SALMON,
                        Material.TROPICAL_FISH,
                        Material.PUFFERFISH,
                };
                if (Arrays.stream(fishMaterials).anyMatch(material -> material == caughtItem.getType())) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("You started fishing! You're getting a: ").append(caughtItem.displayName()));
                    Component fishBar = Component.text(builder -> {
                        builder.append(Component.text("["));
                        for (int i = 0; i < 100; i++) {
                            if (i < 20) {
                                builder.append(Component.text("▏", net.kyori.adventure.text.format.NamedTextColor.GREEN));
                            } else {
                                builder.append(Component.text("▏", net.kyori.adventure.text.format.NamedTextColor.RED));
                            }
                        }
                        builder.append(Component.text("]"));
                    });
                    Title title = Title.title(
                            fishBar,
                            Component.text("Fishing: ").append(caughtItem.displayName()),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(30), Duration.ZERO)
                    );
                    player.showTitle(title);
                    player.addScoreboardTag("fishing");
                    player.getServer().getScheduler().runTaskLater(StardewFishing.getPlugin(), () -> {
                        player.removeScoreboardTag("fishing");
                        player.sendMessage(Component.text("You stopped fishing!"));
                        event.getHook().remove();
                    }, 600L); // 600 ticks = 30 seconds

                } else {
                    player.sendMessage(Component.text("You caught something, but it's not a fish! It is: ").append(caughtItem.displayName()));
                }
            }

            case REEL_IN, BITE -> {
                if (player.getScoreboardTags().contains("fishing")) {
                    event.setCancelled(true);
                }
            }
            default -> {

            }
        }

    }
}

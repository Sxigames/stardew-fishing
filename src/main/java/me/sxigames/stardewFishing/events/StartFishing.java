package me.sxigames.stardewFishing.events;

import me.sxigames.stardewFishing.StardewFishing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StartFishing implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        Plugin plugin = StardewFishing.getPlugin();
        NamespacedKey barLocationKey = new NamespacedKey(plugin, "barLocation");
//        NamespacedKey barSpeedKey = new NamespacedKey(plugin, "barSpeed");
        player.sendMessage(event.getState().name());
        switch (event.getState()) {
            case CAUGHT_FISH -> {
                if (player.getScoreboardTags().contains("fishing")) {
                    event.setCancelled(true);
                    return;
                }
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
                    player.addScoreboardTag("fishing");
                    AtomicInteger fishPos = new AtomicInteger((int) (Math.random() * 100));
                    AtomicBoolean fishDirection = new AtomicBoolean(true); // true = right, false = left
                    player.sendMessage(String.valueOf(fishPos.get()));
                    player.getPersistentDataContainer().set(barLocationKey, PersistentDataType.INTEGER, 0);
                    int taskID = player.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                            if(fishPos.get() >= 100){
                                fishDirection.set(false); // Change fishDirection to left
                            }
                            if (fishPos.get() <= 0) {
                                fishDirection.set(true); // Change fishDirection to right
                            }
                            if (fishDirection.get()) {
                                fishPos.addAndGet(1); // Move right
                            } else {
                                fishPos.addAndGet(-1); // Move left
                            }
                            Component fishBar = Component.text(builder -> {
                                builder.append(Component.text("["));
                                Integer barLocation = player.getPersistentDataContainer().get(barLocationKey, PersistentDataType.INTEGER);
                                if (barLocation == null) {
                                    barLocation = 0; // Default value
                                }
                                for (int i = 0; i < 100; i++) {
                                    if (i == fishPos.get()) {
                                        builder.append(Component.text("▏", net.kyori.adventure.text.format.NamedTextColor.RED));
                                    }
                                    else if (i < barLocation + 10 && i > barLocation) {
                                        builder.append(Component.text("▏", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
                                    } else {
                                        builder.append(Component.text("▏", net.kyori.adventure.text.format.NamedTextColor.BLUE));
                                    }
                                }
                                builder.append(Component.text("]"));
                                if(barLocation > 0) {
                                    player.getPersistentDataContainer().set(barLocationKey, PersistentDataType.INTEGER, barLocation - 1);
                                }
                            });
                            Title title = Title.title(
                                    fishBar,
                                    Component.text("Fishing: ").append(caughtItem.displayName()),
                                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
                            );
                            player.showTitle(title);
                    }, 0L, 2L); // 2 ticks = 10 times per second
                    player.getServer().getScheduler().runTaskLater(plugin, () -> {
                        player.removeScoreboardTag("fishing");
                        player.sendMessage(Component.text("You stopped fishing!"));
                        event.getHook().remove();
                        player.getServer().getScheduler().cancelTask(taskID);
                    }, 600L); // 600 ticks = 30 seconds

                } else {
                    player.sendMessage(Component.text("You caught something, but it's not a fish! It is: ").append(caughtItem.displayName()));
                }
            }

            case REEL_IN -> {
                if (player.getScoreboardTags().contains("fishing")) {
                    event.setCancelled(true);
                    Integer barLocation = player.getPersistentDataContainer().get(barLocationKey, PersistentDataType.INTEGER);
//                    Integer barSpeed = player.getPersistentDataContainer().get(barSpeedKey, PersistentDataType.INTEGER);
                    if (barLocation == null) {
                        barLocation = 0; // Default value
                    }
                    if (barLocation <= 90){
                        player.getPersistentDataContainer().set(barLocationKey, PersistentDataType.INTEGER, barLocation + 2);
                    }
                }
            }
            default -> {
                if (player.getScoreboardTags().contains("fishing")) {
                    event.setCancelled(true);
                }
            }
        }

    }
}

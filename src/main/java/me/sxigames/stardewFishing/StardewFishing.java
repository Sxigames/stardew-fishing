package me.sxigames.stardewFishing;

import me.sxigames.stardewFishing.events.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class StardewFishing extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new StartFishing(), this);
        getServer().getPluginManager().registerEvents(new ChangeSlot(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static StardewFishing getPlugin() {
        return StardewFishing.getPlugin(StardewFishing.class);
    }
}

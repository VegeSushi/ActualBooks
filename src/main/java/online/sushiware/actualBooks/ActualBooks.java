package online.sushiware.actualBooks;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

public final class ActualBooks extends JavaPlugin {
    public static final Map<String, ItemStack> bookCache = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("ActualBooks has been enabled!");
        getServer().getPluginManager().registerEvents(new VillagerTradeListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ActualBooks has been disabled. Clearing cache...");
        bookCache.clear();
    }
}
package online.sushiware.actualBooks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import java.util.*;

public class VillagerTradeListener implements Listener {
    private final ActualBooks plugin;
    private final WikipediaFetcher fetcher;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public VillagerTradeListener(ActualBooks plugin) {
        this.plugin = plugin;
        this.fetcher = new WikipediaFetcher(plugin.getLogger());
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (villager.getProfession() != Villager.Profession.LIBRARIAN) return;

        // Level-based trade limit check
        long customTradeCount = villager.getRecipes().stream()
                .filter(r -> r.getResult().getType() == Material.WRITTEN_BOOK)
                .count();

        if (customTradeCount >= villager.getVillagerLevel()) return;

        UUID pId = event.getPlayer().getUniqueId();
        if (cooldowns.getOrDefault(pId, 0L) > System.currentTimeMillis()) return;
        cooldowns.put(pId, System.currentTimeMillis() + 3000);

        fetcher.fetchRandomSummary().thenAccept(page -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                ItemStack book = ActualBooks.bookCache.computeIfAbsent(page.title(),
                        k -> BookFactory.createBook(page.title(), page.content())).clone();

                MerchantRecipe recipe = new MerchantRecipe(book, 9999999);
                recipe.addIngredient(new ItemStack(Material.EMERALD, 3));

                List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
                recipes.add(recipe);
                villager.setRecipes(recipes);
                plugin.getLogger().info("Added new trade: " + page.title());
            });
        });
    }
}
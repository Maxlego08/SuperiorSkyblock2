package com.bgsoftware.superiorskyblock.core.zmenu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.BannedPlayersButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.CoopsButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.CountsButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.RatingsButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.TargetShowButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.bank.BankLogsButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.confirm.ButtonConfirmBan;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.confirm.ButtonConfirmDisband;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.confirm.ButtonConfirmKick;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.confirm.ButtonConfirmLeave;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.members.IslandMemberBanButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.members.IslandMemberInfoButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.members.IslandMemberKickButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.members.IslandMembersButton;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.BankActionLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.BankLogsSortLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.BlockValueLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.BorderColorLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.BorderToggleLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.GlobalWarpsLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandBiomeLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandCreationLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandMemberRoleLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandPermissionLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandSettingsLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandTopLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandTopSortLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.PlayerLanguageLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.RateLoader;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.button.loader.NoneLoader;
import fr.maxlego08.menu.exceptions.InventoryException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ZMenuManager implements Listener {

    private final SuperiorSkyblockPlugin plugin;
    private final InventoryManager inventoryManager;
    private final ButtonManager buttonManager;
    private final Map<Player, PlayerCache> caches = new HashMap<>();

    public ZMenuManager(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.inventoryManager = getProvider(InventoryManager.class);
        this.buttonManager = getProvider(ButtonManager.class);
    }

    private <T> T getProvider(Class<T> classz) {
        RegisteredServiceProvider<T> provider = Bukkit.getServer().getServicesManager().getRegistration(classz);
        return provider == null ? null : provider.getProvider() != null ? provider.getProvider() : null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.caches.remove(event.getPlayer());
    }

    public PlayerCache getCache(Player player) {
        return this.caches.computeIfAbsent(player, PlayerCache::new);
    }

    public void registerButtons() {

        this.buttonManager.register(new IslandCreationLoader(this.plugin));
        this.buttonManager.register(new IslandSettingsLoader(this.plugin));
        this.buttonManager.register(new IslandBiomeLoader(this.plugin));
        this.buttonManager.register(new IslandMemberRoleLoader(this.plugin));
        this.buttonManager.register(new IslandPermissionLoader(this.plugin));
        this.buttonManager.register(new IslandTopLoader(this.plugin));
        this.buttonManager.register(new IslandTopSortLoader(this.plugin));
        this.buttonManager.register(new BorderColorLoader(this.plugin));
        this.buttonManager.register(new BorderToggleLoader(this.plugin));
        this.buttonManager.register(new PlayerLanguageLoader(this.plugin));
        this.buttonManager.register(new BlockValueLoader(this.plugin));
        this.buttonManager.register(new BankLogsSortLoader(this.plugin));
        this.buttonManager.register(new GlobalWarpsLoader(this.plugin));
        this.buttonManager.register(new BankActionLoader(this.plugin));
        this.buttonManager.register(new RateLoader(this.plugin));

        this.buttonManager.register(new NoneLoader(this.plugin, IslandMembersButton.class, "SUPERIORSKYBLOCK_MEMBERS"));
        this.buttonManager.register(new NoneLoader(this.plugin, IslandMemberInfoButton.class, "SUPERIORSKYBLOCK_MEMBER_INFO"));
        this.buttonManager.register(new NoneLoader(this.plugin, IslandMemberBanButton.class, "SUPERIORSKYBLOCK_MEMBER_BAN"));
        this.buttonManager.register(new NoneLoader(this.plugin, IslandMemberKickButton.class, "SUPERIORSKYBLOCK_MEMBER_KICK"));
        this.buttonManager.register(new NoneLoader(this.plugin, ButtonConfirmBan.class, "SUPERIORSKYBLOCK_CONFIRM_BAN"));
        this.buttonManager.register(new NoneLoader(this.plugin, ButtonConfirmDisband.class, "SUPERIORSKYBLOCK_CONFIRM_DISBAND"));
        this.buttonManager.register(new NoneLoader(this.plugin, ButtonConfirmKick.class, "SUPERIORSKYBLOCK_CONFIRM_KICK"));
        this.buttonManager.register(new NoneLoader(this.plugin, ButtonConfirmLeave.class, "SUPERIORSKYBLOCK_CONFIRM_LEAVE"));
        this.buttonManager.register(new NoneLoader(this.plugin, TargetShowButton.class, "SUPERIORSKYBLOCK_TARGET_SHOW"));
        this.buttonManager.register(new NoneLoader(this.plugin, BankLogsButton.class, "SUPERIORSKYBLOCK_BANK_LOGS"));
        this.buttonManager.register(new NoneLoader(this.plugin, BannedPlayersButton.class, "SUPERIORSKYBLOCK_BANNED_PLAYERS"));
        this.buttonManager.register(new NoneLoader(this.plugin, CoopsButton.class, "SUPERIORSKYBLOCK_COOPS"));
        this.buttonManager.register(new NoneLoader(this.plugin, CountsButton.class, "SUPERIORSKYBLOCK_COUNTS"));
        this.buttonManager.register(new NoneLoader(this.plugin, RatingsButton.class, "SUPERIORSKYBLOCK_RATINGS"));
    }

    public void loadInventories() {

        File folder = new File(plugin.getDataFolder(), "inventories");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Save inventories files
        List<String> strings = Arrays.asList("island-creation", "settings", "biomes", "members",
                "member-manage", "member-role", "permissions", "control-panel", "top-islands",
                "border-color", "confirm-ban", "confirm-disband", "confirm-kick", "confirm-leave",
                "player-language", "values", "bank-logs", "banned-players", "coops", "counts",
                "global-warps", "island-bank", "island-ratings", "island-rate");

        strings.forEach(inventoryName -> {
            if (!new File(plugin.getDataFolder(), "inventories/" + inventoryName + ".yml").exists()) {
                this.plugin.saveResource("inventories/" + inventoryName + ".yml", false);
            }
        });

        this.inventoryManager.deleteInventories(this.plugin);

        this.files(folder, file -> {
            try {
                this.inventoryManager.loadInventory(this.plugin, file);
            } catch (InventoryException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void files(File folder, Consumer<File> consumer) {
        try (Stream<Path> s = Files.walk(Paths.get(folder.getPath()))) {
            s.skip(1).map(Path::toFile).filter(File::isFile).filter(e -> e.getName().endsWith(".yml")).forEach(consumer);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void cache(SuperiorPlayer superiorPlayer, Consumer<PlayerCache> consumer) {
        this.cache(superiorPlayer.asPlayer(), consumer);
    }

    public void cache(Player player, Consumer<PlayerCache> consumer) {
        consumer.accept(getCache(player));
    }

    public void openInventory(SuperiorPlayer superiorPlayer, String inventoryName, Consumer<PlayerCache> consumer) {
        this.openInventory(superiorPlayer.asPlayer(), inventoryName, consumer);
    }

    public void openInventory(Player player, String inventoryName, Consumer<PlayerCache> consumer) {
        this.cache(player, consumer);
        this.openInventory(player, inventoryName);
    }

    public void openInventory(SuperiorPlayer superiorPlayer, String inventoryName) {
        this.openInventory(superiorPlayer.asPlayer(), inventoryName);
    }

    public void openInventory(Player player, String inventoryName) {
        List<Inventory> inventories = new ArrayList<>();
        this.inventoryManager.getCurrentPlayerInventory(player).ifPresent(inventories::add);
        Optional<Inventory> optional = this.inventoryManager.getInventory(plugin, inventoryName);
        if (optional.isPresent()) {
            this.inventoryManager.openInventory(player, optional.get(), 1, inventories);
        } else {
            player.sendMessage(ChatColor.RED + "Impossible to find the inventory " + inventoryName + " !");
        }
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}

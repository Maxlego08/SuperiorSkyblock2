package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    public final String databaseType;
    public final String databaseMySQLAddress;
    public final int databaseMySQLPort;
    public final String databaseMySQLDBName;
    public final String databaseMySQLUsername;
    public final String databaseMySQLPassword;
    public final String databaseMySQLPrefix;
    public final boolean databaseMySQLSSL;
    public final int maxIslandSize;
    public final String islandCommand;
    public final int defaultIslandSize;
    public final KeyMap<Integer> defaultBlockLimits;
    public final Map<EntityType, Integer> defaultEntityLimits;
    public final int defaultWarpsLimit;
    public final int defaultTeamLimit;
    public final int defaultCoopLimit;
    public final int defaultCropGrowth;
    public final int defaultSpawnerRates;
    public final int defaultMobDrops;
    public final int islandsHeight;
    public final boolean worldBordersEnabled;
    public final boolean stackedBlocksEnabled;
    public final KeySet whitelistedStackedBlocks;
    public final List<String> stackedBlocksDisabledWorlds;
    public final String stackedBlocksName;
    public final KeyMap<Integer> stackedBlocksLimits;
    public final boolean stackedBlocksAutoPickup;
    public final String islandLevelFormula;
    public final boolean roundedIslandLevel;
    public final String islandTopOrder;
    public final ConfigurationSection islandRolesSection;
    public final long calcInterval;
    public final String signWarpLine;
    public final List<String> signWarp;
    public final String visitorsSignLine;
    public final String visitorsSignActive;
    public final String visitorsSignInactive;
    public final int bankWorthRate;
    public final String islandWorldName;
    public final boolean netherWorldEnabled;
    public final boolean netherWorldUnlocked;
    public final boolean endWorldEnabled;
    public final boolean endWorldUnlocked;
    public final boolean optimizeWorlds;
    public final String spawnLocation;
    public final boolean spawnProtection;
    public final List<String> spawnSettings;
    public final List<String> spawnPermissions;
    public final boolean spawnWorldBorder;
    public final int spawnSize;
    public final boolean voidTeleport;
    public final List<String> interactables;
    public final boolean visitorsDamage;
    public final boolean coopDamage;
    public final int disbandCount;
    public final boolean islandTopIncludeLeader;
    public final Registry<String, String> defaultPlaceholders;
    public final boolean disbandConfirm;
    public final String spawnersProvider;
    public final boolean disbandInventoryClear;
    public final boolean islandNamesRequiredForCreation;
    public final int islandNamesMaxLength;
    public final int islandNamesMinLength;
    public final List<String> filteredIslandNames;
    public final boolean islandNamesColorSupport;
    public final boolean islandNamesIslandTop;
    public final boolean islandNamesPreventPlayerNames;
    public final boolean teleportOnJoin;
    public final boolean teleportOnKick;
    public final boolean clearOnJoin;
    public final boolean rateOwnIsland;
    public final boolean bonusAffectLevel;
    public final List<String> defaultSettings;
    public final boolean disableRedstoneOffline;
    public final KeyMap<Integer> defaultGenerator;
    public final Registry<String, Pair<Integer, String>> commandsCooldown;
    public final String numberFormat;
    public final boolean skipOneItemMenus;
    public final boolean teleportOnPVPEnable;
    public final boolean immuneToPVPWhenTeleport;
    public final List<String> blockedVisitorsCommands;
    public final boolean defaultContainersEnabled;
    public final Registry<InventoryType, Registry<Integer, ItemStack>> defaultContainersContents;
    public final Registry<String, List<String>> eventCommands;
    public final long warpsWarmup;
    public final long homeWarmup;
    public final boolean liquidUpdate;
    public final List<String> pvpWorlds;
    public final boolean stopLeaving;
    public final boolean valuesMenu;
    public final int chunksPerTick;
    public final List<String> cropsToGrow;
    public final int cropsInterval;
    public final boolean onlyBackButton;
    public final boolean buildOutsideIsland;
    public final String defaultLanguage;
    public final boolean defaultWorldBorder;
    public final boolean defaultBlocksStacker;
    public final boolean defaultToggledPanel;
    public final boolean defaultIslandFly;
    public final String defaultBorderColor;
    public final boolean generators;
    public final boolean obsidianToLava;
    public final boolean syncWorth;
    public final boolean negativeWorth;
    public final boolean negativeLevel;
    public final List<String> disabledEvents;

    public SettingsHandler(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        convertData(cfg);
        convertInteractables(plugin, cfg);

        cfg.syncWithConfig(file, plugin.getResource("config.yml"),  "config.yml",
                "ladder", "commands-cooldown", "containers", "event-commands");

        databaseType = cfg.getString("database.type");
        databaseMySQLAddress = cfg.getString("database.address");
        databaseMySQLPort = cfg.getInt("database.port");
        databaseMySQLDBName = cfg.getString("database.db-name");
        databaseMySQLUsername = cfg.getString("database.user-name");
        databaseMySQLPassword = cfg.getString("database.password");
        databaseMySQLPrefix = cfg.getString("database.prefix");
        databaseMySQLSSL = cfg.getBoolean("database.useSSL");

        calcInterval = cfg.getLong("calc-interval", 6000);
        islandCommand = cfg.getString("island-command", "island,is,islands");
        maxIslandSize = cfg.getInt("max-island-size", 200);
        defaultIslandSize = cfg.getInt("default-values.island-size", 20);
        defaultBlockLimits = new KeyMap<>();
        for(String line : cfg.getStringList("default-values.block-limits")){
            String[] sections = line.split(":");
            String key = sections.length == 2 ? sections[0] : sections[0] + ":" + sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            defaultBlockLimits.put(Key.of(key), Integer.parseInt(limit));
        }
        defaultEntityLimits = new HashMap<>();
        for(String line : cfg.getStringList("default-values.entity-limits")){
            String[] sections = line.split(":");
            try {
                defaultEntityLimits.put(EntityType.valueOf(sections[0]), Integer.parseInt(sections[1]));
            }catch(Exception ignored){}
        }
        defaultTeamLimit = cfg.getInt("default-values.team-limit", 4);
        defaultWarpsLimit = cfg.getInt("default-values.warps-limit", 3);
        defaultCoopLimit = cfg.getInt("default-values.coop-limit", 8);
        defaultCropGrowth = cfg.getInt("default-values.crop-growth", 1);
        defaultSpawnerRates = cfg.getInt("default-values.spawner-rates", 1);
        defaultMobDrops = cfg.getInt("default-values.mob-drops", 1);
        islandsHeight = cfg.getInt("islands-height", 100);
        worldBordersEnabled = cfg.getBoolean("world-borders", true);
        stackedBlocksEnabled = cfg.getBoolean("stacked-blocks.enabled", true);
        stackedBlocksDisabledWorlds = cfg.getStringList("stacked-blocks.disabled-worlds");
        whitelistedStackedBlocks = new KeySet(cfg.getStringList("stacked-blocks.whitelisted"));
        stackedBlocksName = StringUtils.translateColors(cfg.getString("stacked-blocks.custom-name"));
        stackedBlocksLimits = new KeyMap<>();
        cfg.getStringList("stacked-blocks.limits").forEach(line -> {
            String[] sections = line.split(":");
            try {
                if (sections.length == 2)
                    stackedBlocksLimits.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                else if (sections.length == 3)
                    stackedBlocksLimits.put(Key.of(sections[0] + ":" + sections[1]), Integer.parseInt(sections[2]));
            }catch(Exception ignored){}
        });
        stackedBlocksAutoPickup = cfg.getBoolean("stacked-blocks.auto-collect", false);
        islandLevelFormula = cfg.getString("island-level-formula", "{} / 2");
        roundedIslandLevel = cfg.getBoolean("rounded-island-level", false);
        islandTopOrder = cfg.getString("island-top-order", "WORTH");
        islandRolesSection = cfg.getConfigurationSection("island-roles");
        signWarpLine = cfg.getString("sign-warp-line", "[IslandWarp]");
        signWarp = StringUtils.translateColors(cfg.getStringList("sign-warp"));
        visitorsSignLine = cfg.getString("visitors-sign.line", "[Welcome]");
        visitorsSignActive = StringUtils.translateColors(cfg.getString("visitors-sign.active", "&a[Welcome]"));
        visitorsSignInactive = StringUtils.translateColors(cfg.getString("visitors-sign.inactive", "&c[Welcome]"));
        bankWorthRate = cfg.getInt("bank-worth-rate", 1000);
        islandWorldName = cfg.getString("worlds.normal-world", "SuperiorWorld");
        netherWorldEnabled = cfg.getBoolean("worlds.nether-world", false);
        netherWorldUnlocked = cfg.getBoolean("worlds.nether-unlock", true);
        endWorldEnabled = cfg.getBoolean("worlds.end-world", false);
        endWorldUnlocked = cfg.getBoolean("worlds.end-unlock", false);
        optimizeWorlds = cfg.getBoolean("worlds.optimize", false);
        spawnLocation = cfg.getString("spawn.location", "SuperiorWorld, 0, 100, 0, 0, 0");
        spawnProtection = cfg.getBoolean("spawn.protection", true);
        spawnSettings = cfg.getStringList("spawn.settings");
        spawnPermissions = cfg.getStringList("spawn.permissions");
        spawnWorldBorder = cfg.getBoolean("spawn.world-border", false);
        spawnSize = cfg.getInt("spawn.size", 200);
        voidTeleport = cfg.getBoolean("void-teleport", true);
        interactables = loadInteractables(plugin);
        visitorsDamage = cfg.getBoolean("visitors-damage", false);
        coopDamage = cfg.getBoolean("coop-damage", true);
        disbandCount = cfg.getInt("disband-count", 5);
        islandTopIncludeLeader = cfg.getBoolean("island-top-include-leader", true);
        defaultPlaceholders = Registry.createRegistry(cfg.getStringList("default-placeholders").stream().collect(Collectors.toMap(
                line -> line.split(":")[0].replace("superior_", "").toLowerCase(),
                line -> line.split(":")[1]
        )));
        disbandConfirm = cfg.getBoolean("disband-confirm");
        spawnersProvider = cfg.getString("spawners-provider", "AUTO");
        disbandInventoryClear = cfg.getBoolean("disband-inventory-clear", true);
        islandNamesRequiredForCreation = cfg.getBoolean("island-names.required-for-creation", true);
        islandNamesMaxLength = cfg.getInt("island-names.max-length", 16);
        islandNamesMinLength = cfg.getInt("island-names.min-length", 3);
        filteredIslandNames = cfg.getStringList("island-names.filtered-names");
        islandNamesColorSupport = cfg.getBoolean("island-names.color-support", true);
        islandNamesIslandTop = cfg.getBoolean("island-names.island-top", true);
        islandNamesPreventPlayerNames = cfg.getBoolean("island-names.prevent-player-names", true);
        teleportOnJoin = cfg.getBoolean("teleport-on-join", false);
        teleportOnKick = cfg.getBoolean("teleport-on-kick", false);
        clearOnJoin = cfg.getBoolean("clear-on-join", false);
        rateOwnIsland = cfg.getBoolean("rate-own-island", false);
        bonusAffectLevel = cfg.getBoolean("bonus-affect-level", true);
        defaultSettings = cfg.getStringList("default-settings");
        defaultGenerator = new KeyMap<>();
        for(String line : cfg.getStringList("default-generator")){
            String[] sections = line.split(":");
            String key = sections.length == 2 ? sections[0] : sections[0] + sections[1];
            String percentage = sections.length == 2 ? sections[1] : sections[2];
            defaultGenerator.put(key, Integer.parseInt(percentage));
        }
        disableRedstoneOffline = cfg.getBoolean("disable-redstone-offline", true);
        commandsCooldown = Registry.createRegistry();
        for(String subCommand : cfg.getConfigurationSection("commands-cooldown").getKeys(false)){
            int cooldown = cfg.getInt("commands-cooldown." + subCommand + ".cooldown");
            String permission = cfg.getString("commands-cooldown." + subCommand + ".bypass-permission");
            commandsCooldown.add(subCommand, new Pair<>(cooldown, permission));
        }
        numberFormat = cfg.getString("number-format", "en-US");
        StringUtils.setNumberFormatter(numberFormat);
        skipOneItemMenus = cfg.getBoolean("skip-one-item-menus", false);
        teleportOnPVPEnable = cfg.getBoolean("teleport-on-pvp-enable", true);
        immuneToPVPWhenTeleport = cfg.getBoolean("immune-to-pvp-when-teleport", true);
        blockedVisitorsCommands = cfg.getStringList("blocked-visitors-commands");
        defaultContainersEnabled = cfg.getBoolean("default-containers.enabled", false);
        defaultContainersContents = Registry.createRegistry();
        for(String container : cfg.getConfigurationSection("default-containers.containers").getKeys(false)){
            try {
                InventoryType containerType = InventoryType.valueOf(container.toUpperCase());
                Registry<Integer, ItemStack>  containerContents = Registry.createRegistry();
                ConfigurationSection containerSection = cfg.getConfigurationSection("default-containers.containers." + container);
                for(String slot : containerSection.getKeys(false)) {
                    try {
                        ItemStack itemStack = FileUtils.getItemStack("config.yml", containerSection.getConfigurationSection(slot)).build();
                        itemStack.setAmount(containerSection.getInt(slot + ".amount", 1));
                        containerContents.add(Integer.parseInt(slot), itemStack);
                    } catch (Exception ignored) { }
                }
                defaultContainersContents.add(containerType, containerContents);
            }catch (IllegalArgumentException ex){
                SuperiorSkyblockPlugin.log("&cInvalid container type: " + container + ".");
            }
        }
        eventCommands = Registry.createRegistry();
        for(String eventName : cfg.getConfigurationSection("event-commands").getKeys(false)){
            eventCommands.add(eventName.toLowerCase(), cfg.getStringList("event-commands." + eventName));
        }
        warpsWarmup = cfg.getLong("warps-warmup", 0);
        homeWarmup = cfg.getLong("home-warmup", 0);
        liquidUpdate = cfg.getBoolean("liquid-update", false);
        pvpWorlds = cfg.getStringList("pvp-worlds");
        stopLeaving = cfg.getBoolean("stop-leaving", false);
        valuesMenu = cfg.getBoolean("values-menu", true);
        chunksPerTick = cfg.getInt("chunks-per-tick", 10);
        cropsToGrow = cfg.getStringList("crops-to-grow");
        cropsInterval = cfg.getInt("crops-interval", 5);
        onlyBackButton = cfg.getBoolean("only-back-button", false);
        buildOutsideIsland = cfg.getBoolean("build-outside-island", false);
        defaultLanguage = cfg.getString("default-language", "en-US");
        defaultWorldBorder = cfg.getBoolean("default-world-border", true);
        defaultBlocksStacker = cfg.getBoolean("default-blocks-stacker", true);
        defaultToggledPanel = cfg.getBoolean("default-toggled-panel", false);
        defaultIslandFly = cfg.getBoolean("default-island-fly", false);
        defaultBorderColor = cfg.getString("default-border-color", "BLUE");
        generators = cfg.getBoolean("generators", true);
        obsidianToLava = cfg.getBoolean("obsidian-to-lava", false);
        syncWorth = cfg.getBoolean("sync-worth", false);
        negativeWorth = cfg.getBoolean("negative-worth", true);
        negativeLevel = cfg.getBoolean("negative-level", true);
        disabledEvents = cfg.getStringList("disabled-events").stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    public void updateValue(String path, Object value){
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, plugin.getResource("config.yml"), "config.yml",
                "ladder", "commands-cooldown", "containers", "event-commands");

        cfg.set(path, value);

        cfg.save(file);

        plugin.setSettings(new SettingsHandler(plugin));
    }

    private void convertData(YamlConfiguration cfg){
        if(cfg.contains("default-hoppers-limit")){
            cfg.set("default-limits", Collections.singletonList("HOPPER:" + cfg.getInt("default-hoppers-limit")));
            cfg.set("default-hoppers-limit", null);
        }
        if(cfg.contains("default-permissions")){
            cfg.set("island-roles.guest.name", "Guest");
            cfg.set("island-roles.guest.permissions", cfg.getStringList("default-permissions.guest"));
            cfg.set("island-roles.ladder.member.name", "Member");
            cfg.set("island-roles.ladder.member.weight", 0);
            cfg.set("island-roles.ladder.member.permissions", cfg.getStringList("default-permissions.member"));
            cfg.set("island-roles.ladder.mod.name", "Moderator");
            cfg.set("island-roles.ladder.mod.weight", 1);
            cfg.set("island-roles.ladder.mod.permissions", cfg.getStringList("default-permissions.mod"));
            cfg.set("island-roles.ladder.admin.name", "Admin");
            cfg.set("island-roles.ladder.admin.weight", 2);
            cfg.set("island-roles.ladder.admin.permissions", cfg.getStringList("default-permissions.admin"));
            cfg.set("island-roles.ladder.leader.name", "Leader");
            cfg.set("island-roles.ladder.leader.weight", 3);
            cfg.set("island-roles.ladder.leader.permissions", cfg.getStringList("default-permissions.leader"));
        }
        if(cfg.contains("spawn-location"))
            cfg.set("spawn.location", cfg.getString("spawn-location"));
        if(cfg.contains("spawn-protection"))
            cfg.set("spawn.protection", cfg.getBoolean("spawn-protection"));
        if(cfg.getBoolean("spawn-pvp", false))
            cfg.set("spawn.settings", Collections.singletonList("PVP"));
        if(cfg.contains("island-world"))
            cfg.set("worlds.normal-world", cfg.getString("island-world"));
        if(cfg.contains("welcome-sign-line"))
            cfg.set("visitors-sign.line", cfg.getString("welcome-sign-line"));
        if(cfg.contains("island-roles.ladder")){
            for(String name : cfg.getConfigurationSection("island-roles.ladder").getKeys(false)){
                if(!cfg.contains("island-roles.ladder." + name + ".id"))
                    cfg.set("island-roles.ladder." + name + ".id", cfg.getInt("island-roles.ladder." + name + ".weight"));
            }
        }
        if(cfg.contains("default-island-size"))
            cfg.set("default-values.island-size", cfg.getInt("default-island-size"));
        if(cfg.contains("default-limits"))
            cfg.set("default-values.block-limits", cfg.getStringList("default-limits"));
        if(cfg.contains("default-entity-limits"))
            cfg.set("default-values.entity-limits", cfg.getStringList("default-entity-limits"));
        if(cfg.contains("default-warps-limit"))
            cfg.set("default-values.warps-limit", cfg.getInt("default-warps-limit"));
        if(cfg.contains("default-team-limit"))
            cfg.set("default-values.team-limit", cfg.getInt("default-team-limit"));
        if(cfg.contains("default-crop-growth"))
            cfg.set("default-values.crop-growth", cfg.getInt("default-crop-growth"));
        if(cfg.contains("default-spawner-rates"))
            cfg.set("default-values.spawner-rates", cfg.getInt("default-spawner-rates"));
        if(cfg.contains("default-mob-drops"))
            cfg.set("default-values.mob-drops", cfg.getInt("default-mob-drops"));
        if(cfg.contains("default-island-height"))
            cfg.set("islands-height", cfg.getInt("default-island-height"));
        if(cfg.contains("starter-chest")){
            cfg.set("default-containers.enabled", cfg.getBoolean("starter-chest.enabled"));
            cfg.set("default-containers.containers.chest", cfg.getConfigurationSection("starter-chest.contents"));
        }
    }

    private void convertInteractables(SuperiorSkyblockPlugin plugin, YamlConfiguration cfg){
        if(!cfg.contains("interactables"))
            return;

        File file = new File(plugin.getDataFolder(), "interactables.yml");

        if(!file.exists())
            plugin.saveResource("interactables.yml", false);

        CommentedConfiguration commentedConfig = CommentedConfiguration.loadConfiguration(file);

        commentedConfig.set("interactables", cfg.getStringList("interactables"));

        commentedConfig.save(file);
    }

    private List<String> loadInteractables(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "interactables.yml");

        if(!file.exists())
            plugin.saveResource("interactables.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        return cfg.getStringList("interactables");
    }

}

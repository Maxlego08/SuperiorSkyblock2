package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.reflections.Fields;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.bgsoftware.superiorskyblock.api.key.Key;
import net.minecraft.server.v1_11_R1.BiomeBase;
import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.Blocks;
import net.minecraft.server.v1_11_R1.Chunk;
import net.minecraft.server.v1_11_R1.ChunkSection;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EnumParticle;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;
import net.minecraft.server.v1_11_R1.SoundCategory;
import net.minecraft.server.v1_11_R1.SoundEffects;
import net.minecraft.server.v1_11_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_11_R1.World;
import net.minecraft.server.v1_11_R1.WorldBorder;
import net.minecraft.server.v1_11_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_11_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.util.UnsafeList;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_11_R1 implements NMSAdapter {

    private SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public void registerCommand(BukkitCommand command) {
        ((CraftServer) plugin.getServer()).getCommandMap().register("superiorskyblock2", command);
    }

    @Override
    @Deprecated
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        Material type = Material.getMaterial(chunkSnapshot.getBlockTypeId(x, y, z));
        short data = (short) chunkSnapshot.getBlockData(x, y, z);
        return Key.of(type, data);
    }

    @Override
    public int getSpawnerDelay(CreatureSpawner creatureSpawner) {
        Location location = creatureSpawner.getLocation();
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)((CraftWorld) location.getWorld())
                .getTileEntityAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return mobSpawner.getSpawner().spawnDelay;
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        try {
            if (!plugin.getSettings().worldBordersEnabled)
                return;

            boolean disabled = !superiorPlayer.hasWorldBorderEnabled();

            WorldBorder worldBorder = new WorldBorder();

            worldBorder.world = ((CraftWorld) superiorPlayer.getWorld()).getHandle();
            worldBorder.setSize(disabled || island == null || (!plugin.getSettings().spawnWorldBorder && island.isSpawn()) ? Integer.MAX_VALUE : (island.getIslandSize() * 2) + 1);

            org.bukkit.World.Environment environment = superiorPlayer.getWorld().getEnvironment();

            Location center = island == null ? superiorPlayer.getLocation() : island.getCenter(environment);

            if(environment == org.bukkit.World.Environment.NETHER){
                worldBorder.setCenter(center.getX() * 8, center.getZ() * 8);
            }
            else{
                worldBorder.setCenter(center.getX(), center.getZ());
            }

            switch (superiorPlayer.getBorderColor()){
                case GREEN:
                    worldBorder.transitionSizeBetween(worldBorder.getSize() - 0.1D, worldBorder.getSize(), Long.MAX_VALUE);
                    break;
                case RED:
                    worldBorder.transitionSizeBetween(worldBorder.getSize(), worldBorder.getSize() - 1.0D, Long.MAX_VALUE);
                    break;
            }

            PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
            ((CraftPlayer) superiorPlayer.asPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        EntityPlayer entityPlayer = ((CraftPlayer) superiorPlayer.asPlayer()).getHandle();
        Optional<Property> optional = entityPlayer.getProfile().getProperties().get("textures").stream().findFirst();
        optional.ifPresent(property -> superiorPlayer.setTextureValue(property.getValue()));
    }

    @Override
    public void clearInventory(OfflinePlayer offlinePlayer) {
        if(offlinePlayer.isOnline() || offlinePlayer instanceof Player){
            Player player = offlinePlayer instanceof Player ? (Player) offlinePlayer : offlinePlayer.getPlayer();
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName());

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(0);
        EntityPlayer entity = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
        Player targetPlayer = entity.getBukkitEntity();

        targetPlayer.loadData();

        clearInventory(targetPlayer);

        //Setting the entity to the spawn location
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(org.bukkit.World.Environment.NORMAL);
        entity.world = ((CraftWorld) spawnLocation.getWorld()).getHandle();
        entity.setPositionRotation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        targetPlayer.saveData();
    }

    @Override
    public void playGeneratorSound(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        double x = location.getX(), y = location.getY(), z = location.getZ();
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        world.a(null, blockPosition, SoundEffects.dr, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

        for(int i = 0; i < 8; i++)
            world.addParticle(EnumParticle.SMOKE_LARGE, x + Math.random(), y + 1.2D, z + Math.random(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void setBiome(org.bukkit.Chunk bukkitChunk, Biome biome) {
        byte biomeBase = (byte) BiomeBase.REGISTRY_ID.a(CraftBlock.biomeToBiomeBase(biome));
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        Arrays.fill(chunk.getBiomeIndex(), biomeBase);
        chunk.e();
    }

    @Override
    public Enchantment getGlowEnchant() {
        int id = 100;

        //noinspection StatementWithEmptyBody, deprecation
        while(Enchantment.getById(id++) != null);

        return new Enchantment(id) {
            @Override
            public String getName() {
                return "SuperiorSkyblockGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }

            @Override
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
            }
        };
    }

    @Override
    public void regenerateChunk(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        for(int i = 0; i < 16; i++)
            chunk.getSections()[i] = null;

        for(int i = 0; i < 16; i++)
            chunk.entitySlices[i] = new UnsafeList<>();

        chunk.tileEntities.keySet().forEach(chunk.world::s);
        chunk.tileEntities.clear();

        ChunksTracker.markEmpty(bukkitChunk);
    }

    @Override
    public void injectChunkSections(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        for(int i = 0; i < 16; i++)
            chunk.getSections()[i] = EmptyCounterChunkSection.of(chunk.getSections()[i]);
    }

    @Override
    public boolean isChunkEmpty(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(chunk.getSections()).allMatch(Objects::isNull);
    }

    @Override
    public ItemStack[] getEquipment(EntityEquipment entityEquipment) {
        ItemStack[] itemStacks = new ItemStack[7];

        itemStacks[0] = new ItemStack(Material.ARMOR_STAND);
        itemStacks[1] = entityEquipment.getItemInMainHand();
        itemStacks[2] = entityEquipment.getItemInOffHand();
        itemStacks[3] = entityEquipment.getHelmet();
        itemStacks[4] = entityEquipment.getChestplate();
        itemStacks[5] = entityEquipment.getLeggings();
        itemStacks[6] = entityEquipment.getBoots();

        return itemStacks;
    }

    private static class EmptyCounterChunkSection extends ChunkSection {

        private int nonEmptyBlockCount, tickingBlockCount;

        EmptyCounterChunkSection(ChunkSection chunkSection){
            super(chunkSection.getYPosition(), chunkSection.getSkyLightArray() != null);

            nonEmptyBlockCount = (int) Fields.CHUNK_SECTION_NON_EMPTY_BLOCK_COUNT.get(chunkSection);
            tickingBlockCount = (int) Fields.CHUNK_SECTION_TICKING_BLOCK_COUNT.get(chunkSection);
            Fields.CHUNK_SECTION_BLOCK_IDS.set(this, chunkSection.getBlocks());
            Fields.CHUNK_SECTION_EMITTED_LIGHT.set(this, chunkSection.getEmittedLightArray());
            Fields.CHUNK_SECTION_SKY_LIGHT.set(this, chunkSection.getSkyLightArray());
        }

        @Override
        public void setType(int i, int j, int k, IBlockData iblockdata) {
            Block currentBlock = getType(i, j, k).getBlock(), placedBlock = iblockdata.getBlock();

            if (currentBlock != Blocks.AIR) {
                nonEmptyBlockCount--;
                if (currentBlock.isTicking()) {
                    tickingBlockCount--;
                }
            }

            if (placedBlock != Blocks.AIR) {
                nonEmptyBlockCount++;
                if (placedBlock.isTicking()) {
                    tickingBlockCount++;
                }
            }

            super.setType(i, j, k, iblockdata);
        }

        public void recalcBlockCounts() {
            nonEmptyBlockCount = 0;
            tickingBlockCount = 0;

            for(int i = 0; i < 16; ++i) {
                for(int j = 0; j < 16; ++j) {
                    for(int k = 0; k < 16; ++k) {
                        Block block = getType(i, j, k).getBlock();
                        if (block != Blocks.AIR) {
                            nonEmptyBlockCount++;
                            if (block.isTicking()) {
                                tickingBlockCount++;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean shouldTick() {
            return tickingBlockCount > 0;
        }

        @Override
        public boolean a() {
            return nonEmptyBlockCount == 0;
        }

        static EmptyCounterChunkSection of(ChunkSection chunkSection){
            return chunkSection == null ?  null : new EmptyCounterChunkSection(chunkSection);
        }

    }

}

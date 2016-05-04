package net.tangentmc.nmsUtils.v1_9_R1;

import com.google.common.base.Preconditions;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.World;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSArmorStand;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R1.CraftEffect;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R1.metadata.BlockMetadataStore;
import org.bukkit.craftbukkit.v1_9_R1.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_9_R1.util.CraftMagicNumbers;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.*;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class NMSCraftWorld extends CraftWorld{

    private CraftWorld world;
    public NMSCraftWorld(CraftWorld world) {
        super(world.getHandle(), world.getGenerator(), world.getEnvironment());
        this.world = world;
    }
    @Override
    public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data) throws IllegalArgumentException {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(material, "Material cannot be null");
        Validate.isTrue(material.isBlock(), "Material must be a block");

        double x = location.getBlockX() + 0.5;
        double y = location.getBlockY() + 0.5;
        double z = location.getBlockZ() + 0.5;

        EntityFallingBlock entity = (EntityFallingBlock) instrument(EntityFallingBlock.class,world, x, y, z, net.minecraft.server.v1_9_R1.Block.getById(material.getId()).fromLegacyData(data));
        entity.ticksLived = 1;

        world.addEntity(entity, SpawnReason.CUSTOM);
        return (FallingBlock) entity.getBukkitEntity();
    }
    public net.minecraft.server.v1_9_R1.Entity instrument(Class<? extends net.minecraft.server.v1_9_R1.Entity> clazz, Object... args2) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        //Load from bukkits class loader not the default one
        enhancer.setClassLoader(NMSCraftWorld.class.getClassLoader());
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if(method.getDeclaringClass() != Object.class && method.getName().equals("m")) {
                Collideable.testCollision((net.minecraft.server.v1_9_R1.Entity)obj);
                proxy.invokeSuper(obj, args);
                Collideable.testMovement((net.minecraft.server.v1_9_R1.Entity)obj);
                return null;
            } else {
                return proxy.invokeSuper(obj, args);
            }
        });
        //We should do a better fit for constructors
        Class<?>[] classes = new Class[args2.length];
        for (int i = 0; i < args2.length; i++) {
            classes[i] = args2[i].getClass();

            if (World.class.isAssignableFrom(classes[i])) {
                classes[i] = World.class;

            }
        }
        return (net.minecraft.server.v1_9_R1.Entity) enhancer.create(classes,args2);
    }
    @SuppressWarnings("unchecked")
    //For updates, use regex entity = new (\w*)\(([()A-z ,_]*)\); and replace with entity = instrument($1.class, $2);
    public net.minecraft.server.v1_9_R1.Entity createEntity(Location location, Class<? extends Entity> clazz) throws IllegalArgumentException {
        if (location == null || clazz == null) {
            throw new IllegalArgumentException("Location or entity class cannot be null");
        }

        net.minecraft.server.v1_9_R1.Entity entity = null;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        WorldServer world = this.world.getHandle();
        // order is important for some of these
        if (Boat.class.isAssignableFrom(clazz)) {
            entity = instrument(EntityBoat.class, world, x, y, z);
        } else if (FallingBlock.class.isAssignableFrom(clazz)) {
            x = location.getBlockX();
            y = location.getBlockY();
            z = location.getBlockZ();
            IBlockData blockData = world.getType(new BlockPosition(x, y, z));
            int type = CraftMagicNumbers.getId(blockData.getBlock());
            int data = blockData.getBlock().toLegacyData(blockData);
            entity = instrument(EntityFallingBlock.class, world, x + 0.5, y + 0.5, z + 0.5, net.minecraft.server.v1_9_R1.Block.getById(type).fromLegacyData(data));
        } else if (Projectile.class.isAssignableFrom(clazz)) {
            if (Snowball.class.isAssignableFrom(clazz)) {
                entity = instrument(EntitySnowball.class, world, x, y, z);
            } else if (Egg.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityEgg.class, world, x, y, z);
            } else if (Arrow.class.isAssignableFrom(clazz)) {
                if (TippedArrow.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityTippedArrow.class, world);
                    ((EntityTippedArrow) entity).setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
                } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntitySpectralArrow.class, world);
                } else {
                    entity = instrument(EntityTippedArrow.class, world);
                }
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (ThrownExpBottle.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityThrownExpBottle.class, world);
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (EnderPearl.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityEnderPearl.class, world, null);
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (ThrownPotion.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityPotion.class, world, x, y, z, CraftItemStack.asNMSCopy(new ItemStack(LingeringPotion.class.isAssignableFrom(clazz)? Material.LINGERING_POTION:Material.SPLASH_POTION, 1)));
            } else if (Fireball.class.isAssignableFrom(clazz)) {
                if (SmallFireball.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntitySmallFireball.class, world);
                } else if (WitherSkull.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityWitherSkull.class, world);
                } else if (DragonFireball.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityDragonFireball.class, world);
                } else {
                    entity = instrument(EntityLargeFireball.class, world);
                }
                entity.setPositionRotation(x, y, z, yaw, pitch);
                Vector direction = location.getDirection().multiply(10);
                ((EntityFireball) entity).setDirection(direction.getX(), direction.getY(), direction.getZ());
            } else if (ShulkerBullet.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityShulkerBullet.class, world);
                entity.setPositionRotation(x, y, z, yaw, pitch);
            }
        } else if (Minecart.class.isAssignableFrom(clazz)) {
            if (PoweredMinecart.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityMinecartFurnace.class, world, x, y, z);
            } else if (StorageMinecart.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityMinecartChest.class, world, x, y, z);
            } else if (ExplosiveMinecart.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityMinecartTNT.class, world, x, y, z);
            } else if (HopperMinecart.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityMinecartHopper.class, world, x, y, z);
            } else if (SpawnerMinecart.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityMinecartMobSpawner.class, world, x, y, z);
            } else { // Default to rideable minecart for pre-rideable compatibility
                entity = instrument(EntityMinecartRideable.class, world, x, y, z);
            }
        } else if (EnderSignal.class.isAssignableFrom(clazz)) {
            entity = instrument(EntityEnderSignal.class, world, x, y, z);
        } else if (EnderCrystal.class.isAssignableFrom(clazz)) {
            entity = instrument(EntityEnderCrystal.class, world);
            entity.setPositionRotation(x, y, z, 0, 0);
        } else if (LivingEntity.class.isAssignableFrom(clazz)) {
            if (Chicken.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityChicken.class, world);
            } else if (Cow.class.isAssignableFrom(clazz)) {
                if (MushroomCow.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityMushroomCow.class, world);
                } else {
                    entity = instrument(EntityCow.class, world);
                }
            } else if (Golem.class.isAssignableFrom(clazz)) {
                if (Snowman.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntitySnowman.class, world);
                } else if (IronGolem.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityIronGolem.class, world);
                } else if (Shulker.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityShulker.class, world);
                }
            } else if (Creeper.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityCreeper.class, world);
            } else if (Ghast.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityGhast.class, world);
            } else if (Pig.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityPig.class, world);
            } else if (Player.class.isAssignableFrom(clazz)) {
                // need a net server handler for this one
            } else if (Sheep.class.isAssignableFrom(clazz)) {
                entity = instrument(EntitySheep.class, world);
            } else if (Horse.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityHorse.class, world);
            } else if (Skeleton.class.isAssignableFrom(clazz)) {
                entity = instrument(EntitySkeleton.class, world);
            } else if (Slime.class.isAssignableFrom(clazz)) {
                if (MagmaCube.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityMagmaCube.class, world);
                } else {
                    entity = instrument(EntitySlime.class, world);
                }
            } else if (Spider.class.isAssignableFrom(clazz)) {
                if (CaveSpider.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityCaveSpider.class, world);
                } else {
                    entity = instrument(EntitySpider.class, world);
                }
            } else if (Squid.class.isAssignableFrom(clazz)) {
                entity = instrument(EntitySquid.class, world);
            } else if (Tameable.class.isAssignableFrom(clazz)) {
                if (Wolf.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityWolf.class, world);
                } else if (Ocelot.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityOcelot.class, world);
                }
            } else if (PigZombie.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityPigZombie.class, world);
            } else if (Zombie.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityZombie.class, world);
            } else if (Giant.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityGiantZombie.class, world);
            } else if (Silverfish.class.isAssignableFrom(clazz)) {
                entity = instrument(EntitySilverfish.class, world);
            } else if (Enderman.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityEnderman.class, world);
            } else if (Blaze.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityBlaze.class, world);
            } else if (Villager.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityVillager.class, world);
            } else if (Witch.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityWitch.class, world);
            } else if (Wither.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityWither.class, world);
            } else if (ComplexLivingEntity.class.isAssignableFrom(clazz)) {
                if (EnderDragon.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityEnderDragon.class, world);
                }
            } else if (Ambient.class.isAssignableFrom(clazz)) {
                if (Bat.class.isAssignableFrom(clazz)) {
                    entity = instrument(EntityBat.class, world);
                }
            } else if (Rabbit.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityRabbit.class, world);
            } else if (Endermite.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityEndermite.class, world);
            } else if (Guardian.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityGuardian.class, world);
            } else if (ArmorStand.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityArmorStand.class, world, x, y, z);
            }

            if (entity != null) {
                entity.setLocation(x, y, z, yaw, pitch);
            }
        } else if (Hanging.class.isAssignableFrom(clazz)) {
            Block block = getBlockAt(location);
            BlockFace face = BlockFace.SELF;

            int width = 16; // 1 full block, also painting smallest size.
            int height = 16; // 1 full block, also painting smallest size.

            if (ItemFrame.class.isAssignableFrom(clazz)) {
                width = 12;
                height = 12;
            } else if (LeashHitch.class.isAssignableFrom(clazz)) {
                width = 9;
                height = 9;
            }

            BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};
            final BlockPosition pos = new BlockPosition((int) x, (int) y, (int) z);
            for (BlockFace dir : faces) {
                net.minecraft.server.v1_9_R1.Block nmsBlock = CraftMagicNumbers.getBlock(block.getRelative(dir));
                if (nmsBlock.getBlockData().getMaterial().isBuildable() || BlockDiodeAbstract.isDiode(nmsBlock.getBlockData())) {
                    boolean taken = false;
                    AxisAlignedBB bb = EntityHanging.calculateBoundingBox(null, pos, CraftBlock.blockFaceToNotch(dir).opposite(), width, height);
                    List<net.minecraft.server.v1_9_R1.Entity> list = world.getEntities(null, bb);
                    for (Iterator<net.minecraft.server.v1_9_R1.Entity> it = list.iterator(); !taken && it.hasNext();) {
                        net.minecraft.server.v1_9_R1.Entity e = it.next();
                        if (e instanceof EntityHanging) {
                            taken = true; // Hanging entities do not like hanging entities which intersect them.
                        }
                    }

                    if (!taken) {
                        face = dir;
                        break;
                    }
                }
            }

            EnumDirection dir = CraftBlock.blockFaceToNotch(face).opposite();
            if (Painting.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityPainting.class, world, new BlockPosition((int) x, (int) y, (int) z), dir);
            } else if (ItemFrame.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityItemFrame.class, world, new BlockPosition((int) x, (int) y, (int) z), dir);
            } else if (LeashHitch.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityLeash.class, world, new BlockPosition((int) x, (int) y, (int) z));
                entity.attachedToPlayer = true;
            }

            if (entity != null && !((EntityHanging) entity).survives()) {
                throw new IllegalArgumentException("Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
            }
        } else if (TNTPrimed.class.isAssignableFrom(clazz)) {
            entity = instrument(EntityTNTPrimed.class, world, x, y, z, null);
        } else if (ExperienceOrb.class.isAssignableFrom(clazz)) {
            entity = new EntityExperienceOrb(world, x, y, z, 0);
        } else if (Weather.class.isAssignableFrom(clazz)) {
            // not sure what this can do
            if (LightningStrike.class.isAssignableFrom(clazz)) {
                entity = instrument(EntityLightning.class, world, x, y, z, false);
                // what is this, I don't even
            }
        } else if (Firework.class.isAssignableFrom(clazz)) {
            entity = instrument(EntityFireworks.class, world, x, y, z, null);
        } else if (AreaEffectCloud.class.isAssignableFrom(clazz)) {
            entity = instrument(EntityAreaEffectCloud.class, world, x, y, z);
        }
        if (entity != null) {
            return entity;
        }

        throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
    }


    public Block getBlockAt(int x, int y, int z) {
        return world.getBlockAt(x, y, z);
    }

    public int getBlockTypeIdAt(int x, int y, int z) {
        return world.getBlockTypeIdAt(x,y,z);
    }

    public int getHighestBlockYAt(int x, int z) {
        return world.getHighestBlockYAt(x, z);
    }

    public Location getSpawnLocation() {
        return world.getSpawnLocation();
    }

    public boolean setSpawnLocation(int x, int y, int z) {
        return world.setSpawnLocation(x, y, z);
    }

    public Chunk getChunkAt(int x, int z) {
        return world.getChunkAt(x, z);
    }

    public Chunk getChunkAt(Block block) {
        return world.getChunkAt(block);
    }

    public boolean isChunkLoaded(int x, int z) {
        return world.isChunkLoaded(x, z);
    }

    public Chunk[] getLoadedChunks() {
        return world.getLoadedChunks();
    }

    public void loadChunk(int x, int z) {
        world.loadChunk(x,z);
    }

    public boolean unloadChunk(Chunk chunk) {
        return world.unloadChunk(chunk.getX(), chunk.getZ());
    }

    public boolean unloadChunk(int x, int z) {
        return world.unloadChunk(x, z, true);
    }

    public boolean unloadChunk(int x, int z, boolean save) {
        return world.unloadChunk(x, z, save, false);
    }

    public boolean unloadChunkRequest(int x, int z) {
        return world.unloadChunkRequest(x, z, true);
    }

    public boolean unloadChunkRequest(int x, int z, boolean safe) {
        return world.unloadChunkRequest(x, z, safe);
    }

    public boolean unloadChunk(int x, int z, boolean save, boolean safe) {
        return world.unloadChunk(x, z, save, safe);
    }

    public boolean regenerateChunk(int x, int z) {
        return world.unloadChunk(x,z);
    }

    public boolean refreshChunk(int x, int z) {
        return world.refreshChunk(x, z);
    }

    public boolean isChunkInUse(int x, int z) {
        return world.isChunkInUse(x, z);
    }

    public boolean loadChunk(int x, int z, boolean generate) {
        return world.loadChunk(x, z, generate);
    }

    public boolean isChunkLoaded(Chunk chunk) {
        return world.isChunkLoaded(chunk.getX(), chunk.getZ());
    }

    public void loadChunk(Chunk chunk) {
        world.loadChunk(chunk);
    }

    public WorldServer getHandle() {
        return world.getHandle();
    }

    public org.bukkit.entity.Item dropItem(Location loc, ItemStack item) {
        Validate.notNull(item, "Cannot drop a Null item.");
        Validate.isTrue(item.getTypeId() != 0, "Cannot drop AIR.");
        EntityItem entity = (EntityItem) instrument(EntityItem.class,world.getHandle(), loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
        entity.pickupDelay = 10;
        world.getHandle().addEntity(entity);
        // TODO this is inconsistent with how Entity.getBukkitEntity() works.
        // However, this entity is not at the moment backed by a server entity class so it may be left.
        return new CraftItem(world.getHandle().getServer(), entity);
    }

    private static void randomLocationWithinBlock(Location loc, double xs, double ys, double zs) {
        double prevX = loc.getX();
        double prevY = loc.getY();
        double prevZ = loc.getZ();
        loc.add(xs, ys, zs);
        if (loc.getX() < Math.floor(prevX)) {
            loc.setX(Math.floor(prevX));
        }
        if (loc.getX() >= Math.ceil(prevX)) {
            loc.setX(Math.ceil(prevX - 0.01));
        }
        if (loc.getY() < Math.floor(prevY)) {
            loc.setY(Math.floor(prevY));
        }
        if (loc.getY() >= Math.ceil(prevY)) {
            loc.setY(Math.ceil(prevY - 0.01));
        }
        if (loc.getZ() < Math.floor(prevZ)) {
            loc.setZ(Math.floor(prevZ));
        }
        if (loc.getZ() >= Math.ceil(prevZ)) {
            loc.setZ(Math.ceil(prevZ - 0.01));
        }
    }

    public org.bukkit.entity.Item dropItemNaturally(Location loc, ItemStack item) {
        double xs = world.getHandle().random.nextFloat() * 0.7F - 0.35D;
        double ys = world.getHandle().random.nextFloat() * 0.7F - 0.35D;
        double zs = world.getHandle().random.nextFloat() * 0.7F - 0.35D;
        loc = loc.clone();
        // Makes sure the new item is created within the block the location points to.
        // This prevents item spill in 1-block wide farms.
        randomLocationWithinBlock(loc, xs, ys, zs);
        return dropItem(loc, item);
    }
    @Override
    public <T extends Arrow> T spawnArrow(Location loc, Vector velocity, float speed, float spread, Class<T> clazz) {
        Validate.notNull(loc, "Can not spawn arrow with a null location");
        Validate.notNull(velocity, "Can not spawn arrow with a null velocity");
        Validate.notNull(clazz, "Can not spawn an arrow with no class");

        EntityArrow arrow;
        if (TippedArrow.class.isAssignableFrom(clazz)) {
            arrow = (EntityArrow) instrument(EntityTippedArrow.class,world);
            ((EntityTippedArrow) arrow).setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
        } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
            arrow = (EntityArrow) instrument(EntitySpectralArrow.class,world);
        } else {
            arrow = (EntityArrow) instrument(EntityTippedArrow.class,world);
        }

        arrow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        arrow.shoot(velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
        world.getHandle().addEntity(arrow);
        return (T) arrow.getBukkitEntity();
    }

    public Entity spawnEntity(Location loc, EntityType entityType) {
        return spawn(loc, entityType.getEntityClass());
    }

    public LightningStrike strikeLightning(Location loc) {
        EntityLightning lightning = new EntityLightning(world.getHandle(), loc.getX(), loc.getY(), loc.getZ(),false);
        world.getHandle().strikeLightning(lightning);
        return new CraftLightningStrike(world.getHandle().getServer(), lightning);
    }

    public LightningStrike strikeLightningEffect(Location loc) {
        EntityLightning lightning = new EntityLightning(world.getHandle(), loc.getX(), loc.getY(), loc.getZ(), true);
        world.getHandle().strikeLightning(lightning);
        return new CraftLightningStrike(world.getHandle().getServer(), lightning);
    }

    public boolean generateTree(Location loc, TreeType type) {
        return world.generateTree(loc, type);
    }

    public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate) {
        return world.generateTree(loc, type, delegate);
    }

    public TileEntity getTileEntityAt(final int x, final int y, final int z) {
        return world.getTileEntityAt(x,y,z);
    }

    public String getName() {
        return world.getHandle().worldData.getName();
    }

    @Deprecated
    public long getId() {
        return world.getHandle().worldData.getSeed();
    }

    public UUID getUID() {
        return world.getHandle().getDataManager().getUUID();
    }

    @Override
    public String toString() {
        return "CraftWorld{name=" + getName() + '}';
    }

    public long getTime() {
        long time = getFullTime() % 24000;
        if (time < 0) time += 24000;
        return time;
    }

    public void setTime(long time) {
        long margin = (time - getFullTime()) % 24000;
        if (margin < 0) margin += 24000;
        setFullTime(getFullTime() + margin);
    }

    public long getFullTime() {
        return world.getHandle().getDayTime();
    }

    public void setFullTime(long time) {
        world.getHandle().setDayTime(time);

        // Forces the client to update to the new time immediately
        for (Player p : getPlayers()) {
            CraftPlayer cp = (CraftPlayer) p;
            if (cp.getHandle().playerConnection == null) continue;

            cp.getHandle().playerConnection.sendPacket(new PacketPlayOutUpdateTime(cp.getHandle().world.getTime(), cp.getHandle().getPlayerTime(), cp.getHandle().world.getGameRules().getBoolean("doDaylightCycle")));
        }
    }

    public boolean createExplosion(double x, double y, double z, float power) {
        return createExplosion(x, y, z, power, false, true);
    }

    public boolean createExplosion(double x, double y, double z, float power, boolean setFire) {
        return createExplosion(x, y, z, power, setFire, true);
    }

    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return !world.getHandle().createExplosion(null, x, y, z, power, setFire, breakBlocks).wasCanceled;
    }

    public boolean createExplosion(Location loc, float power) {
        return createExplosion(loc, power, false);
    }

    public boolean createExplosion(Location loc, float power, boolean setFire) {
        return createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire);
    }

    public Environment getEnvironment() {
        return world.getEnvironment();
    }

    public void setEnvironment(Environment env) {
        world.setEnvironment(env);
    }

    public Block getBlockAt(Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public int getBlockTypeIdAt(Location location) {
        return getBlockTypeIdAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public int getHighestBlockYAt(Location location) {
        return getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
    }

    public Chunk getChunkAt(Location location) {
        return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public ChunkGenerator getGenerator() {
        return world.getGenerator();
    }

    public List<BlockPopulator> getPopulators() {
        return world.getPopulators();
    }

    public Block getHighestBlockAt(int x, int z) {
        return getBlockAt(x, getHighestBlockYAt(x, z), z);
    }

    public Block getHighestBlockAt(Location location) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
    }

    public Biome getBiome(int x, int z) {
        return CraftBlock.biomeBaseToBiome(this.world.getHandle().getBiome(new BlockPosition(x, 0, z)));
    }

    public void setBiome(int x, int z, Biome bio) {
        world.setBiome(x, z, bio);
    }

    public double getTemperature(int x, int z) {
        return this.world.getHandle().getBiome(new BlockPosition(x, 0, z)).getTemperature();
    }

    public double getHumidity(int x, int z) {
        return this.world.getHandle().getBiome(new BlockPosition(x, 0, z)).getHumidity();
    }

    public List<Entity> getEntities() {
        return world.getEntities();
    }

    public List<LivingEntity> getLivingEntities() {
        return world.getLivingEntities();
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
        return (Collection<T>)getEntitiesByClasses(classes);
    }

    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> clazz) {
        return world.getEntitiesByClass(clazz);
    }

    public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
        return world.getEntitiesByClasses(classes);
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        return world.getNearbyEntities(location, x, y, z);
    }

    public List<Player> getPlayers() {
        return world.getPlayers();
    }

    public void save() {
        world.save();
    }

    public boolean isAutoSave() {
        return world.isAutoSave();
    }

    public void setAutoSave(boolean value) {
        world.setAutoSave(value);
    }

    public void setDifficulty(Difficulty difficulty) {
        world.getHandle().worldData.setDifficulty(EnumDifficulty.getById(difficulty.getValue()));
    }

    public Difficulty getDifficulty() {
        return Difficulty.getByValue(world.getHandle().getDifficulty().ordinal());
    }

    public BlockMetadataStore getBlockMetadata() {
        return world.getBlockMetadata();
    }

    public boolean hasStorm() {
        return world.getHandle().worldData.hasStorm();
    }

    public void setStorm(boolean hasStorm) {
        world.getHandle().worldData.setStorm(hasStorm);
    }

    public int getWeatherDuration() {
        return world.getHandle().worldData.getWeatherDuration();
    }

    public void setWeatherDuration(int duration) {
        world.getHandle().worldData.setWeatherDuration(duration);
    }

    public boolean isThundering() {
        return world.getHandle().worldData.isThundering();
    }

    public void setThundering(boolean thundering) {
        world.getHandle().worldData.setThundering(thundering);
    }

    public int getThunderDuration() {
        return world.getHandle().worldData.getThunderDuration();
    }

    public void setThunderDuration(int duration) {
        world.getHandle().worldData.setThunderDuration(duration);
    }

    public long getSeed() {
        return world.getHandle().worldData.getSeed();
    }

    public boolean getPVP() {
        return world.getHandle().pvpMode;
    }

    public void setPVP(boolean pvp) {
        world.getHandle().pvpMode = pvp;
    }

    public void playEffect(Player player, Effect effect, int data) {
        playEffect(player.getLocation(), effect, data, 0);
    }

    public void playEffect(Location location, Effect effect, int data) {
        playEffect(location, effect, data, 64);
    }

    public <T> void playEffect(Location loc, Effect effect, T data) {
        playEffect(loc, effect, data, 64);
    }

    public <T> void playEffect(Location loc, Effect effect, T data, int radius) {
        if (data != null) {
            Validate.isTrue(data.getClass().isAssignableFrom(effect.getData()), "Wrong kind of data for this effect!");
        } else {
            Validate.isTrue(effect.getData() == null, "Wrong kind of data for this effect!");
        }

        int datavalue = data == null ? 0 : CraftEffect.getDataValue(effect, data);
        playEffect(loc, effect, datavalue, radius);
    }

    public void playEffect(Location location, Effect effect, int data, int radius) {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(effect, "Effect cannot be null");
        Validate.notNull(location.getWorld(), "World cannot be null");
        int packetData = effect.getId();
        PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(packetData, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data, false);
        int distance;
        radius *= radius;

        for (Player player : getPlayers()) {
            if (((CraftPlayer) player).getHandle().playerConnection == null) continue;
            if (!location.getWorld().equals(player.getWorld())) continue;

            distance = (int) player.getLocation().distanceSquared(location);
            if (distance <= radius) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
        return spawn(location, clazz, SpawnReason.CUSTOM);
    }

    public FallingBlock spawnFallingBlock(Location location, int blockId, byte blockData) throws IllegalArgumentException {
        return spawnFallingBlock(location, org.bukkit.Material.getMaterial(blockId), blockData);
    }



    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(net.minecraft.server.v1_9_R1.Entity entity, SpawnReason reason) throws IllegalArgumentException {
        Preconditions.checkArgument(entity != null, "Cannot spawn null entity");

        if (entity instanceof EntityInsentient) {
            ((EntityInsentient) entity).prepare(getHandle().D(new BlockPosition(entity)), null);
        }

        world.getHandle().addEntity(entity, reason);
        return (T) entity.getBukkitEntity();
    }

    public <T extends Entity> T spawn(Location location, Class<T> clazz, SpawnReason reason) throws IllegalArgumentException {
        net.minecraft.server.v1_9_R1.Entity entity = createEntity(location, clazz);

        return addEntity(entity, reason);
    }

    public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) {
        return world.getEmptyChunkSnapshot(x, z, includeBiome, includeBiomeTempRain);
    }

    public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
        world.setSpawnFlags(allowMonsters, allowAnimals);
    }

    public boolean getAllowAnimals() {
        return world.getAllowAnimals();
    }

    public boolean getAllowMonsters() {
        return world.getAllowMonsters();
    }

    public int getMaxHeight() {
        return world.getMaxHeight();
    }

    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    public boolean getKeepSpawnInMemory() {
        return world.getKeepSpawnInMemory();
    }

    public void setKeepSpawnInMemory(boolean keepLoaded) {
        world.setKeepSpawnInMemory(keepLoaded);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return world.equals(obj);
    }

    public File getWorldFolder() {
        return world.getWorldFolder();
    }

    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        world.sendPluginMessage(source, channel, message);
    }

    public Set<String> getListeningPluginChannels() {
        return world.getListeningPluginChannels();
    }

    public org.bukkit.WorldType getWorldType() {
        return world.getWorldType();
    }

    public boolean canGenerateStructures() {
        return world.canGenerateStructures();
    }

    public long getTicksPerAnimalSpawns() {
        return world.getTicksPerAnimalSpawns();
    }

    public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {
        world.setTicksPerAnimalSpawns(ticksPerAnimalSpawns);
    }

    public long getTicksPerMonsterSpawns() {
        return world.getTicksPerMonsterSpawns();
    }

    public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {
        world.setTicksPerMonsterSpawns(ticksPerMonsterSpawns);
    }

    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        world.setMetadata(metadataKey, newMetadataValue);
    }

    public List<MetadataValue> getMetadata(String metadataKey) {
        return world.getMetadata(metadataKey);
    }

    public boolean hasMetadata(String metadataKey) {
        return world.hasMetadata(metadataKey);
    }

    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        world.removeMetadata(metadataKey, owningPlugin);
    }

    public int getMonsterSpawnLimit() {
        return world.getMonsterSpawnLimit();
    }

    public void setMonsterSpawnLimit(int limit) {
        world.setMonsterSpawnLimit(limit);
    }

    public int getAnimalSpawnLimit() {
        return world.getAnimalSpawnLimit();
    }

    public void setAnimalSpawnLimit(int limit) {
        world.setAnimalSpawnLimit(limit);
    }

    public int getWaterAnimalSpawnLimit() {
        return world.getWaterAnimalSpawnLimit();
    }

    public void setWaterAnimalSpawnLimit(int limit) {
        world.setWaterAnimalSpawnLimit(limit);
    }

    public int getAmbientSpawnLimit() {
        return world.getAmbientSpawnLimit();
    }

    public void setAmbientSpawnLimit(int limit) {
        world.setAmbientSpawnLimit(limit);
    }


    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        world.playSound(loc, sound, volume, pitch);
    }

    public String getGameRuleValue(String rule) {
        return world.getGameRuleValue(rule);
    }

    public boolean setGameRuleValue(String rule, String value) {
        return world.setGameRuleValue(rule, value);
    }

    public String[] getGameRules() {
        return world.getGameRules();
    }

    public boolean isGameRule(String rule) {
        return world.isGameRule(rule);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return world.getWorldBorder();
    }

    public void processChunkGC() {
        world.processChunkGC();
    }
}

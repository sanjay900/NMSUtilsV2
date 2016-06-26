package net.tangentmc.nmsUtils.v1_9_R1.entities;

import lombok.Getter;
import lombok.ToString;
import net.minecraft.server.v1_9_R1.*;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.HologramFactory.HeadItem;
import net.tangentmc.nmsUtils.entities.HologramFactory.HologramObject;
import net.tangentmc.nmsUtils.entities.HologramFactory.HologramType;
import net.tangentmc.nmsUtils.entities.NMSArmorStand;
import net.tangentmc.nmsUtils.utils.AnimatedMessage;
import net.tangentmc.nmsUtils.v1_9_R1.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.*;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class CraftHologramEntity extends CraftEntity {
    public CraftHologramEntity(HologramEntity entity) {
        super((CraftServer) Bukkit.getServer(), entity);
    }

    public boolean teleport(Location loc, TeleportCause cause) {
        return ((HologramEntity)entity).teleport(loc);
    }

    public EntityType getType() {
        return EntityType.COMPLEX_PART;
    }

    public List<org.bukkit.entity.Entity> getLines() {
        return ((HologramEntity)entity).getLines();
    }

    public void remove() {
        ((HologramEntity)entity).remove();
        super.remove();
    }

    public void setLines(HologramObject... lines) {
        ((HologramEntity)entity).setLines(lines);
    }

    public void setLine(int i, String line) {
        ((HologramEntity)entity).setLine(i, new HologramObject(HologramType.TEXT,line),0);
    }

    public void addLine(String line) {
        ((HologramEntity)entity).addLine(new HologramObject(HologramType.TEXT,line));
    }

    public void addItem(ItemStack stack) {
        ((HologramEntity)entity).addLine(new HologramObject(HologramType.ITEM,stack));
    }

    public void addBlock(ItemStack stack) {
        ((HologramEntity)entity).addLine(new HologramObject(HologramType.BLOCK,stack));
    }

    public void removeLine(int idx) {
        ((HologramEntity)entity).removeLine(idx);
    }
    public static class HologramEntity extends net.minecraft.server.v1_9_R1.Entity {
        ArrayList<HologramPart> lines = new ArrayList<>();
        ArrayList<AnimatedMessage> animations = new ArrayList<>();
        @Override
        public void m() {
            Collideable.testCollision(this);
            for (AnimatedMessage message: animations) {
                int line = message.getDelay();
                if (line == 0) line = 1;
                if (ticksLived ==0 || ticksLived % line==0) {
                    int idx = animations.indexOf(message);
                    message.next();
                    lines.stream().filter(t -> t.getPartType() == HologramType.TEXT).map(m -> (TextHologramEntity)m).forEach(m -> m.updateText(idx));
                }
            }

        }
        public boolean teleport(Location loc) {
            this.location = loc;
            this.currentY = 0;
            for (HologramPart part : lines) {
                ((Entity)part).setPosition(loc.getX(), loc.getY()+currentY, loc.getZ());
                currentY-=part.getHeight();
            }
            fixBoundingBox();
            return true;
        }

        private void fixBoundingBox() {
            double minX = Double.MAX_VALUE,
                    minY= Double.MAX_VALUE,
                    minZ= Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY= -Double.MAX_VALUE, maxZ= -Double.MAX_VALUE;
            for (HologramPart line: lines) {
                boolean isItem = line.getPartType() == HologramType.ITEM;
                boolean isBlock = line.getPartType() == HologramType.BLOCK;
                boolean isHead = line.getPartType() == HologramType.HEAD;
                Location l = line.getBukkitEntity().getLocation();
                if (l.getX()-0.5< minX) minX = l.getX()-0.5;
                if (l.getZ()-0.5< minZ) minZ = l.getZ()-0.5;
                if (l.getX()+0.5> maxX) maxX = l.getX()+0.5;
                if (l.getZ()+0.5> maxZ) maxZ = l.getZ()+0.5;
                if (isBlock) {
                    if (l.getY()< minY) minY = l.getY();
                    if (l.getY()+1> maxY) maxY = l.getY()+1;
                } else if (isItem){
                    if (l.getY()< minY) minY = l.getY();
                    if (l.getY()+0.6> maxY) maxY = l.getY()+0.6;
                } else if (isHead) {
                    //Armorstand placement.
                    if (l.getY()+1< minY) minY = l.getY()+1;
                    if (l.getY()+1+line.getHeight()> maxY) maxY = l.getY()+1+line.getHeight();
                } else {
                    if (l.getY()< minY) minY = l.getY();
                    if (l.getY()+0.23> maxY) maxY = l.getY()+0.23;
                }

            }
            this.a(new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ));
        }

        @Getter
        Location location;
        public void remove() {
            if (this.dead) return;
            lines.stream().map(en -> (Entity)en).forEach(en -> {
                en.passengers.forEach(en4 -> en4.getBukkitEntity().remove());
                en.getBukkitEntity().remove();
            });
            lines.clear();
        }
        public HologramEntity(Location loc, ArrayList<HologramObject> lines) {
            super(((CraftWorld)loc.getWorld()).getHandle());
            location = loc;
            lines.forEach(this::addLine);
            NMSUtilImpl.addEntityToWorld((WorldServer)world,this);
        }

        public List<org.bukkit.entity.Entity> getLines() {
            return lines.stream().map(HologramPart::getBukkitEntity).collect(Collectors.toList());
        }

        public void addLine(HologramObject line) {
            if (this.dead) return;
            if (line.getObject() instanceof AnimatedMessage) {
                AnimatedMessage message = (AnimatedMessage) line.getObject();
                animations.add(message);
                for (int i = 0;i<message.current().getLines().length;i++) {
                    setLine(lines.size(),new HologramObject(HologramType.TEXT,animations.indexOf(message)),i);
                }
                return;
            }
            setLine(lines.size(),line,0);
        }

        public void removeLine(int idx) {
            if (this.dead) return;
            if (!(lines.size() > idx)) return;
            lines.get(idx).getBukkitEntity().remove();
            lines.remove(idx);
        }
        @Override
        public CraftHologramEntity getBukkitEntity() {
            return new CraftHologramEntity(this);
        }
        public void setLines(HologramObject[] lines) {
            if (this.dead) return;
            boolean same = this.lines.size() == lines.length;
            if (same) {
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].getType() != this.lines.get(i).getPartType()) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    for (int i = 0; i < lines.length; i++) {
                        this.lines.get(i).set(lines[i]);
                    }
                }
            } else {
                this.currentY = 0;
                this.lines.forEach(HologramPart::remove);
                this.lines.clear();
                Arrays.stream(lines).forEach(this::addLine);
            }
        }

        double currentY = 0;
        public void setLine(int i, HologramObject line, int iline) {
            boolean isItem = line.getType() == HologramType.ITEM;
            boolean isBlock = line.getType() == HologramType.BLOCK;
            boolean isHead = line.getType() == HologramType.HEAD;
            HologramPart part;
            this.removeLine(i);
            if (isItem || isBlock) {
                ItemStack it = (ItemStack) line.getObject();
                if (isBlock) {
                    currentY-=1;
                    part = new BlockHologramEntity(world, location.clone().add(0, currentY, 0), it, this);
                } else {
                    currentY-=0.6;
                    part = new ItemHologramEntity(world, location.clone().add(0, currentY, 0), it, this);

                }
            } else if (isHead) {
                HeadItem hi = (HeadItem) line.getObject();
                currentY-=hi.getHeight();
                part = new HeadHologramEntity(world, location.clone().add(0, currentY, 0), hi.getStack(), hi.getHeight(), this);
            } else {
                currentY-=0.23;
                part = new TextHologramEntity(world, location.clone().add(0, currentY-0.3, 0),line.getObject(),iline, this);

            }
            this.lines.add(part);
            fixBoundingBox();
        }
        @Override
        public void a(NBTTagCompound nbttagcompound) {}
        @Override
        public void b(NBTTagCompound nbttagcompound) {}
        @Override
        public boolean c(NBTTagCompound nbttagcompound) {
            return false;
        }
        @Override
        public boolean d(NBTTagCompound nbttagcompound) {
            return false;
        }
        @Override
        public void e(NBTTagCompound nbttagcompound) {}
        @Override
        protected void i() {}
        public void spawnAll() {
            lines.forEach(HologramPart::addToWorld);
        }

    }

    public static class CraftHologramPart extends org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity {
        private HologramEntity parent;

        interface HologramPart {
            void addToWorld();
            void set(HologramObject object);
            default void remove() {
                getParent().remove();
            }
            CraftHologramEntity getParent();
            EntityType getType();
            HologramType getPartType();
            CraftHologramPart getBukkitEntity();
            double getHeight();
        }
        public CraftHologramPart(Entity entity, HologramEntity parent) {
            super((CraftServer)Bukkit.getServer(), entity);
            this.parent = parent;
        }
        //Do all metadata operations to the parent
        @Override
        public boolean hasMetadata(String metadata) {
            return parent.getBukkitEntity().hasMetadata(metadata);
        }
        @Override
        public List<MetadataValue> getMetadata(String metadata) {
            return parent.getBukkitEntity().getMetadata(metadata);
        }
        @Override
        public void setMetadata(String metadata, MetadataValue value) {
            parent.getBukkitEntity().setMetadata(metadata, value);
        }
        @Override
        public void removeMetadata(String metadata,Plugin plugin) {
            parent.getBukkitEntity().removeMetadata(metadata,plugin);
        }

        @Override
        public EntityType getType() {
            return EntityType.COMPLEX_PART;
        }
        public static class TextHologramEntity extends EntityArmorStand implements HologramPart {
            HologramEntity parent;
            int message;
            int line;
            public TextHologramEntity(World world, Location loc, Object object, int line, HologramEntity parent) {
                super(world);
                this.line = line;
                this.parent = parent;
                this.getBukkitEntity().setMetadata("instrumented",new FixedMetadataValue(NMSUtils.getInstance(),true));
                if (object instanceof String) {
                    this.setCustomName((String)object);
                    this.setCustomNameVisible(true);
                } else {
                    message = (int) object;
                    this.setCustomNameVisible(true);
                    this.setCustomName(parent.animations.get(message).current().getLines()[line]);
                }
                this.setInvisible(true);
                this.setGravity(false);
                this.setPosition(loc.getX(), loc.getY(), loc.getZ());
                this.setSmall(true);
                this.setMarker(true);
                this.addToWorld();

            }
            public void updateText(int idx) {
                if (this.message != idx) return;
                String curr = parent.animations.get(message).current().getLines()[line];
                if (!this.getCustomName().equals(curr))
                    this.setCustomName(curr);

            }
            @Override
            public void m() {
                if (this.hasGravity()) super.m();
            }
            @Override
            public void set(HologramObject obj) {
                this.setCustomName((String) obj.getObject());
            }

            @Override
            public void remove() {
                getBukkitEntity().remove();

            }

            @Override
            public CraftHologramEntity getParent() {
                return parent.getBukkitEntity();
            }

            @Override
            public EntityType getType() {
                return EntityType.ARMOR_STAND;
            }

            @Override
            public HologramType getPartType() {
                return HologramType.TEXT;
            }

            @Override
            public CraftHologramPart getBukkitEntity() {
                return new CraftHologramPart(this,parent);
            }
            @Override
            public void a(NBTTagCompound nbttagcompound) {}
            @Override
            public void b(NBTTagCompound nbttagcompound) {}
            @Override
            public boolean c(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public boolean d(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public void e(NBTTagCompound nbttagcompound) {}
            @Override
            public double getHeight() {
                return 1;
            }
            @Override
            public void addToWorld() {

                NMSUtilImpl.addEntityToWorld((WorldServer)world,this);
            }
        }

        public static class ItemHologramEntity extends EntityArmorStand implements HologramPart {
            HologramEntity parent;
            HologramItem item;
            public ItemHologramEntity(World world, Location loc, ItemStack stack, HologramEntity parent) {
                super(world);
                this.parent = parent;
                this.getBukkitEntity().setMetadata("instrumented",new FixedMetadataValue(NMSUtils.getInstance(),true));
                this.setPosition(loc.getX(), loc.getY(), loc.getZ());
                this.setGravity(false);
                this.setInvisible(true);
                item = new HologramItem(world,loc,stack,this);
                item.ticksLived = 1;
                item.pickupDelay = 10;

                this.setSmall(true);
                this.setMarker(true);
                this.addToWorld();
            }

            @Override
            public void set(HologramObject obj) {
                item.getBukkitEntity().remove();
                item = new HologramItem(world,this.getBukkitEntity().getLocation(),(ItemStack) obj.getObject(),this);
                item.ticksLived = 1;
                item.pickupDelay = 10;
                this.getBukkitEntity().setPassenger(item.getBukkitEntity());
                NMSUtilImpl.addEntityToWorld((WorldServer)world,item);
            }

            @Override
            public void remove() {
                getBukkitEntity().remove();
                item.getBukkitEntity().remove();
            }

            @Override
            public CraftHologramEntity getParent() {
                return parent.getBukkitEntity();
            }

            @Override
            public EntityType getType() {
                return EntityType.ARMOR_STAND;
            }

            @Override
            public HologramType getPartType() {
                return HologramType.ITEM;
            }

            @Override
            public CraftHologramPart getBukkitEntity() {
                return new CraftHologramPart(this,parent);
            }
            @Override
            public void m() {
                if (!this.dead)
                    this.startRiding(parent);
            }
            @Override
            public void a(NBTTagCompound nbttagcompound) {}
            @Override
            public void b(NBTTagCompound nbttagcompound) {}
            @Override
            public boolean c(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public boolean d(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public void e(NBTTagCompound nbttagcompound) {}
            public static class HologramItem extends EntityItem {
                public ItemHologramEntity parent;
                public HologramItem(World world, Location loc, ItemStack it, ItemHologramEntity parent) {
                    super(world,loc.getX(),loc.getY()-1,loc.getZ(), CraftItemStack.asNMSCopy(it));
                    this.parent = parent;
                }
                //Disable pickup.
                @Override
                public void d(EntityHuman entityhuman) {

                }
                //Disable merge!
                @Override
                public void m(){};

                @Override
                public void a(NBTTagCompound nbttagcompound) {}
                @Override
                public void b(NBTTagCompound nbttagcompound) {}
                @Override
                public boolean c(NBTTagCompound nbttagcompound) {
                    return false;
                }
                @Override
                public boolean d(NBTTagCompound nbttagcompound) {
                    return false;
                }
                @Override
                public void e(NBTTagCompound nbttagcompound) {}
            }
            @Override
            public double getHeight() {
                return 0.7;
            }

            @Override
            public void addToWorld() {
                NMSUtilImpl.addEntityToWorld((WorldServer)world,this);
                NMSUtilImpl.addEntityToWorld((WorldServer)world,item);
            }
        }
        public static class BlockHologramEntity extends EntityArmorStand implements HologramPart {
            HologramEntity parent;
            HologramBlock block;
            boolean teleporting;
            public BlockHologramEntity(World world, Location loc, ItemStack stack, HologramEntity parent) {
                super(world);
                this.parent = parent;
                this.getBukkitEntity().setMetadata("instrumented",new FixedMetadataValue(NMSUtils.getInstance(),true));
                this.setPosition(loc.getX(), loc.getY(), loc.getZ());
                this.setGravity(false);
                this.setInvisible(true);
                this.setSmall(true);
                this.setMarker(true);
                block = new HologramBlock(world,loc,stack,this);
                block.ticksLived = 20;
                this.addToWorld();
            }

            @Override
            public void set(HologramObject obj) {
                block.getBukkitEntity().remove();
                block = new HologramBlock(world,this.getBukkitEntity().getLocation(),(ItemStack) obj.getObject(),this);
                block.ticksLived = 1;
                this.getBukkitEntity().setPassenger(block.getBukkitEntity());
                NMSUtilImpl.addEntityToWorld((WorldServer)world,block);
            }

            @Override
            public void remove() {
                getBukkitEntity().remove();
                block.getBukkitEntity().remove();
            }

            @Override
            public CraftHologramEntity getParent() {
                return parent.getBukkitEntity();
            }

            @Override
            public EntityType getType() {
                return EntityType.ARMOR_STAND;
            }

            @Override
            public HologramType getPartType() {
                return HologramType.BLOCK;
            }

            @Override
            public CraftHologramPart getBukkitEntity() {
                return new CraftHologramPart(this,parent);
            }
            @Override
            public void m() {
                if (this.hasGravity()) super.m();

                if (!this.dead && !parent.dead) {
                    block.ticksLived = 1;
                }
            }
            @Override
            public void a(NBTTagCompound nbttagcompound) {}
            @Override
            public void b(NBTTagCompound nbttagcompound) {}
            @Override
            public boolean c(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public boolean d(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public void e(NBTTagCompound nbttagcompound) {}
            public static class HologramBlock extends EntityFallingBlock {
                public BlockHologramEntity parent;
                public HologramBlock(World world, Location loc, ItemStack it, BlockHologramEntity parent) {
                    super(world,loc.getX(),loc.getY(),loc.getZ(),net.minecraft.server.v1_9_R1.Block.getById(it.getType().getId()).fromLegacyData(it.getData().getData()));
                    this.parent = parent;
                }
                @Override
                public void m() {
                    if (!dead)
                    this.startRiding(parent);
                }
                @Override
                public void a(NBTTagCompound nbttagcompound) {}
                @Override
                public void b(NBTTagCompound nbttagcompound) {}
                @Override
                public boolean c(NBTTagCompound nbttagcompound) {
                    return false;
                }
                @Override
                public boolean d(NBTTagCompound nbttagcompound) {
                    return false;
                }
                @Override
                public void e(NBTTagCompound nbttagcompound) {}
            }
            @Override
            public double getHeight() {
                return 1;
            }

            @Override
            public void addToWorld() {
                NMSUtilImpl.addEntityToWorld((WorldServer)world,this);
                NMSUtilImpl.addEntityToWorld((WorldServer)world,block);
            }

        }
        @ToString
        public static class HeadHologramEntity extends EntityArmorStand implements HologramPart {
            HologramEntity parent;
            @Getter
            double height = 1;
            public HeadHologramEntity(World world, Location loc, ItemStack stack, double height, HologramEntity parent) {
                super(world);
                this.height = height;
                this.parent = parent;
                this.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(),loc.getYaw(),loc.getPitch());
                CraftArmorStand en =  new CraftArmorStand(world.getServer(),this);
                en.setHelmet(stack);
                NMSArmorStand ns = NMSArmorStand.wrap(en);
                ns.lock();
                this.setGravity(false);
                this.setInvisible(true);
                this.setSmall(true);
                this.setSize((float)height, (float)height);
                this.addToWorld();

            }

            @Override
            public void set(HologramObject obj) {
                HeadItem hi = (HeadItem) obj.getObject();
                this.height = hi.getHeight();
                CraftArmorStand en =  new CraftArmorStand(world.getServer(),this);
                NMSArmorStand ns = NMSArmorStand.wrap(en);
                ns.unlock();
                en.setHelmet(hi.getStack());
                ns.lock();
            }

            @Override
            public void remove() {
                getBukkitEntity().remove();
            }

            @Override
            public CraftHologramEntity getParent() {
                return parent.getBukkitEntity();
            }

            @Override
            public EntityType getType() {
                return EntityType.ARMOR_STAND;
            }

            @Override
            public HologramType getPartType() {
                return HologramType.HEAD;
            }

            @Override
            public CraftHologramPart getBukkitEntity() {
                return new CraftHologramPart(this,parent);
            }
            @Override
            public void m() {}
            @Override
            public void a(NBTTagCompound nbttagcompound) {}
            @Override
            public void b(NBTTagCompound nbttagcompound) {}
            @Override
            public boolean c(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public boolean d(NBTTagCompound nbttagcompound) {
                return false;
            }
            @Override
            public void e(NBTTagCompound nbttagcompound) {}

            @Override
            public void addToWorld() {

                NMSUtilImpl.addEntityToWorld((WorldServer)world,this);
            }
        }

    }
}

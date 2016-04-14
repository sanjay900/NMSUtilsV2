package net.tangentmc.nmsUtils.v1_9_R1.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.ToString;
import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.EntityDamageSource;
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.MovingObjectPosition;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.Vec3D;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.entities.HologramFactory.HeadItem;
import net.tangentmc.nmsUtils.entities.HologramFactory.HologramObject;
import net.tangentmc.nmsUtils.entities.HologramFactory.HologramType;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.entities.NMSHologram;
import net.tangentmc.nmsUtils.utils.AnimatedMessage;
import net.tangentmc.nmsUtils.v1_9_R1.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.BlockHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.HeadHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.HologramPart;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.ItemHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.TextHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftArmorStandEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftArmorStandEntity.ArmorStandEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftFallingBlockEntity.FallingBlockEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftItemEntity.ItemEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSlimeEntity.SlimeEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;
public class CraftHologramEntity extends CraftEntity implements NMSHologram {

	public CraftHologramEntity(HologramEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	@Override
	public boolean teleport(Location loc, TeleportCause cause) {
		return ((HologramEntity)entity).teleport(loc);
	}
	@Override
	public EntityType getType() {
		return EntityType.COMPLEX_PART;
	}
	@Override
	public List<org.bukkit.entity.Entity> getLines() {
		return ((HologramEntity)entity).getLines();
	}
	@Override
	public void remove() {
		((HologramEntity)entity).remove();
		super.remove();
	}
	@Override
	public void setLines(HologramObject... lines) {
		((HologramEntity)entity).setLines(lines);
	}
	@Override
	public void setLine(int i, String line) {
		((HologramEntity)entity).setLine(i, new HologramObject(HologramType.TEXT,line),0);
	}
	@Override
	public void addLine(String line) {
		((HologramEntity)entity).addLine(new HologramObject(HologramType.TEXT,line));
	}
	@Override
	public void addItem(ItemStack stack) {
		((HologramEntity)entity).addLine(new HologramObject(HologramType.ITEM,stack));
	}
	@Override
	public void addBlock(ItemStack stack) {
		((HologramEntity)entity).addLine(new HologramObject(HologramType.BLOCK,stack));
	}
	@Override
	public void removeLine(int idx) {
		((HologramEntity)entity).removeLine(idx);
	}
	public static class HologramEntity extends net.minecraft.server.v1_9_R1.Entity {
		ArrayList<HologramPart> lines = new ArrayList<>();
		ArrayList<AnimatedMessage> animations = new ArrayList<>();
		@Override
		public void m() {
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
			this.currentY = 0;
			for (HologramPart part : lines) {
				((Entity)part).setPosition(loc.getX(), currentY, loc.getZ());
				currentY-=part.getHeight();
			}
			return true;
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
			NMSUtilImpl.addEntityToWorld(world, this);
		}

		public List<org.bukkit.entity.Entity> getLines() {
			return lines.stream().map(en -> en.getBukkitEntity()).collect(Collectors.toList());
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
			if (lines.toArray(new HologramPart[0])[idx] !=null)
				lines.toArray(new HologramPart[0])[idx].getBukkitEntity().remove();
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
				this.lines.forEach(en -> en.remove());
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
		public Entity[] getAll() {
			List<Entity> wontCollide = new ArrayList<Entity>();
			lines.forEach(w->{
				wontCollide.add((Entity) w);
				wontCollide.addAll(((Entity) w).passengers);
				if (((Entity)w).getVehicle() != null)
				wontCollide.add(((Entity) w).getVehicle());
			});
			return wontCollide.toArray(new Entity[0]);
		}
	}

	public static class CraftHologramPart extends CraftEntity implements NMSEntity{
		public interface HologramPart {
			void addToWorld();
			void set(HologramObject object);
			default void remove() {
				getParent().remove();
			}
			void setFrozen(boolean b);
			CraftHologramEntity getParent();
			EntityType getType();
			HologramType getPartType();
			CraftHologramPart getBukkitEntity();
			double getHeight();
		}
		public CraftHologramPart(Entity entity) {
			super((CraftServer)Bukkit.getServer(), entity);
		}


		@Override
		public EntityType getType() {
			return EntityType.COMPLEX_PART;
		}
		public static class TextHologramEntity extends EntityArmorStand implements HologramPart, Collideable{
			HologramEntity parent;
			int message;
			int line;
			public TextHologramEntity(World world, Location loc, Object object, int line, HologramEntity parent) {
				super(world);
				this.line = line;
				this.parent = parent;
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
				this.testCollision(parent.getAll());
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
				return new CraftHologramPart(this);
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
				NMSUtilImpl.addEntityToWorld(world, this);
			}
			@Override
			public void setFrozen(boolean b) {
				this.setGravity(!b);
			}
		}

		public static class ItemHologramEntity extends ArmorStandEntity implements HologramPart {
			HologramEntity parent;
			HologramItem item;
			boolean teleporting = false;
			public ItemHologramEntity(World world, Location loc, ItemStack stack, HologramEntity parent) {
				super(world);
				this.parent = parent;
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
				NMSUtilImpl.addEntityToWorld(world, item);
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
				return new CraftHologramPart(this);
			}
			@Override
			public void m() {
				if (!this.dead)
					this.getBukkitEntity().setPassenger(item.getBukkitEntity());
				if (this.hasGravity()) super.m();
				this.testCollision(parent.getAll());
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
			public static class HologramItem extends ItemEntity {
				public ItemHologramEntity parent;
				public HologramItem(World world, Location loc, ItemStack it, ItemHologramEntity parent) {
					super(world,loc.add(0, -1, 0),it);
					this.parent = parent;
				}
				//Disable pickup.
				@Override
				public void d(EntityHuman entityhuman) {

				}
				//Disable merge - but still collide!
				@Override
				public void m() {
					this.testCollision(parent);
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
				return 0.7;
			}

			@Override
			public void addToWorld() {
				NMSUtilImpl.addEntityToWorld(world, this);
				NMSUtilImpl.addEntityToWorld(world, item);
			}

			@Override
			public void setFrozen(boolean b) {
				this.setGravity(!b);
			}
		}
		public static class BlockHologramEntity extends ArmorStandEntity implements HologramPart {
			HologramEntity parent;
			HologramBlock block;
			boolean teleporting;
			public BlockHologramEntity(World world, Location loc, ItemStack stack, HologramEntity parent) {
				super(world);
				this.parent = parent;
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
				NMSUtilImpl.addEntityToWorld(world, block);
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
				return new CraftHologramPart(this);
			}
			@Override
			public void m() {
				if (this.hasGravity()) super.m();
				
				if (!this.dead) {
					this.getBukkitEntity().setPassenger(block.getBukkitEntity());
					block.ticksLived = 1;
					block.a(new EntityBoundingBox(this.locX-0.5, this.locY-0.5, this.locZ-0.5, this.locX+0.5, this.locY+0.5, this.locZ+0.5));
					if (!this.hasGravity())
					this.testCollision(parent.getAll());
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
			public static class HologramBlock extends FallingBlockEntity {
				public BlockHologramEntity parent;
				public HologramBlock(World world, Location loc, ItemStack it, BlockHologramEntity parent) {
					super(world,loc,it);
					this.parent = parent;
				}
				@Override
				public void m() {
					this.testCollision(parent.parent.getAll());
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
				NMSUtilImpl.addEntityToWorld(world, this);
				NMSUtilImpl.addEntityToWorld(world, block);
			}

			@Override
			public void setFrozen(boolean b) {
				this.setGravity(!b);
			}

		}
		@ToString
		public static class HeadHologramEntity extends ArmorStandEntity implements HologramPart, Collideable {
			HologramEntity parent;
			boolean teleporting;
			@Getter
			double height = 1;
			public HeadHologramEntity(World world, Location loc, ItemStack stack, double height, HologramEntity parent) {
				super(world);
				this.height = height;
				this.parent = parent;
				this.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(),loc.getYaw(),loc.getPitch());
				CraftArmorStandEntity en = new CraftArmorStandEntity(this);
				lock();
				en.setHelmet(stack);
				this.setGravity(false);
				this.setInvisible(true);
				this.setSmall(true);
				this.setSize((float)height, (float)height);
				this.a(new HeadBoundingBox(this));
				this.addToWorld();
			
			}

			@Override
			public void set(HologramObject obj) {
				HeadItem hi = (HeadItem) obj.getObject();
				this.height = hi.getHeight();
				CraftArmorStandEntity en =  new CraftArmorStandEntity(this);
				en.unlock();
				en.setHelmet(hi.getStack());
				en.lock();
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
				return new CraftHologramPart(this);
			}
			@Override
			public void m() {
				this.testCollision(parent.getAll());
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
			public void addToWorld() {
				NMSUtilImpl.addEntityToWorld(world, this);
			}

			@Override
			public void setFrozen(boolean b) {
				this.setGravity(!b);
			}
		}
		public static class TouchHologramEntity extends SlimeEntity implements HologramPart {
			boolean teleporting;
			HologramEntity parent;
			public TouchHologramEntity(World world, Location loc, HologramEntity parent) {
				super(world);
				this.parent = parent;
				this.setPosition(loc.getX(), loc.getY(), loc.getZ());
				this.a(new NullBoundingBox());
				this.setInvisible(true);
				NMSUtilImpl.addEntityToWorld(world, this);
				this.addToWorld();
			}

			@Override
			public EntityType getType() {
				return EntityType.SLIME;
			}
			@Override
			public boolean damageEntity(DamageSource damageSource, float amount) {
				if (damageSource instanceof EntityDamageSource) {
					EntityDamageSource entityDamageSource = (EntityDamageSource) damageSource;
					if (entityDamageSource.getEntity() instanceof EntityPlayer) {
						Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(((EntityPlayer) entityDamageSource.getEntity()).getBukkitEntity(), getBukkitEntity()));
					}
				}
				return false;
			}

			@Override
			public boolean isInvulnerable(DamageSource source) {
				/*
				 * The field Entity.invulnerable is private.
				 * It's only used while saving NBTTags, but since the entity would be killed
				 * on chunk unload, we prefer to override isInvulnerable().
				 */
				return true;
			}
			@Override
			public HologramType getPartType() {
				return HologramType.TouchPart;
			}
			@Override
			public CraftHologramPart getBukkitEntity() {
				return new CraftHologramPart(this);
			}

			@Override
			public void set(HologramObject text) {
				//Not used, we cant change anything about this entity
			}

			@Override
			public void remove() {
				this.getBukkitEntity().remove();
			}

			@Override
			public CraftHologramEntity getParent() {
				return parent.getBukkitEntity();
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
				return 0;
			}

			@Override
			public void addToWorld() {
				NMSUtilImpl.addEntityToWorld(world, this);
			}

			@Override
			public void setFrozen(boolean b) {
				super.setFrozen(b);
			}

		}
		//Don't do anything in craft
		@Override
		public void setFrozen(boolean b) {
			
		}


		@Override
		public boolean isFrozen() {
			return false;
		}


		@Override
		public void setWillSave(boolean b) {
			
		}


		@Override
		public boolean willSave() {
			return false;
		}


		@Override
		public void spawn() {
			
		}
	}
	public static class HeadBoundingBox extends AxisAlignedBB {

		public HeadBoundingBox(HeadHologramEntity en) {
			super(en.locX-(en.height/2), en.locY-(en.height/2), en.locZ-(en.height/2), en.locX+(en.height/2), en.locY+(en.height/2), en.locZ+(en.height/2));
		}

		@Override
		public double a() {
			return 0.0;
		}

		@Override
		public double a(AxisAlignedBB arg0, double arg1) {
			return 0.0;
		}

		@Override
		public AxisAlignedBB a(AxisAlignedBB arg0) {
			return this;
		}

		@Override
		public AxisAlignedBB a(double arg0, double arg1, double arg2) {
			return this;
		}

		@Override
		public MovingObjectPosition a(Vec3D arg0, Vec3D arg1) {
			return super.a(arg0, arg1);
		}

		@Override
		public boolean a(Vec3D arg0) {
			return false;
		}

		@Override
		public double b(AxisAlignedBB arg0, double arg1) {
			return 0.0;
		}

		@Override
		public boolean b(AxisAlignedBB arg0) {
			return false;
		}

		@Override
		public double c(AxisAlignedBB arg0, double arg1) {
			return 0.0;
		}

		@Override
		public AxisAlignedBB c(double arg0, double arg1, double arg2) {
			return this;
		}

		@Override
		public AxisAlignedBB grow(double arg0, double arg1, double arg2) {
			return this;
		}
	}
	public static class EntityBoundingBox extends AxisAlignedBB {

		public EntityBoundingBox(double arg0, double arg1, double arg2, double arg3, double arg4, double arg5) {
			super(arg0, arg1, arg2, arg3, arg4, arg5);
		}
		
		@Override
		public AxisAlignedBB a(AxisAlignedBB arg0) {
			return this;
		}

		@Override
		public AxisAlignedBB a(double arg0, double arg1, double arg2) {
			return this;
		}

		@Override
		public MovingObjectPosition a(Vec3D arg0, Vec3D arg1) {
			return super.a(arg0, arg1);
		}

		@Override
		public AxisAlignedBB c(double arg0, double arg1, double arg2) {
			return this;
		}

		@Override
		public AxisAlignedBB grow(double arg0, double arg1, double arg2) {
			return this;
		}
	}
	public static class NullBoundingBox extends AxisAlignedBB {

		public NullBoundingBox() {
			super(0, 0, 0, 0, 0, 0);
		}

		@Override
		public double a() {
			return 0.0;
		}

		@Override
		public double a(AxisAlignedBB arg0, double arg1) {
			return 0.0;
		}

		@Override
		public AxisAlignedBB a(AxisAlignedBB arg0) {
			return this;
		}

		@Override
		public AxisAlignedBB a(double arg0, double arg1, double arg2) {
			return this;
		}

		@Override
		public MovingObjectPosition a(Vec3D arg0, Vec3D arg1) {
			return super.a(arg0, arg1);
		}

		@Override
		public boolean a(Vec3D arg0) {
			return false;
		}

		@Override
		public double b(AxisAlignedBB arg0, double arg1) {
			return 0.0;
		}

		@Override
		public boolean b(AxisAlignedBB arg0) {
			return false;
		}

		@Override
		public double c(AxisAlignedBB arg0, double arg1) {
			return 0.0;
		}

		@Override
		public AxisAlignedBB c(double arg0, double arg1, double arg2) {
			return this;
		}

		@Override
		public AxisAlignedBB grow(double arg0, double arg1, double arg2) {
			return this;
		}
	}
	@Override
	public void spawn() {
		((HologramEntity)entity).spawnAll();
	}
	@Override
	public void setWillSave(boolean b ) {
		
	}
	@Override
	public boolean willSave() {
		return false;
	}
	@Override
	public void setFrozen(boolean b) {
		for (HologramPart p: ((HologramEntity)this.entity).lines) {
			p.setFrozen(b);
		}
	}
	@Override
	public boolean isFrozen() {
		return true;
	}
}

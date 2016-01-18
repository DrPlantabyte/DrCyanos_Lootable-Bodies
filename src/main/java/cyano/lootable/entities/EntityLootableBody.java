package cyano.lootable.entities;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mojang.authlib.GameProfile;

import cyano.lootable.LootableBodies;

public class EntityLootableBody extends net.minecraft.entity.EntityLiving implements IInventory{

	public static final int INVENTORY_SIZE = 9*6;
	public static int additionalItemDamage = 10;
	public static float corpseHP = 40;
	public static boolean hurtByFire = false;
	public static boolean hurtByBlast = false;
	public static boolean hurtByFall = false;
	public static boolean hurtByCactus = false;
	public static boolean hurtByWeapons = false;
	public static boolean hurtByBlockSuffocation = false;
	public static boolean hurtByAll = false;
	public static boolean hurtByOther = false;
	public static boolean invulnerable = false;
	
	final static byte VACUUM_TIMELIMIT = 20;
	final static int VACUUM_RADIUS = 3;
	
	final static int WATCHER_ID_OWNER = 28;
	
	private static final DamageSource selfDestruct = new DamageSource(EntityLootableBody.class.getSimpleName());

	
	protected final ItemStack[] equipment = new ItemStack[INVENTORY_SIZE];
	protected final java.util.Deque<ItemStack> auxInventory = new java.util.LinkedList<ItemStack>();
	private byte vacuumTime = 0;
	private GameProfile owner = null;
	private int shovelHits = 0;
	private static final int shovelHitLimit = 3;
	
	private long deathTimestamp = Long.MAX_VALUE;
	
	private boolean deadMode = false;
	
	public EntityLootableBody(World w) {
		super(w);
		this.setSize(0.85f, 0.75f);
		this.isImmuneToFire = (!hurtByFire) || invulnerable;
		vacuumTime = 0;
		this.getDataWatcher().addObject(WATCHER_ID_OWNER, "");
	}
	
	
	@Override
    protected void applyEntityAttributes() {
		// called during constructor of EntityLivingBase
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(corpseHP);
    }
	
	@Override
    protected void entityInit() {
		// called during constructor of Entity
        super.entityInit();
	}
	
	public float getRotation(){
		return this.rotationYaw;
	}
	
	public void setRotation(float newRot){
		this.rotationYaw = newRot;
		this.renderYawOffset = this.rotationYaw;
		this.prevRenderYawOffset = this.rotationYaw;
		this.prevRotationYaw = this.rotationYaw;
		this.newRotationYaw = this.rotationYaw;
		this.setRotationYawHead(this.rotationYaw);
	}
	
	
	@Override
    protected boolean canDespawn() {
        return false;
    }
	
	public void setOwner(GameProfile gp){
		owner = gp;
		updatePlayerProfile();
		if(gp.getName() != null){
			this.getDataWatcher().updateObject(WATCHER_ID_OWNER, gp.getName());
		} else {
			this.getDataWatcher().updateObject(WATCHER_ID_OWNER, "");
		}
	}
	
	public GameProfile getOwner(){
		if(owner == null){
			owner = getGameProfileFromName(this.getDataWatcher().getWatchableObjectString(WATCHER_ID_OWNER));
		} else if(this.getDataWatcher().getWatchableObjectString(WATCHER_ID_OWNER).isEmpty()){
			owner = null;
		}
		return owner;
	}
	
	private void updatePlayerProfile() {
        this.owner = net.minecraft.tileentity.TileEntitySkull.updateGameprofile(this.owner); // fills in the missing information
    }
	
	private GameProfile getGameProfileFromName(String name){
		if(name == null || name.isEmpty()){
			return null;
		}
		GameProfile gp = new GameProfile((UUID)null, name);
		return net.minecraft.tileentity.TileEntitySkull.updateGameprofile(gp); // fills in the missing information
	}
	
	@Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if(LootableBodies.allowCorpseDecay && !this.worldObj.isRemote && worldObj.getWorldTime() % 20 == 0 ){
        	// count-down decay timer
        	if(LootableBodies.decayOnlyWhenEmpty){
        		for(int i = 0; i < this.equipment.length; i++){
        			if(this.equipment[i] != null){
        				this.deathTimestamp = worldObj.getTotalWorldTime();
        				break;
        			}
        		}
        	}
        	if((worldObj.getTotalWorldTime() - this.deathTimestamp) > LootableBodies.corpseDecayTime){
        		this.dropEquipment(true, 0);
        		this.kill();
        		return;
        	}
        }
        if(vacuumTime < VACUUM_TIMELIMIT){
        	// vacuum up loose items (which may be dropped by other mods that give expanded inventories)
        	if(!this.worldObj.isRemote  && this.notFull()){
        		double x1 = this.posX - VACUUM_RADIUS;
        		double y1 = this.posY - VACUUM_RADIUS;
        		double z1 = this.posZ - VACUUM_RADIUS;
        		double x2 = this.posX + VACUUM_RADIUS;
        		double y2 = this.posY + VACUUM_RADIUS;
        		double z2 = this.posZ + VACUUM_RADIUS;
        		List<EntityItem> ae = this.worldObj.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x1,y1,z1,x2,y2,z2));
        		if(!ae.isEmpty()){
        			for(int n = ae.size() - 1; n >= 0; n--){
        				Entity e = ae.get(n); // old-school for-loop in reverse direction in case there are concurrent modification issues
        				ItemStack leftover = vacuumItem(((EntityItem)e).getEntityItem());
        				this.worldObj.removeEntity(e);
        				if(leftover != null){
        					this.entityDropItem(leftover, 0.0f);
        				}
        			}
        		}
        	}
        	vacuumTime++;
        }
        shiftInventory();
        if(deadMode){
        	this.dropEquipment(true, 0);
        	worldObj.removeEntity(this);
        }
    }
    
	/**
	 * condenses inventory when there's an overflow buffer
	 */
	private void shiftInventory(){
		if(!auxInventory.isEmpty()){
			if(equipment[equipment.length - 1] == null){
				// pull item from overflow buffer
				equipment[equipment.length - 1] = auxInventory.pop();
			}
			for(int dstSlot = 5; dstSlot < equipment.length; dstSlot++){
				if(equipment[dstSlot] == null){
					// shift next item to here
					int srcSlot = dstSlot+1;
					while( srcSlot < equipment.length && equipment[srcSlot] == null){
						srcSlot++;
					}
					if(srcSlot == equipment.length){
						// inventory already condensed
						return;
					}
					equipment[dstSlot] = equipment[srcSlot];
					equipment[srcSlot] = null;
				}
			}
		}
	}
    


	@Override
    public void writeEntityToNBT(final NBTTagCompound root) {
        super.writeEntityToNBT(root);
        final NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.equipment.length; ++i) {
            final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            if (this.equipment[i] != null) {
                this.equipment[i].writeToNBT(nbttagcompound1);
            }
            nbttaglist.appendTag(nbttagcompound1);
        }
        root.setTag("Equipment", nbttaglist);
        if(!this.auxInventory.isEmpty()){
        	final NBTTagList nbtauxtaglist = new NBTTagList();
        	Iterator<ItemStack> iter = this.auxInventory.iterator();
        	while (iter.hasNext()) {
        		ItemStack i = iter.next();
        		if(i == null) continue;
                final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                i.writeToNBT(nbttagcompound1);
                nbtauxtaglist.appendTag(nbttagcompound1);
            }
            root.setTag("Aux", nbtauxtaglist);
        }
        if(vacuumTime < VACUUM_TIMELIMIT)root.setByte("Vac", vacuumTime);
        if(owner != null){
        	final NBTTagCompound nbtTagCompound = new NBTTagCompound();
            NBTUtil.writeGameProfile(nbtTagCompound, owner);
            root.setTag("Owner", nbtTagCompound);
        }
        root.setLong("DeathTime", this.deathTimestamp);
    }
    
    @Override
    public void readEntityFromNBT(final NBTTagCompound root) {
    	// read equipment first because parent class also does equipment loading
    	if (root.hasKey("Equipment", 9)) {
            final NBTTagList nbttaglist = root.getTagList("Equipment", 10);
            for (int i = 0; i < equipment.length && i < nbttaglist.tagCount(); ++i) {
                this.equipment[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
            }
        }
    	root.removeTag("Equipment");
    	if(root.hasKey("Aux")){
    		final NBTTagList nbttaglist = root.getTagList("Aux", 10);
    		for (int i = 0; i < equipment.length && i < nbttaglist.tagCount(); ++i) {
                this.auxInventory.addLast(ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i)));
            }
    	}
        super.readEntityFromNBT(root);
        // now read the rest of the tag
        if(root.hasKey("Vac")){
        	this.vacuumTime = root.getByte("Vac");
        } else {
        	this.vacuumTime = VACUUM_TIMELIMIT;
        }
        if(root.hasKey("DeathTime")){
        	this.deathTimestamp = (root.getLong("DeathTime"));
        }
        if (root.hasKey("Yaw")) {
        	this.rotationYaw = root.getFloat("Yaw");
        }
        if (root.hasKey("Owner")) {
        	this.setOwner(NBTUtil.readGameProfileFromNBT(root.getCompoundTag("Owner")));
        }
        if (root.hasKey("Name")) {
        	this.setOwner(new GameProfile(null,root.getString("Name")));
        }
        else if (root.hasKey("ExtraType", 8)) {
            final String string = root.getString("ExtraType");
            if (!StringUtils.isNullOrEmpty(string)) {
                this.setOwner(new GameProfile((UUID)null, string));
            }
        }
        this.setRotation(this.rotationYaw);
    }
    
    @Override protected void damageEntity(final DamageSource src, float amount) {
    //	//System.out.println("Corpse hit by "+(src.getEntity() == null ? "unknown" : src.getEntity().getName())+" (instance of "+src.getClass().getName() +") for "+amount + " damage");
    	// .kill was called, override invulnerability
    	if(src == selfDestruct){
    		super.damageEntity(src,amount);
    		return;
    	}
    	// use shovel to dispose the body
    	if(src.getEntity() != null && src.getEntity() instanceof EntityPlayer && ((EntityPlayer)src.getEntity()).getHeldItem() != null){
    		ItemStack itemStack = ((EntityPlayer)src.getEntity()).getHeldItem(); 
    		Item item = itemStack.getItem();
    		if(item instanceof net.minecraft.item.ItemSpade || item.getHarvestLevel(itemStack, "shovel") >= 0){
    			shovelHits++;
    			super.damageEntity(src,amount);
    			if(shovelHits >= shovelHitLimit){
    				// bury the body
    				this.kill();
    			}
    		}
    	}
    	if(this.posY < -1){
    		// fell out of the world
    		this.kill();
    	}
    	
    	// spit the body out of a wall is hurt by block suffocation
    	if(src.equals(DamageSource.inWall)){
    		jumpOutOfWall();
    	}
    	
    	// special cases handled before this point
    	// general cases:
    	if(invulnerable) return;
    	if(this.hurtByAll) super.damageEntity(src, amount);
    	if(src.getEntity() != null && src.getEntity() instanceof EntityLivingBase && this.hurtByWeapons) super.damageEntity(src, amount);
    	if(src.isFireDamage() && this.hurtByFire) super.damageEntity(src, amount);
    	if(src.isExplosion() && this.hurtByBlast) super.damageEntity(src, amount);
    	if(src == DamageSource.fall && this.hurtByFall) super.damageEntity(src, amount);
    	if(src == DamageSource.cactus && this.hurtByCactus) super.damageEntity(src, amount);
    	if(src == DamageSource.inWall && this.hurtByBlockSuffocation) super.damageEntity(src, amount);
    	if(this.hurtByOther) super.damageEntity(src, amount);
    	
    	// He's dead, Jim
    	if(super.getHealth() <= 0){
    		deadMode = true;
    	}
    }

    public void jumpOutOfWall(){
    	double root2 = 1.414213562;
		BlockPos currentCoord = new BlockPos(this.posX, this.posY, this.posZ);
		// first try going out to the nearest adjacent block
    	double[] vector = new double[3];
    	vector[0] = currentCoord.getX()+0.5 - this.posX;
    	vector[1] = 0;
    	vector[2] = currentCoord.getZ()+0.5 - this.posZ;
    	double normalizer = 1.0/Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]+vector[2]*vector[2]);
    	vector[0] *= normalizer;
    	vector[1] *= normalizer;
    	vector[2] *= normalizer;
    	IBlockState bs = worldObj.getBlockState(new BlockPos(this.posX+vector[0], this.posY+vector[1], this.posZ+vector[2]));
    	if(!(bs.getBlock().getMaterial().blocksMovement())){
    		this.setPosition(this.posX+vector[0], this.posY+vector[1], this.posZ+vector[2]);
    		return;
    	}
    	
		// then try finding an open space in all adjacent blocks
    	BlockPos n;
    	n = currentCoord.up();
    	if(!(worldObj.getBlockState(n).getBlock().getMaterial().blocksMovement())){
    		this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
    		return;
    	}
    	n = currentCoord.north();
    	if(!(worldObj.getBlockState(n).getBlock().getMaterial().blocksMovement())){
    		this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
    		return;
    	}
    	n = currentCoord.east();
    	if(!(worldObj.getBlockState(n).getBlock().getMaterial().blocksMovement())){
    		this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
    		return;
    	}
    	n = currentCoord.south();
    	if(!(worldObj.getBlockState(n).getBlock().getMaterial().blocksMovement())){
    		this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
    		return;
    	}
    	n = currentCoord.west();
    	if(!(worldObj.getBlockState(n).getBlock().getMaterial().blocksMovement())){
    		this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
    		return;
    	}
    	n = currentCoord.down();
    	if(!(worldObj.getBlockState(n).getBlock().getMaterial().blocksMovement())){
    		this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
    		return;
    	}
		// then if the above fails, move 1.5 blocks in a random direction
    	vector[0] = worldObj.rand.nextDouble();
    	vector[1] = worldObj.rand.nextDouble();
    	vector[2] = worldObj.rand.nextDouble();
    	normalizer = root2/Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]+vector[2]*vector[2]);
    	this.setPosition(this.posX+vector[0], this.posY+vector[1], this.posZ+vector[2]);
    }
    
    @Override
    public boolean attackEntityFrom(final DamageSource src, float amount) {
    	// Disable forge hooks to fix plugin bug
		/*
		if (!ForgeHooks.onLivingAttack(this, p_attackEntityFrom_1_, p_attackEntityFrom_2_)) {
	        return false;
	    }
		 */
		if (this.isEntityInvulnerable(src)) {
			return false;
		}
		if (this.worldObj.isRemote) {
			return false;
		}
		this.entityAge = 0;
		if (this.getHealth() <= 0.0f) {
			return false;
		}
		if (src.isFireDamage() && this.isPotionActive(Potion.fireResistance)) {
			return false;
		}
		if ((src == DamageSource.anvil || src == DamageSource.fallingBlock) && this.getEquipmentInSlot(4) != null) {
			this.getEquipmentInSlot(4).damageItem((int)(amount * 4.0f + this.rand.nextFloat() * amount * 2.0f), this);
			amount *= 0.75f;
		}
		boolean flag = true;
		if (this.hurtResistantTime > this.maxHurtResistantTime / 2.0f) {
			if (amount <= this.lastDamage) {
				return false;
			}
			this.damageEntity(src, amount - this.lastDamage);
			this.lastDamage = amount;
			flag = false;
		}
		else {
			this.lastDamage = amount;
			this.hurtResistantTime = this.maxHurtResistantTime;
			this.damageEntity(src, amount);
			final int n = 10;
			this.maxHurtTime = n;
			this.hurtTime = n;
		}
		this.attackedAtYaw = 0.0f;
		final Entity entity = src.getEntity();
		if (entity != null) {
			if (entity instanceof EntityLivingBase) {
				this.setRevengeTarget((EntityLivingBase)entity);
			}
			if (entity instanceof EntityPlayer) {
				this.recentlyHit = 100;
				this.attackingPlayer = (EntityPlayer)entity;
			}
			else if (entity instanceof EntityTameable) {
				final EntityTameable entitywolf = (EntityTameable)entity;
				if (entitywolf.isTamed()) {
					this.recentlyHit = 100;
					this.attackingPlayer = null;
				}
			}
		}
		if (flag) {
			this.worldObj.setEntityState(this, (byte)2);
			if (src != DamageSource.drown) {
				this.setBeenAttacked();
			}
			if (entity != null) {
				double d1;
				double d2;
				for (d1 = entity.posX - this.posX, d2 = entity.posZ - this.posZ; d1 * d1 + d2 * d2 < 1.0E-4; d1 = (Math.random() - Math.random()) * 0.01, d2 = (Math.random() - Math.random()) * 0.01) {}
				this.knockBack(entity, amount, d1, d2);
			}
		}
		if (this.getHealth() <= 0.0f) {
			final String s = this.getDeathSound();
			if (flag && s != null) {
				this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
			}
			this.onDeath(src);
		} else {
			final String s = this.getHurtSound();
			if (flag && s != null) {
				this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
			}
		}
		return true;
    }

    
    @Override
    protected void kill() {
    	deadMode = true;
        this.attackEntityFrom(selfDestruct, this.getMaxHealth());
        this.markDirty();
    }
    
    @Override
    public ItemStack getHeldItem() {
        return this.equipment[0];
    }
    
    @Override
    public ItemStack getEquipmentInSlot(final int slot) {
        return this.equipment[slot % equipment.length];
    }
    
    @Override
    public ItemStack getCurrentArmor(final int armorSlot) {
        return getEquipmentInSlot(armorSlot + 1);
    }
    
    @Override
    public void setCurrentItemOrArmor(final int slot, final ItemStack item) {
        this.equipment[slot] = item;
    }
    
    @Override
    public ItemStack[] getInventory() {
        return this.equipment;
    }
    
    @Override
    protected void dropEquipment(final boolean doDrop, final int dropProbability) {
        if(!doDrop) return;
    	for (int j = this.equipment.length - 1; j >= 0 ; j--) {
            final ItemStack itemstack = equipment[j];
            if (itemstack != null ) {
                this.entityDropItem(itemstack, 0.0f);
                equipment[j] = null;
            }
        }
    	if(!auxInventory.isEmpty()){
	    	Iterator<ItemStack> buffer = this.auxInventory.iterator();
			while(buffer.hasNext()){
				ItemStack itemstack = buffer.next();
				if(itemstack == null) continue;
				this.entityDropItem(itemstack, 0.0f);
			}
			auxInventory.clear();
    	}
    }
    
    boolean notFull(){
    	for(int i = 5; i < equipment.length; i++){
    		if(equipment[i] == null){
    			return true;
    		}
    	}
    	return auxInventory.size() < LootableBodies.corpseAuxilleryInventorySize;
    }
    /**
     * 
     * @param item
     * @return Returns null if the item was taken up, returns an ItemStack if 
     * the item was not taken up (means that it was partially stacked and the 
     * remainder was returned) 
     */
    public ItemStack vacuumItem(ItemStack item){
    	if(item == null) return null;
    	int nextIndex = 5; // after the armor and held item slots
    	while(nextIndex < equipment.length){
    		if(equipment[nextIndex] != null){
	    		if(canStack(item,equipment[nextIndex])){
	    			ItemStack remainder = stackItemStacks(equipment[nextIndex],item);
	    			if(remainder != null){
	    	    		item = remainder;
	    			} else {
	    				return null;
	    			}
	    		}
    		} else {
    			equipment[nextIndex] = applyItemDamage(item);
    			return null;
    		}
			nextIndex++;
    	}
		// inventory is full
		if(auxInventory.size() < LootableBodies.corpseAuxilleryInventorySize){
			// put item in the overflow
			Iterator<ItemStack> buffer = this.auxInventory.iterator();
			while(buffer.hasNext()){
				ItemStack bufferItem = buffer.next();
				if(canStack(bufferItem,item)){
					item = stackItemStacks(bufferItem,item);
					if(item == null){
						return null;
					}
				}
			}
//System.out.println("Adding "+item.toString()+" to the buffer.");
			this.auxInventory.addLast(applyItemDamage(item));
			return null;
		} else {
			// and the overflow buffer is full too
    		return item.copy();
		} 
    	
    }
    /**
     * Stacks two ItemStacks, returning the remainder.
     * @param dest ItemStack to stack into
     * @param src ItemStack we want to add
     * @return Returns the leftover items as an ItemStack (returns null if both 
     * stacks can merge into a single stack) 
     */
    ItemStack stackItemStacks(ItemStack dest, ItemStack src){
    	if(src.stackSize == 0) return null;
//System.out.println("Stacking "+src.toString()+" into "+dest.toString());
    	if(canStack(dest,src)){
    		int maxStackSize = Math.min(dest.getMaxStackSize(),this.getInventoryStackLimit());
			if(src.stackSize + dest.stackSize < maxStackSize){
				dest.stackSize += src.stackSize;
				return null;
			} else if(dest.stackSize < maxStackSize){
				int difference = maxStackSize - dest.stackSize;
				dest.stackSize += difference;
				src.stackSize -= difference;
				if(src.stackSize == 0) return null;
				return src;
			} else {
				return src;
			}
    	} else {
    		// cannot stack
    		return src;
    	}
    }
    
    public static ItemStack applyItemDamage(ItemStack itemstack){
    	if(additionalItemDamage == 0) return itemstack;
    	if (itemstack != null 
    			&& itemstack.isItemStackDamageable()
    			// Damn sloppy modders have metadata items that retrun true when you call itemstack.isItemStackDamageable()
    			// The following are extra checks to really confirm that an item is damageable
    			&& itemstack.getItem().isDamageable() 
    			&& (!itemstack.isStackable())
    			&& (!itemstack.getItem().getHasSubtypes())
    			&& (itemstack.getItem().getMaxDamage() > 0) ) {
            final int newDamageValue = itemstack.getItemDamage() + additionalItemDamage;
            itemstack.setItemDamage(Math.min(newDamageValue, itemstack.getMaxDamage() - 1));
        }
    	return itemstack;
    }
    
    
    boolean canStack(ItemStack a, ItemStack b){
    	if(ItemStack.areItemsEqual(a,b) && a.isStackable()){
    		if(a.getItemDamage() == b.getItemDamage()){
    			if(ItemStack.areItemStackTagsEqual(a,b)){
    				return true;
    			}
    		}
    	}
    	return false;
    }
	
    public void setDeathTime(long timestamp){
    	this.deathTimestamp = timestamp;
    }
    
    @Override
    public int getTalkInterval() {
        return 1200;
    }
	@Override
    public void playLivingSound() {
        // do nothing
    }
	

	
	@Override
    protected int getExperiencePoints(final EntityPlayer p_getExperiencePoints_1_) {
        return 0;
    }
	
	
	@Override
    protected String getLivingSound() {
        return null;
    }
    
	@Override
    protected Item getDropItem() {
        return null;
    }
    
    @Override
    public boolean canBeSteered() {
        return false;
    }
    
    
    @Override
    public void setEquipmentDropChance(final int p_setEquipmentDropChance_1_, final float p_setEquipmentDropChance_2_) {
        // do nothing
    }
    
    @Override
    public boolean canPickUpLoot() {
        return false; // picking up items done in special way
    }
    
    @Override
    public void setCanPickUpLoot(final boolean p_setCanPickUpLoot_1_) {
        // do nothing
    }
    
    
    
    @Override
    protected boolean interact(final EntityPlayer player) {
    	player.displayGUIChest(this);
        return true;
    }
    
    
    
    @Override
    public boolean allowLeashing() {
        return false; // leashing not allowed
    }
    
    protected void playSound(String soundID){
    	if (!worldObj.isRemote)
    	{
    		worldObj.playSoundAtEntity(this, soundID, 0.5F, 0.4F );
    	}
    }
    
    @Override public boolean canBreatheUnderwater() {
        return true;
    }

///// INVENTORY METHODS /////
	//@Override // gradle thinks this method is called clearInventory(), eclipse thinks it is called clear()
	public void clear() {
		for (int i = 0; i < this.equipment.length; ++i) {
            this.equipment[i] = null;
        }
		auxInventory.clear();
	}
	public void clearInventory(){
		clear();
	}


	@Override
	public void closeInventory(EntityPlayer player) {
		playSound("mob.horse.leather");
	}


	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (this.equipment[index] == null) {
            return null;
        }
        if (this.equipment[index].stackSize <= count) {
            final ItemStack itemStack = this.equipment[index];
            this.equipment[index] = null;
            this.markDirty();
            return itemStack;
        }
        final ItemStack splitStack = this.equipment[index].splitStack(count);
        if (this.equipment[index].stackSize == 0) {
            this.equipment[index] = null;
        }
        this.markDirty();
        return splitStack;
	}


	@Override
	public int getField(int id) {
		return 0;
	}


	@Override
	public int getFieldCount() {
		return 0;
	}


	@Override
	public int getInventoryStackLimit() {
		return 64;
	}


	@Override
	public int getSizeInventory() {
		return this.equipment.length;
	}


	@Override
	public ItemStack getStackInSlot(int index) {
		return equipment[index];
	}


	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack x = equipment[index];
		equipment[index] = null;
		return x;
	}



	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(index < 5 && index > 0){
			// armor display slots
			if(getArmorPosition(stack) != index){
				return false;
			}
		}
		return true;
	}


	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		// check distance
		if(deadMode) return false;
		return player.getDistanceSq(this.posX, this.posY, this.posZ) <= 16.0;
	}


	@Override
	public void markDirty() {
		// sync inventory across clients
		// actually, we instead dis-allow concurrent access
	}


	@Override
	public void openInventory(EntityPlayer player) {
		playSound("mob.horse.armor");
	}


	@Override
	public void setField(int id, int value) {
		// do nothing
		
	}


	@Override
	public void setInventorySlotContents(final int slot, final ItemStack item) {
        this.equipment[slot] = item;
        if (item != null && item.stackSize > this.getInventoryStackLimit()) {
            item.stackSize = this.getInventoryStackLimit();
        }
        this.markDirty();
    }


    
///// END OF INVENTORY METHODS /////
   
    
}

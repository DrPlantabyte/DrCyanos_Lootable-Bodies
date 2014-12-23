package cyano.lootable.entities;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	
	final static int WATCHER_ID_BUSY = 27; // data watcher used to exclude multiple people looting body at same time
	final static int WATCHER_ID_OWNER = 28;
	
	private static final DamageSource selfDestruct = new DamageSource(EntityLootableBody.class.getSimpleName());

	
	// TODO: way to dispose an invincible body
	protected final ItemStack[] equipment = new ItemStack[INVENTORY_SIZE];
	private byte vacuumTime = 0;
	private GameProfile owner = null;
	private int shovelHits = 0;
	private static final int shovelHitLimit = 3;
	
	public EntityLootableBody(World w) {
		super(w);
		this.isImmuneToFire = (!hurtByFire) || invulnerable;
		vacuumTime = 0;
		this.getDataWatcher().addObject(WATCHER_ID_BUSY, (byte)0);
		this.getDataWatcher().addObject(WATCHER_ID_OWNER, "");
	}
	
	public boolean isBusy(){
		return this.getDataWatcher().getWatchableObjectByte(WATCHER_ID_BUSY) != 0;
	}
	
	public void setBusy(boolean busy){
		if(worldObj.isRemote) return;
		// server-side only
		if(busy){
			this.getDataWatcher().updateObject(WATCHER_ID_BUSY, (byte)1);
		} else {
			this.getDataWatcher().updateObject(WATCHER_ID_BUSY, (byte)0);
		}
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
		if(worldObj.isRemote){ // remote means client
			this.setRotationYawHead(this.rotationYaw);
		}
	}
	
	@Override
    protected void updateEntityActionState() {
		// do nothing
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
		if (this.owner == null || StringUtils.isNullOrEmpty(this.owner.getName())) {
			return;
		}
		if (owner.isComplete() && owner.getProperties().containsKey((Object)"textures")) {
			return;
		}
		GameProfile field_152110_j = MinecraftServer.getServer().func_152358_ax().func_152655_a(owner.getName());
		if (field_152110_j == null) {
			return;
		}
		if (Iterables.getFirst((Iterable)field_152110_j.getProperties().get("textures"), (Object)null) == null) {
			field_152110_j = MinecraftServer.getServer().func_147130_as().fillProfileProperties(field_152110_j, true);
		}
		owner = field_152110_j;
		
	}

	private GameProfile getGameProfileFromName(String name){
		if(name == null || name.isEmpty()){
			return null;
		}
		GameProfile gp = new GameProfile((UUID)null, name);
		return gp; // may not be fully initialized
	}
	
	@Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if(vacuumTime < VACUUM_TIMELIMIT){
        	// vacuum up loose items (which may be dropped by other mods that give expanded inventories)
        	if(!this.worldObj.isRemote){
        		double x1 = this.posX - VACUUM_RADIUS;
        		double y1 = this.posY - VACUUM_RADIUS;
        		double z1 = this.posZ - VACUUM_RADIUS;
        		double x2 = this.posX + VACUUM_RADIUS;
        		double y2 = this.posY + VACUUM_RADIUS;
        		double z2 = this.posZ + VACUUM_RADIUS;
        		List<Entity> ae = this.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(x1,y1,z1,x2,y2,z2));
        		for(int n = ae.size() - 1; n >= 0; n--){
        			Entity e = ae.get(n); // old-school for-loop in reverse direction in case there are concurrent modification issues
        			//if(e instanceof EntityItem){
        				if(vacuumItem(((EntityItem)e).getEntityItem()))
        					this.worldObj.removeEntity(e);
        			//}
        		}
        	}
        	vacuumTime++;
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
        if(vacuumTime < VACUUM_TIMELIMIT)root.setByte("Vac", vacuumTime);
        if(owner != null){
        	final NBTTagCompound nbtTagCompound = new NBTTagCompound();
            NBTUtil.func_152460_a(nbtTagCompound, owner);
            root.setTag("Owner", nbtTagCompound);
        }
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
        super.readEntityFromNBT(root);
        // now read the rest of the tag
        if(root.hasKey("Vac")){
        	this.vacuumTime = root.getByte("Vac");
        } else {
        	this.vacuumTime = VACUUM_TIMELIMIT;
        }
        if (root.hasKey("Yaw")) {
        	this.rotationYaw = root.getFloat("Yaw");
        }
        if (root.hasKey("Owner")) {
        	this.setOwner(NBTUtil.func_152459_a(root.getCompoundTag("Owner")));
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
    //	System.out.println("Corpse hit by "+(src.getEntity() == null ? "unknown" : src.getEntity().getName())+" (instance of "+src.getClass().getName() +") for "+amount + " damage");
    	// .kill was called, override invulnerability
    	if(src == selfDestruct){
    		super.damageEntity(src,amount);
    		return;
    	}
    	// use shovel to dispose the body
    	if(src.getEntity() != null && src.getEntity() instanceof EntityPlayer && ((EntityPlayer)src.getEntity()).getHeldItem() != null){
    		Item item = ((EntityPlayer)src.getEntity()).getHeldItem().getItem();
    		if(item instanceof net.minecraft.item.ItemSpade || item.getHarvestLevel(new ItemStack(item), "shovel") != -1){
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
    }
    
    @Override
    protected void kill() {
        this.attackEntityFrom(selfDestruct, this.getMaxHealth());
    }
    
    
    @Override public void updateAITasks(){
    	// do nothing
    }
    @Override
    public ItemStack getHeldItem() {
        return this.equipment[0];
    }
    
    @Override
    public ItemStack getEquipmentInSlot(final int slot) {
        return this.equipment[slot % equipment.length];
    }
    
    @Deprecated // 1.7.10 does not provide parent for this method.
    public ItemStack getCurrentArmor(final int armorSlot) {
        return getEquipmentInSlot(armorSlot + 1);
    }
    
    public ItemStack armorItemInSlot(int i){
    	return getCurrentArmor(i);
    }
    
    @Override
    public void setCurrentItemOrArmor(final int slot, final ItemStack item) {
        this.equipment[slot] = item;
    }
    
    @Deprecated // 1.7.10 does not provide parent for this method.
    public ItemStack[] getInventory() {
        return this.equipment;
    }
    
    @Override
    protected void dropEquipment(final boolean doDrop, final int dropProbability) {
        for (int j = 0; j < this.getInventory().length; ++j) {
            final ItemStack itemstack = this.getEquipmentInSlot(j);
            if (itemstack != null && doDrop  ) {
                this.entityDropItem(itemstack, 0.0f);
            }
        }
    }
    
    public boolean vacuumItem(ItemStack item){
    	if(item == null) return true;
    	int nextIndex = 5;
    	while(nextIndex < equipment.length && equipment[nextIndex] != null){
    		if(canStack(equipment[nextIndex],item)){
    			int maxStackSize = Math.min(item.getMaxStackSize(),this.getInventoryStackLimit());
    			if(item.stackSize + equipment[nextIndex].stackSize < maxStackSize){
    				equipment[nextIndex].stackSize += item.stackSize;
    				return true;
    			}
    		}
    		nextIndex++;
    	}
    	if(nextIndex == equipment.length) return false; // inventory is full
    	equipment[nextIndex] = applyItemDamage(item);
    	return true;
    }
    
    private boolean canStack(ItemStack a, ItemStack b){
    	if(a == null || b == null) return false;
    	if(a.getItem() == b.getItem()){
    		if(a.getItemDamage() != b.getItemDamage()) return false;
        	if(a.isStackable() == false )return false;
    		if(a.stackSize + b.stackSize > Math.min(a.getMaxStackSize(),this.getInventoryStackLimit())) return false;
    		if(a.hasTagCompound() == false && b.hasTagCompound() == false){
    			return true;
    		} else if(a.hasTagCompound() && b.hasTagCompound()){
    			return ItemStack.areItemStackTagsEqual(a, b);
    		}
    	}
    	return false;
    }
    
    public static ItemStack applyItemDamage(ItemStack itemstack){
    	if (itemstack != null && itemstack.isItemStackDamageable()) {
            final int newDamageValue = itemstack.getItemDamage() + additionalItemDamage;
            itemstack.setItemDamage(Math.min(newDamageValue, itemstack.getMaxDamage() - 1));
        }
    	return itemstack;
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
	@SideOnly(Side.CLIENT)
    @Override
    public void handleHealthUpdate(final byte p_handleHealthUpdate_1_) {
       // do nothing
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
//    	if(player.getHeldItem().getItem() instanceof ItemSpade && player.isSneaking()){
//    		this.kill();
//    		return true;
//    	}
    	if(isBusy()) return false;
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

///// INVENTORY METHODS /////
	//@Override // gradle thinks this method is called clearInventory(), eclipse thinks it is called clear()
	public void clear() {
		for (int i = 0; i < this.equipment.length; ++i) {
            this.equipment[i] = null;
        }
	}
	public void clearInventory(){
		clear();
	}


	@Override
	public void closeInventory() {
		setBusy(false);
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
	public ItemStack getStackInSlotOnClosing(int index) {
		if (this.equipment[index] != null) {
            final ItemStack itemStack = this.equipment[index];
            this.equipment[index] = null;
            return itemStack;
        }
        return null;
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
		return player.getDistanceSq(this.posX, this.posY, this.posZ) <= 16.0;
	}


	@Override
	public void markDirty() {
		// sync inventory across clients
		// actually, we instead dis-allow concurrent access
	}


	@Override
	public void openInventory() {
		setBusy(true);
		playSound("mob.horse.armor");
	}




	@Override
	public void setInventorySlotContents(final int slot, final ItemStack item) {
        this.equipment[slot] = item;
        if (item != null && item.stackSize > this.getInventoryStackLimit()) {
            item.stackSize = this.getInventoryStackLimit();
        }
        this.markDirty();
    }


	@Override public String getInventoryName(){
		return this.getClass().getSimpleName();
	}
	@Override public boolean hasCustomInventoryName(){
		return false;
	}
///// END OF INVENTORY METHODS /////
   
    
}

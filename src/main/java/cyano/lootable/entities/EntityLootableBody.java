package cyano.lootable.entities;

import com.mojang.authlib.GameProfile;
import cyano.lootable.LootableBodies;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static cyano.lootable.LootableBodies.corpseHP;

public class EntityLootableBody extends EntityLiving implements IInventory{


	private static final DamageSource selfDestruct = new DamageSource(EntityLootableBody.class.getSimpleName());

	public static final EntityEquipmentSlot[] EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET, EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND};

	final static byte VACUUM_TIMELIMIT = 20;
	final static int VACUUM_RADIUS = 3;

	private final ItemStack[] mainInventory = new ItemStack[6*7];
	private final Deque<ItemStack> auxInventory = new LinkedList<>();
	private byte vacuumTime = 0;
	private long deathTimestamp;
	private boolean leftHanded = false; // TODO: handle handed-ness

	private final AtomicReference<GameProfile> gpSwap = new AtomicReference<>(null) ; // for lazy-evaluation of player skins
	private int terminate = -1;
	private static final int DEATH_COUNTDOWN = 10;


	public EntityLootableBody(World w) {
		super(w);
		log("Initializting");// TODO: remove
		deathTimestamp = System.currentTimeMillis();
		this.setAlwaysRenderNameTag(LootableBodies.displayNameTag);
		this.setSize(0.85F, 0.75F);

		this.isImmuneToFire = LootableBodies.completelyInvulnerable || (!LootableBodies.hurtByEnvironment);
	}



	private void log(String format, Object... o){
		FMLLog.info("%s: %s",getClass().getSimpleName(), String.format(format, o));
	}

	private void log(Object o){
		FMLLog.info("%s: %s",getClass().getSimpleName(),String.valueOf(o));
	}

	private String oldName = null;
	@Override
	public void onEntityUpdate(){
		super.onEntityUpdate();
		String nameUpdate = this.getCustomNameTag();
		if(ObjectUtils.notEqual(oldName,nameUpdate)){
			oldName = nameUpdate;
			log("generating game profile from %s",nameUpdate);// TODO: remove
			if(nameUpdate != null && nameUpdate.trim().length() > 0) {
				GameProfile gp = new GameProfile(null, nameUpdate);
				if(this.getEntityWorld().isRemote) gp = TileEntitySkull.updateGameprofile(gp);
				setGameProfile(gp);
				super.setCustomNameTag(nameUpdate);
			} else {
				setGameProfile(null);
				super.setCustomNameTag("");
			}
		}

		if(terminate > 0) terminate--;
		if(terminate == 0){
			// needs to be manually deleted for some reason
			this.dropEquipment(true, 0);
			getEntityWorld().removeEntity(this);
		}
		if(terminate < 0 && (super.getHealth() <= 0 || this.isDead)){
			terminate = DEATH_COUNTDOWN;
		}

		if(!auxInventory.isEmpty()) {
			for (int slot = this.getSizeInventory() - 1; slot >= EQUIPMENT_SLOTS.length; slot--) {
				if(this.getStackInSlot(slot) == null){
					this.setInventorySlotContents(slot,auxInventory.pollFirst());
					this.markDirty();
					break;
				}
			}
		}
		// TODO: item vacuuming
	}

	@Override
	protected void damageEntity(DamageSource src, float amount){
		if(src.damageType.equals(DamageSource.outOfWorld)) super.damageEntity(src,amount); // kill command and falling out of the world
		// TODO: damage management and wall escape
		log("%s damage from %s",amount, src.damageType);// TODO: remove
		EntityVillager h;
		super.damageEntity(src,amount);
	}


	public GameProfile getGameProfile(){
		return gpSwap.get();
	}

	public void setGameProfile(GameProfile gp){
		gpSwap.set(gp);
		log("Game profile set to %s", gp);// TODO: remove
	}
	public void setUserName(String name){
		this.setCustomNameTag(name);
		log("Name change request %s", name);// TODO: remove
	}

	private long getDeathTimestamp() {
		return deathTimestamp;
	}
	private void setDeathTimestamp(long timestamp) {
		deathTimestamp = timestamp;
	}


	@Override
	protected void applyEntityAttributes() {
		// called during constructor of EntityLivingBase
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(corpseHP);
	}

	public float getRotation(){
		return this.rotationYaw;
	}

	public void setRotation(float newRot){
		this.rotationYaw = newRot;
		this.renderYawOffset = this.rotationYaw;
		this.prevRenderYawOffset = this.rotationYaw;
		this.prevRotationYaw = this.rotationYaw;
		this.setRotationYawHead(this.rotationYaw);
	}


	@Override
	protected boolean canDespawn() {
		return false;
	}

	public void jumpOutOfWall(){
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
		if(!(bs.getMaterial().blocksMovement())){
			this.setPosition(this.posX+vector[0], this.posY+vector[1], this.posZ+vector[2]);
			return;
		}

		// then try finding an open space in all adjacent blocks
		BlockPos n;
		n = currentCoord.up();
		if(!(worldObj.getBlockState(n).getMaterial().blocksMovement())){
			this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
			return;
		}
		n = currentCoord.north();
		if(!(worldObj.getBlockState(n).getMaterial().blocksMovement())){
			this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
			return;
		}
		n = currentCoord.east();
		if(!(worldObj.getBlockState(n).getMaterial().blocksMovement())){
			this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
			return;
		}
		n = currentCoord.south();
		if(!(worldObj.getBlockState(n).getMaterial().blocksMovement())){
			this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
			return;
		}
		n = currentCoord.west();
		if(!(worldObj.getBlockState(n).getMaterial().blocksMovement())){
			this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
			return;
		}
		n = currentCoord.down();
		if(!(worldObj.getBlockState(n).getMaterial().blocksMovement())){
			this.setPosition(n.getX()+0.5,n.getY()+0.015625, n.getZ()+0.5);
			return;
		}
		// then if the above fails, move 2 blocks in a random direction
		vector[0] = worldObj.rand.nextDouble() * 2;
		vector[1] = worldObj.rand.nextDouble() * 2;
		vector[2] = worldObj.rand.nextDouble() * 2;
		this.setPosition(this.posX+vector[0], this.posY+vector[1], this.posZ+vector[2]);
	}



	@Override
	protected void kill() {
		terminate = DEATH_COUNTDOWN;
		this.attackEntityFrom(selfDestruct, this.getMaxHealth());
		this.markDirty();
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
	protected SoundEvent getAmbientSound() {
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
	public boolean canBeLeashedTo(EntityPlayer player) {
		return false; // leashing not allowed
	}


	@Override
	public boolean canPickUpLoot() {
		return false; // picking up items done in special way
	}

	@Override
	public void setCanPickUpLoot(final boolean p_setCanPickUpLoot_1_) {
		// do nothing
	}


	@Override public boolean canBreatheUnderwater() {
		return true;
	}

	////////// IInventory //////////
	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory() {
		return EQUIPMENT_SLOTS.length + mainInventory.length;
	}

	/**
	 * Returns the stack in the given slot.
	 *
	 * @param index
	 */
	@Override
	public ItemStack getStackInSlot(int index) {
		if(index < EQUIPMENT_SLOTS.length){
			return super.getItemStackFromSlot(EQUIPMENT_SLOTS[index]);
		} else {
			return mainInventory[index - EQUIPMENT_SLOTS.length];
		}
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 *
	 * @param index
	 * @param stack
	 */
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(index < EQUIPMENT_SLOTS.length){
			super.setItemStackToSlot(EQUIPMENT_SLOTS[index], stack);
		} else {
			mainInventory[index - EQUIPMENT_SLOTS.length] = stack;
		}
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 *
	 * @param index
	 */
	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack i = this.getStackInSlot(index);
		this.setInventorySlotContents(index,null);
		return i;
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 *
	 * @param index
	 * @param count
	 */
	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack i = this.getStackInSlot(index);
		ItemStack result = i.splitStack(count);
		if (i.stackSize <= 0) {
			i = null;
		}
		this.setInventorySlotContents(index, i);
		return result;
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
	 * hasn't changed and skip it.
	 */
	@Override
	public void markDirty() {
		// do nothing
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes with Container
	 *
	 * @param player
	 */
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack item){
		log("Interaction");// TODO: remove
		if(player.getPosition().distanceSq(this.getPosition()) < 25) {
			log("Sending GUI request");// TODO: remove
			FMLNetworkHandler.openGui(player, LootableBodies.getInstance(), 0, getEntityWorld(), this.getEntityId(), 0, 0);
			return true;
		}
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		log("opening");// TODO: remove
		this.getEntityWorld().playSound(player,posX,posY,posZ, SoundEvents.entity_horse_saddle, SoundCategory.PLAYERS,1F,1F);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		//
		log("closing");// TODO: remove
		this.getEntityWorld().playSound(player,posX,posY,posZ, SoundEvents.entity_horse_saddle, SoundCategory.PLAYERS,1F,1F);
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
	 *
	 * @param index
	 * @param stack
	 */
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(index < 4){
			return stack.getItem().isValidArmor (stack, EQUIPMENT_SLOTS[index], this);
		} else {
			return true;
		}
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// do nothing
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for(int i = 0; i < EQUIPMENT_SLOTS.length; i++){
			this.setInventorySlotContents(i,null);
		}
		Arrays.fill(mainInventory,null);
		auxInventory.clear();
	}

	////////// End of IInventory //////////


	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
		return EnumCreatureAttribute.UNDEAD;
	}


	@Override
	public void writeEntityToNBT(final NBTTagCompound root) {
		super.writeEntityToNBT(root);
		final NBTTagList equipmentListTag = new NBTTagList();
		for (int i = 0; i < this.getSizeInventory(); i++) {
			ItemStack item = this.getStackInSlot(i);
			if (item != null) {
				NBTTagCompound slotTag = new NBTTagCompound();
				slotTag.setByte("Slot", (byte)i);
				item.writeToNBT(slotTag);
				equipmentListTag.appendTag(slotTag);
			}
		}
		root.setTag("Equipment", equipmentListTag);

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
		if(getGameProfile() != null){
			root.setString("Name", getGameProfile().getName());
		}
		root.setLong("DeathTime", this.getDeathTimestamp());
	}


	@Override
	public void readEntityFromNBT(final NBTTagCompound root) {
		log("Reading from NBT: %s", root.toString());// TODO: remove
		log("root.getString(\"Name\")->%s", root.getString("Name"));// TODO: remove
		super.readEntityFromNBT(root);
		if (root.hasKey("Equipment", 9)) {
			final NBTTagList nbttaglist = root.getTagList("Equipment", 10);
			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound slotTag = nbttaglist.getCompoundTagAt(i);
				int slot = slotTag.getByte("Slot");
				ItemStack item = ItemStack.loadItemStackFromNBT(slotTag);
				this.setInventorySlotContents(slot,item);
			}
		}
		if(root.hasKey("Aux")){
			final NBTTagList nbttaglist = root.getTagList("Aux", 10);
			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				this.auxInventory.addLast(ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i)));
			}
		}
		if(root.hasKey("Vac")){
			this.vacuumTime = root.getByte("Vac");
		} else {
			this.vacuumTime = VACUUM_TIMELIMIT;
		}
		if(root.hasKey("DeathTime")){
			this.setDeathTimestamp(root.getLong("DeathTime"));
		}
		if (root.hasKey("Yaw")) {
			this.rotationYaw = root.getFloat("Yaw");
		}
		if (root.hasKey("Name")) {
			this.setUserName(root.getString("Name"));
		}
		this.setRotation(this.rotationYaw, 0);
	}

	public boolean useThinArms() {
		if(this.getGameProfile() != null){
			UUID uid = this.getGameProfile().getId();
			if(uid != null){
				return (uid.hashCode() & 1) == 1;
			}
		}
		return false;
	}
}

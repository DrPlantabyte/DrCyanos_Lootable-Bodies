package cyano.lootable.entities;

import com.mojang.authlib.GameProfile;
import cyano.lootable.LootableBodies;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class EntityLootableBody extends EntityLiving implements IInventory{



	final static byte VACUUM_TIMELIMIT = 20;
	final static int VACUUM_RADIUS = 3;

	private final Deque<ItemStack> auxInventory = new LinkedList<>();
	private byte vacuumTime = 0;
	private long deathTimestamp;

	private final AtomicReference<GameProfile> gpSwap = new AtomicReference<>(null) ; // for lazy-evaluation of player skins



	public EntityLootableBody(World w) {
		super(w);
		log("Initializting");// TODO: remove
		deathTimestamp = System.currentTimeMillis();
		this.setAlwaysRenderNameTag(LootableBodies.displayNameTag);
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
				gp = TileEntitySkull.updateGameprofile(gp);
				setGameProfile(gp);
				super.setCustomNameTag(nameUpdate);
			} else {
				setGameProfile(null);
				super.setCustomNameTag("");
			}
		}
	}


	public GameProfile getGameProfile(){
		super.onUpdate();
		return gpSwap.get();
	}
	public void setGameProfile(GameProfile gp){
		gpSwap.set(gp);
		log("Game profile set to %s", gp);// TODO: remove
	}
	public void setGameProfileFromUserName(String name){
		this.setCustomNameTag(name);
		log("Name change request %s", name);// TODO: remove
	}

	private long getDeathTimestamp() {
		return deathTimestamp;
	}
	private void setDeathTimestamp(long timestamp) {
		deathTimestamp = timestamp;
	}



	////////// IInventory //////////
	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory() {
		return 0; // TODO: implementation
	}

	/**
	 * Returns the stack in the given slot.
	 *
	 * @param index
	 */
	@Override
	public ItemStack getStackInSlot(int index) {
		return null; // TODO: implementation
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 *
	 * @param index
	 * @param count
	 */
	@Override
	public ItemStack decrStackSize(int index, int count) {
		return null; // TODO: implementation
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 *
	 * @param index
	 */
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null; // TODO: implementation
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 *
	 * @param index
	 * @param stack
	 */
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		// TODO: implementation
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit() {
		return 0; // TODO: implementation
	}

	/**
	 * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
	 * hasn't changed and skip it.
	 */
	@Override
	public void markDirty() {
		// TODO: implementation
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes with Container
	 *
	 * @param player
	 */
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false; // TODO: implementation
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO: implementation
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO: implementation
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
	 *
	 * @param index
	 * @param stack
	 */
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return false; // TODO: implementation
	}

	@Override
	public int getField(int id) {
		return 0; // TODO: implementation
	}

	@Override
	public void setField(int id, int value) {
		// TODO: implementation
	}

	@Override
	public int getFieldCount() {
		return 0; // TODO: implementation
	}

	@Override
	public void clear() {
		// TODO: implementation
	}

	////////// End of IInventory //////////

	// TODO: Rewriting from scratch

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
			this.setGameProfileFromUserName(root.getString("Name"));
		}
		this.setRotation(this.rotationYaw, 0);
	}

}

package cyano.lootable.graphics;

import cyano.lootable.entities.EntityLootableBody;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Chris on 4/12/2016.
 */
public class CorpseContainer extends net.minecraft.inventory.Container {

	private static final EntityEquipmentSlot[] EQUIPMENT_SLOTS = EntityLootableBody.EQUIPMENT_SLOTS;

	private final IInventory targetInventory;
	public CorpseContainer(InventoryPlayer playerItems, IInventory entity){
		this.targetInventory = entity;
		int index = 0;
		while(index < 4) {
			// armor
			final int i = index;
			this.addSlotToContainer(new net.minecraft.inventory.Slot(targetInventory, i, 8, 8 + 18*i) {
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public boolean isItemValid(ItemStack item) {
					return super.isItemValid(item) && item.getItem().isValidArmor(item, EQUIPMENT_SLOTS[i], (Entity) targetInventory);
				}
				
				@Override
				@SideOnly(Side.CLIENT)
				public String getSlotTexture()
				{
					return ItemArmor.EMPTY_SLOT_NAMES[3-i];
				}
			});
			index++;
		}
		while(index < EQUIPMENT_SLOTS.length){
			// held items
			final int i = index;
			this.addSlotToContainer(new net.minecraft.inventory.Slot(targetInventory, i, 8 + 18 * (i - 4), 98 ) {
				@Override
				@SideOnly(Side.CLIENT)
				public String getSlotTexture()
				{
					return "minecraft:items/empty_armor_slot_shield";
				}
			});
			index++;
		}

		while(index < targetInventory.getSizeInventory()){
			int n = index - EQUIPMENT_SLOTS.length;
			int x = n % 6;
			int y = n / 6;

			this.addSlotToContainer(new Slot(targetInventory, index, 62 + x * 18, 8 + y * 18));

			index++;
		}

		bindPlayerInventory(playerItems, 140);
	}



	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return targetInventory.isUseableByPlayer(entityplayer);
	}


	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		int hostSize = targetInventory.getSizeInventory();
		ItemStack stack = null;
		Slot slotObject = inventorySlots.get(slot);
		//null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			//merges the item into player inventory since its in the tileEntity
			if (slot < hostSize) {
				if (!this.mergeItemStack(stackInSlot, hostSize, 36+hostSize, true)) {
					return null;
				}
			}
			//places it into the tileEntity if possible since it's in the player inventory
			else if (!this.mergeItemStack(stackInSlot, 0, hostSize, false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}

	protected void bindPlayerInventory(InventoryPlayer playerInv, int yOffset) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, yOffset+58));
		}
	}

}

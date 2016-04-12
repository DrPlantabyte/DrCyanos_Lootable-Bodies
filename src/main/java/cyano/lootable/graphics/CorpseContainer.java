package cyano.lootable.graphics;

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

	private static final EntityEquipmentSlot[] EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET, EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND};

	private final IInventory entity;
	public CorpseContainer(InventoryPlayer playerItems, IInventory entity){
		this.entity = entity;
		net.minecraft.inventory.ContainerHorseInventory k;
		net.minecraft.inventory.ContainerPlayer h;
		int index = 0;
		while(index < 4) {
			// armor
			final int i = index;
			this.addSlotToContainer(new net.minecraft.inventory.Slot(entity, i, 8, 8 + 18*i) {
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public boolean isItemValid(ItemStack item) {
					return super.isItemValid(item) && item.getItem().isValidArmor(item, EQUIPMENT_SLOTS[i], (Entity) entity);
				}
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
			this.addSlotToContainer(new net.minecraft.inventory.Slot(entity, i, 8 + 18 * (i - 4), 98 ) {
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public boolean isItemValid(ItemStack item) {
					return super.isItemValid(item) && item.getItem().isValidArmor(item, EQUIPMENT_SLOTS[i], (Entity) entity);
				}
				@SideOnly(Side.CLIENT)
				public String getSlotTexture()
				{
					if(EQUIPMENT_SLOTS[i] == EntityEquipmentSlot.OFFHAND) {
						return "minecraft:items/empty_armor_slot_shield";
					} else {
						return super.getSlotTexture();
					}
				}
			});
			index++;
		}

		bindPlayerInventory(playerItems, 140);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return false;
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer, int yOffset) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, yOffset + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, yOffset+58));
		}
	}
}

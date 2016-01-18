package cyano.lootable.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cyano.lootable.LootableBodies;
import cyano.lootable.entities.EntityLootableBody;

public class PlayerDeathEventHandler {
	
	@SubscribeEvent(priority=EventPriority.LOW) 
	//public void playerDeathEvent(PlayerDropsEvent event){
	public void playerDeathEvent(PlayerDropsEvent event){
		if(event.entityLiving instanceof EntityPlayer){
			
		EntityPlayer player = (EntityPlayer)event.entityLiving;
//System.out.println("Player "+player.getName()+" died. Entity on "+(event.entity.worldObj.isRemote ? "client" : "server") + " world.");
		World w = event.entity.worldObj;
		//if (!w.isRemote ){
			
			float rotation = player.getRotationYawHead();
			EntityLootableBody corpse = new EntityLootableBody(w);
			corpse.setPositionAndRotation(player.posX, player.posY, player.posZ,rotation,0);
			corpse.setDeathTime(w.getTotalWorldTime());

//System.out.println("Creating corpse with UUID "+corpse.getOwner()+" at ("+corpse.posX+","+corpse.posY+","+corpse.posZ+") with rotation "+rotation+".");
			if(w.getGameRules().getBoolean("keepInventory") == false){ // keepInventory check
				// set items
				corpse.setCurrentItemOrArmor(0, EntityLootableBody.applyItemDamage(withdrawHeldItem(player)));
				for(int i = 0; i < 4; i++){
					corpse.setCurrentItemOrArmor(i+1, EntityLootableBody.applyItemDamage(player.getCurrentArmor(i)));
					player.inventory.armorInventory[i] = null;
				}
				for(int i = 0; i < player.inventory.mainInventory.length; i++){
					corpse.vacuumItem(player.inventory.mainInventory[i]);
					player.inventory.mainInventory[i] = null;
				}
			}
			 // for the LOLs
			if(LootableBodies.addBonesToCorpse){
				corpse.vacuumItem(new ItemStack(Items.rotten_flesh,w.rand.nextInt(3)+1));
				corpse.vacuumItem(new ItemStack(Items.bone,w.rand.nextInt(3)+1));
			}
			w.spawnEntityInWorld(corpse);
			try{
				corpse.setOwner(player.getGameProfile());
			}catch(Exception ex){
				FMLLog.severe("%s: Error occured while setting owner of corpse.\n%s", this.getClass().getName(), ex);
			}
			corpse.setRotation(rotation);
	 //   }
		}
	}
	
	static ItemStack withdrawHeldItem(EntityPlayer player){
		ItemStack item = player.getHeldItem(); // get item
		if(item == null) return null;
		// remove from inventory
		for(int i = 0; i < player.inventory.getSizeInventory(); i++){
			if(player.inventory.getStackInSlot(i) == null)continue;
			if(item == player.inventory.getStackInSlot(i)){
				player.inventory.removeStackFromSlot(i);
				break;
			}
		}
		return item; // return the item
	}
	
	static boolean deepEquals(ItemStack a, ItemStack b){
		return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStacksEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
	}
}

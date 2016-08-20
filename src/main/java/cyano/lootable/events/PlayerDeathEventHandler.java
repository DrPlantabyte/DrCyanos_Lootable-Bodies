package cyano.lootable.events;

import net.minecraft.enchantment.EnchantmentHelper;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import cyano.lootable.LootableBodies;
import cyano.lootable.entities.EntityLootableBody;

import java.util.Map;

public class PlayerDeathEventHandler {

        // set PlayerDropsEvent to *LOWEST* priority to remove compatibility issues
        @SubscribeEvent(priority=EventPriority.LOWEST)
        public void playerDeathEvent(PlayerDropsEvent event){
        // define variables here
        EntityPlayer player = (EntityPlayer) event.entity;
        World w = event.entity.worldObj;
        // checks to see if event isn't canceled before continuing, preventing errors
        if ((!player.worldObj.isRemote) && (!event.isCanceled()));
        
        // initiates body creation after passing check               
{						
			float rotation = player.getRotationYawHead();
            EntityLootableBody corpse = new EntityLootableBody(w);
            corpse.setPositionAndRotation(player.posX, player.posY, player.posZ, rotation, 0);
            corpse.setDeathTime(w.getTotalWorldTime());
//System.out.println("Creating corpse with UUID "+corpse.getOwner()+" at ("+corpse.posX+","+corpse.posY+","+corpse.posZ+") with rotation "+rotation+".");
            if(w.getGameRules().getGameRuleBooleanValue("keepInventory") == false){
                // set items
                corpse.setCurrentItemOrArmor(0, EntityLootableBody.applyItemDamage(withdrawHeldItem(player)));
                for(int i = 0; i < 4; i++){
                    Map<Integer, Integer> enchantments = player.inventory.armorInventory[i] != null ? EnchantmentHelper.getEnchantments(player.inventory.armorInventory[i]) : null;
                    if (enchantments != null && enchantments.containsKey(LootableBodies.eioSoulboundID)) {
                        System.out.println("Armor item: This item should not be in the corpse");
                        //Skip
                    } else {
                        corpse.setCurrentItemOrArmor(i + 1, EntityLootableBody.applyItemDamage(player.getCurrentArmor(i)));
                        player.inventory.armorInventory[i] = null;
                    }
                }
                for(int i = 0; i < player.inventory.mainInventory.length; i++){
                    Map<Integer, Integer> enchantments = player.inventory.mainInventory[i] != null ? EnchantmentHelper.getEnchantments(player.inventory.mainInventory[i]) : null;
                    if (enchantments != null && enchantments.containsKey(LootableBodies.eioSoulboundID)) {
                        System.out.println("Inventory item: This item should not be in the corpse");
                        //Skip
                    } else {
                        corpse.vacuumItem(player.inventory.mainInventory[i]);
                        player.inventory.mainInventory[i] = null;
                    }
                }
            }
             // for the LOLs
            if(LootableBodies.addBonesToCorpse){
                corpse.vacuumItem(new ItemStack(Items.rotten_flesh,w.rand.nextInt(3)+1));
                corpse.vacuumItem(new ItemStack(Items.bone,w.rand.nextInt(3)+1));
            }
            // removed profile fetching as it isn't needed, and creates issues when creating bodies(duplicating them if Witchery is installed, and player is vampire*)
            w.spawnEntityInWorld(corpse);
            corpse.setRotation(rotation);
     //   }
        }
    }
    
    static ItemStack withdrawHeldItem(EntityPlayer player){
        ItemStack item = player.getHeldItem(); // get item
        if(item == null) return null;
        Map<Integer, Integer> enchantments = EnchantmentHelper.getEnchantments(item);
        if (enchantments != null && enchantments.containsKey(LootableBodies.eioSoulboundID)) {
            System.out.println(item.getDisplayName() + ": This item should not be in the corpse");
            return null;
        }
        // remove from inventory
        for(int i = 0; i < player.inventory.getSizeInventory(); i++){
            if(player.inventory.getStackInSlot(i) == null)continue;
            if(item == player.inventory.getStackInSlot(i)){
                player.inventory.setInventorySlotContents(i, null);
                break;
            }
        }
        return item; // return the item
    }
    
    static boolean deepEquals(ItemStack a, ItemStack b){
        return ItemStack.areItemStacksEqual(a, b) && ItemStack.areItemStacksEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }
}
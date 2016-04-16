package cyano.lootable.events;

import cyano.lootable.LootableBodies;
import cyano.lootable.entities.EntityLootableBody;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDeathEventHandler {

	private static final Map<EntityPlayer,Map<ItemStack,EntityEquipmentSlot>> equipmentCache = new HashMap<>();

	// TODO: handed-ness
	// TODO: bones and rotten flesh
	@SubscribeEvent(priority= EventPriority.LOW)
	public void entityHurtEvent(LivingHurtEvent e){
		log("%s: %s %s damage to %s",e.getClass(), e.getSource().damageType, e.getAmount(), e.getEntity().getClass());// TODO: remove
	}

	@SubscribeEvent(priority= EventPriority.LOW)
	public void entityDeathEvent(LivingDeathEvent e){
		log("%s: %s was killed by %s ",e.getClass(), e.getEntity().getClass(), e.getSource().damageType);// TODO: remove
		if(e.getEntity() instanceof EntityPlayer
				&& e.getResult() != Event.Result.DENY
				&& !e.getEntity().getEntityWorld().isRemote) {
			final EntityPlayer player = (EntityPlayer)e.getEntity();
			if(player.isSpectator()) return;
			Map<ItemStack,EntityEquipmentSlot> cache = equipmentCache.computeIfAbsent(player,(EntityPlayer p)->new HashMap<>());
			for(EntityEquipmentSlot slot : EntityLootableBody.EQUIPMENT_SLOTS){
				cache.put(player.getItemStackFromSlot(slot),slot);
			}

			if(player.getPrimaryHand() == EnumHandSide.LEFT){
				// swap main and off hand items (easier than messing with the rendering code)
				cache.put(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND),EntityEquipmentSlot.OFFHAND);
				cache.put(player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND),EntityEquipmentSlot.MAINHAND);
			}
		}
	}

	@SubscribeEvent(priority= EventPriority.LOWEST)
	public void entityDropEvent(LivingDropsEvent e){
		log("%s: %s was dropped by %s. Dropped items: %s",e.getClass(), e.getEntity().getClass(), e.getSource().damageType, e.getDrops());// TODO: remove
		if(e.getEntity() instanceof EntityPlayer
				&& e.getResult() != Event.Result.DENY
				&& !e.getEntity().getEntityWorld().isRemote) {
			final EntityPlayer player = (EntityPlayer)e.getEntity();
			if(player.isSpectator()) return;
			final World w = player.getEntityWorld();
			Map<ItemStack,EntityEquipmentSlot> cache = equipmentCache.computeIfAbsent(player, (EntityPlayer p) -> new HashMap<>());
			log("slot cache: %s ",cache);// TODO: remove

			EntityLootableBody corpse = new EntityLootableBody(player);
			corpse.setUserName(player.getName());
			log("player %s dropping loot at (%s d%s/dt, %s d%s/dt, %s d%s/dt)",player.getName(), player.posX, player.motionX, player.posY, player.motionY, player.posZ, player.motionZ);// TODO: remove
			corpse.setRotation(player.rotationYaw);

			List<ItemStack> items = new ArrayList<>();
			for (EntityItem itemEntity : e.getDrops()) {
				ItemStack item = itemEntity.getEntityItem();
				if (item != null && cache.containsKey(item)) {
					corpse.setItemStackToSlot(cache.get(item),item);
				} else {
					items.add(item);
				}
			}
			corpse.initializeItems(items.toArray(new ItemStack[0]));

			if(LootableBodies.addBonesToCorpse){
				corpse.addItem(new ItemStack(Items.bone,1+w.rand.nextInt(3)));
				corpse.addItem(new ItemStack(Items.rotten_flesh,1+w.rand.nextInt(3)));
			}

			w.spawnEntityInWorld(corpse);

			e.getDrops().clear();
		}
	}


	private static void log(String s, Object... o){
		FMLLog.info("%s: %s", LootableBodies.MODID,String.format(s,o));
	}
	private static void log(Object o){
		FMLLog.info("%s: %s", LootableBodies.MODID,String.valueOf(o));
	}
}

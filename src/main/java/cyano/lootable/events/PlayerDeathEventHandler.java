package cyano.lootable.events;

import cyano.lootable.LootableBodies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerDeathEventHandler {

	@SubscribeEvent(priority= EventPriority.LOW)
	public void entityHurtEvent(LivingHurtEvent e){
		log("%s: %s %s damage to %s",e.getClass(), e.getSource().damageType, e.getAmount(), e.getEntity().getClass());
	}

	@SubscribeEvent(priority= EventPriority.LOW)
	public void entityDeathEvent(LivingDeathEvent e){
		log("%s: %s was killed by %s ",e.getClass(), e.getEntity().getClass(), e.getSource().damageType);
	}

	@SubscribeEvent(priority= EventPriority.LOW)
	public void entityDropEvent(LivingDropsEvent e){
		log("%s: %s was dropped by %s. Dropped items: %s",e.getClass(), e.getEntity().getClass(), e.getSource(), e.getDrops());
	}

	public void playerDeathEvent(){
		// TODO: Rewriting from scratch
		EntityPlayer p;
	}


	private static void log(String s, Object... o){
		FMLLog.info("%s: %s", LootableBodies.MODID,String.format(s,o));
	}
	private static void log(Object o){
		FMLLog.info("%s: %s", LootableBodies.MODID,String.valueOf(o));
	}
}

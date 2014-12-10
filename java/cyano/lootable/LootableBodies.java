package cyano.lootable;


import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import cyano.lootable.entities.EntityLootableBody;
import cyano.lootable.events.PlayerDeathEventHandler;

@Mod(modid = LootableBodies.MODID, name=LootableBodies.NAME, version = LootableBodies.VERSION)
public class LootableBodies {
    public static final String MODID = "lootablebodies";
    public static final String NAME ="DrCyano's Lootable Bodies";
    public static final String VERSION = "1.0.0";
	
    public static boolean fancyCorpses = false;
    
    @SidedProxy(clientSide="cyano.lootable.ClientProxy", serverSide="cyano.lootable.ServerProxy")
    public static Proxy proxy;
    
	
	// Mark this method for receiving an FMLEvent (in this case, it's the FMLPreInitializationEvent)
    @EventHandler public void preInit(FMLPreInitializationEvent event)
    {
        // Do stuff in pre-init phase (read config, create blocks and items, register them)
    	// load config
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    	config.load();
		
    	EntityLootableBody.additionalItemDamage = config.getInt("item_damage_on_death", "options", 32, 0,1000,
				"The amount of damage suffered by damageable items when you \n"
				+ "die, to a minimum of 1 durability remaining (items will \n"
				+ "not be destroyed).");
    	EntityLootableBody.corpseHP = config.getFloat("corpse_HP", "options", 50, 1,Short.MAX_VALUE,
				"The amount of damage a corpse can suffer before being \n"
				+ "destroyed and releasing its items. \n"
				+ "Note that 10 hearts = 20 HP.");
    	EntityLootableBody.fireproof = config.getBoolean("corpse_fireproof", "options", true,
				"If true, corpses will not be damaged by fire or lava.");
    	EntityLootableBody.blastproof = config.getBoolean("corpse_blastproof", "options", true,
				"If true, corpses will not be damaged by creepers or TNT. \n"
				+ "If you make the corpse blast-proof, you will probably \n"
				+ "want to also make is fall-proof");
    	EntityLootableBody.fallproof = config.getBoolean("corpse_fallproof", "options", true,
				"If true, corpses will not be damaged by falling.");
    	EntityLootableBody.invulnerable = config.getBoolean("corpse_indestructible", "options", false,
				"If true, corpses will be immune to all damage. You can \n"
				+ "still destroy a corpse by hitting it with a shovel.");
    	fancyCorpses = config.getBoolean("use_player_skin", "options", true,
				"If true, corpses will have the skins of the player who \n"
				+ "died. If false, then skeletons will be used instead.");
    	
	//	OreDictionary.initVanillaEntries()
		config.save();
		proxy.preInit(event);
    }

	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		// register entities
		registerItemRenders();
		
		registerEntity(EntityLootableBody.class);
		MinecraftForge.EVENT_BUS.register(new PlayerDeathEventHandler());
 		
		proxy.init(event);
		
		
	}
	private int entityIndex = 0;
	private void registerEntity(Class entityClass){
		String idName = MODID+"_"+entityClass.getSimpleName();
		EntityRegistry.registerGlobalEntityID(entityClass, idName, EntityRegistry.findGlobalUniqueEntityId());
 		EntityRegistry.registerModEntity(entityClass, idName, entityIndex++/*id*/, this, 64/*trackingRange*/, 10/*updateFrequency*/, true/*sendsVelocityUpdates*/);
 		
	}
    
    private void registerItemRenders() {
    	// client-side only
    	if(proxy instanceof ServerProxy) return;
    //	registerItemRender(wandGeneric,OrdinaryWand.itemName);
	}
    
    private void registerItemRender(Item i, String itemName){
    	Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(i, 0, new ModelResourceLocation(MODID+":"+itemName, "inventory"));
    }

	
	@EventHandler public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}
	/*
	@EventHandler public void onServerStarting(FMLServerStartingEvent event)
	{
		// stub
	}
	*/
	
	

}

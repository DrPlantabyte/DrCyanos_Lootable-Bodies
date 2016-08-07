package cyano.lootable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cyano.lootable.entities.EntityLootableBody;
import cyano.lootable.graphics.RenderLootableBody;
import cyano.lootable.graphics.RenderSkinnedLootableBody;

public class ClientProxy extends Proxy{
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        // client-only pre-init code
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        // client-only init code
        RenderManager rm = RenderManager.instance;
        // add renderers
        // Fancy Corpses not supported in 1.7.10!
//        if(LootableBodies.fancyCorpses){
//          RenderingRegistry.registerEntityRenderingHandler(EntityLootableBody.class, new RenderSkinnedLootableBody(rm));
//        }else {
            RenderingRegistry.registerEntityRenderingHandler(EntityLootableBody.class, new RenderLootableBody(rm));
//        }
        
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        // client-only post-init code
    }
    
}
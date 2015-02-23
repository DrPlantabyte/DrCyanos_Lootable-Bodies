package cyano.lootable;

import cyano.lootable.entities.EntityLootableBody;
import cyano.lootable.graphics.RenderLootableBody;
import cyano.lootable.graphics.RenderSkinnedLootableBody;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;

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
        RenderManager rm = Minecraft.getMinecraft().getRenderManager();
        // add renderers
        if(LootableBodies.fancyCorpses){
        	RenderingRegistry.registerEntityRenderingHandler(EntityLootableBody.class, new RenderSkinnedLootableBody(rm));
        }else {
        	RenderingRegistry.registerEntityRenderingHandler(EntityLootableBody.class, new RenderLootableBody(rm));
        }
    	
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        // client-only post-init code
    }
    
}

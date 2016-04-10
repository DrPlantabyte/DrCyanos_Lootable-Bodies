package cyano.lootable;

import cyano.lootable.entities.EntityLootableBody;
import cyano.lootable.graphics.CorpseRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends Proxy{
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        // client-only pre-init code
		RenderingRegistry.registerEntityRenderingHandler(EntityLootableBody.class, new IRenderFactory<EntityLootableBody>() {
			@Override
			public Render<? super EntityLootableBody> createRenderFor(RenderManager rm) {
				return new CorpseRenderer(rm);
			}
		});
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
		// client-only init code
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        // client-only post-init code
    }
    
}

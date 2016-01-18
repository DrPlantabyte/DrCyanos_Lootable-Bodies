package cyano.lootable.graphics;

import cyano.lootable.entities.EntityLootableBody;
import cyano.lootable.graphics.ModelCorpseSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLootableBody extends RenderBiped{

    private static final ResourceLocation skeletonTexture = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    
	public RenderLootableBody(RenderManager rm) {
		super(rm, new ModelCorpseSkeleton(), 0.5f);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor((RendererLivingEntity)this) {
            @Override
            protected void initArmor() {
                this.field_177189_c = new ModelCorpseSkeleton(0.5f, true); // model base (child version?)
                this.field_177186_d = new ModelCorpseSkeleton(1.0f, true); // model base
            }
        });
        
        
    }
    
	
	
    
//    
//    @Override
//    public void func_82422_c() {
//        GlStateManager.translate(0.09375f, 0.1875f, 0.0f);
//    }
//    
	
    @Override protected void preRenderCallback(final EntityLivingBase entity, final float unknown) {
    	//EntityLootableBody e = (EntityLootableBody)entity;
    	GlStateManager.color(0.8f, 0.9f, 0.9f);
    	GlStateManager.rotate(90f, 1f, 0f, 0f);
    	GlStateManager.translate(0f, 1f, 0.125f);
    }
    
    private ResourceLocation getTexture(final EntityLootableBody e) {
        return skeletonTexture;
    }
    
    @Override
    protected ResourceLocation getEntityTexture(final EntityLiving e){
        return this.getTexture((EntityLootableBody)e);
    }
    
    
   
    
}

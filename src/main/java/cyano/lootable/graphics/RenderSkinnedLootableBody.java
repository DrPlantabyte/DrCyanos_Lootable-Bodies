package cyano.lootable.graphics;

import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import cyano.lootable.entities.EntityLootableBody;
import cyano.lootable.graphics.ModelCorpseSkeleton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class RenderSkinnedLootableBody extends RenderBiped{


    private static final ResourceLocation TEXTURE_STEVE = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation TEXTURE_ALEX = new ResourceLocation("textures/entity/alex.png");
	
	public RenderSkinnedLootableBody(RenderManager rm) {
		super(rm, new ModelPlayerCorpse(), 0.5f);
		// TODO: check field names on Forge update
		this.addLayer(new LayerBipedArmor((RendererLivingEntity)this) {
            @Override
            protected void initArmor() { // get/set armor model?
                this.field_177189_c = new ModelCorpseSkeleton(0.5f, true); // model base (child version?)
                this.field_177186_d = new ModelCorpseSkeleton(1.0f, true); // model base
            }
        });
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerCustomHead(((ModelPlayer)(this.getMainModel())).bipedHead));
        
        
    }
    
	
	
    
//    
//    @Override
//    public void func_82422_c() {
//        GlStateManager.translate(0.09375f, 0.1875f, 0.0f);
//    }
//    
	
    @Override protected void preRenderCallback(final EntityLivingBase entity, final float unknown) {
    	//EntityLootableBody e = (EntityLootableBody)entity;
    	GlStateManager.color(0.8f, 0.8f, 0.8f);
    	GlStateManager.rotate(90f, 1f, 0f, 0f);
    	GlStateManager.translate(0f, 1f, 0.125f);
    }
    
    private ResourceLocation getTexture(final EntityLootableBody e) {
    	GameProfile gameprofile = e.getOwner();
    	if(gameprofile == null){
    		//return DefaultPlayerSkin.getDefaultSkinLegacy();
    		return TEXTURE_STEVE;
    	}
    	final Minecraft minecraft = Minecraft.getMinecraft();
        final Map loadSkinFromCache = minecraft.getSkinManager().loadSkinFromCache(gameprofile); // returned map may or may not be typed 
        if (loadSkinFromCache.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            return minecraft.getSkinManager().loadSkin((MinecraftProfileTexture)loadSkinFromCache.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        }
        else {
            //return DefaultPlayerSkin.getDefaultSkin(EntityPlayer.getUUID(gameprofile));
        	if(isSlimSkin(EntityPlayer.getUUID(gameprofile))){
        		return TEXTURE_ALEX;
        	} else {
        		return TEXTURE_STEVE;
        	}
        }
    }
    
    @Override
    protected ResourceLocation getEntityTexture(final EntityLiving e){
        return this.getTexture((EntityLootableBody)e);
    }
    
    
    
    private static boolean isSlimSkin(final UUID userID) {
        return (userID.hashCode() & 0x1) == 0x1;
    }
}

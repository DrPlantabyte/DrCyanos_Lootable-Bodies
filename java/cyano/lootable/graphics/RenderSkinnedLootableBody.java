package cyano.lootable.graphics;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.lootable.entities.EntityLootableBody;

@SideOnly(Side.CLIENT)
public class RenderSkinnedLootableBody extends RenderBiped{


    private static final ResourceLocation TEXTURE_STEVE = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    
    

    public ModelBiped modelArmorChestplate;
    public ModelBiped modelArmor;
    
    
	public RenderSkinnedLootableBody(RenderManager rm) {
		super( new ModelPlayerCorpse(), 0.5f);
	       // 1.7.10 does not support rendering layers here

        this.modelArmorChestplate = new ModelBiped(1.0f){
        	@Override public void setLivingAnimations(EntityLivingBase e, float f1, float f2, float f3){}
        	@Override public void setRotationAngles(final float f1, final float f2, final float f3, final float f4, final float f5, final float f6, final Entity e){}
        	};
        this.modelArmor = new ModelBiped(0.5f){
        	@Override public void setLivingAnimations(EntityLivingBase e, float f1, float f2, float f3){}
        	@Override public void setRotationAngles(final float f1, final float f2, final float f3, final float f4, final float f5, final float f6, final Entity e){}
        	};
        	
    }
    
	
	
    
//    
//    @Override
//    public void func_82422_c() {
//        GlStateManager.translate(0.09375f, 0.1875f, 0.0f);
//    }
//    
	

    private static final Logger logger = LogManager.getLogger();
  
    
    @Override
    public void doRender(final EntityLiving p_doRender_1_, final double p_doRender_2_, final double p_doRender_4_, final double p_doRender_6_, final float p_doRender_8_, final float p_doRender_9_) {
    	copyAngles(((ModelBiped)this.mainModel).bipedLeftArm, modelArmor.bipedLeftArm);
    	copyAngles(((ModelBiped)this.mainModel).bipedRightArm, modelArmor.bipedRightArm);
    	copyAngles(((ModelBiped)this.mainModel).bipedLeftLeg, modelArmor.bipedLeftLeg);
    	copyAngles(((ModelBiped)this.mainModel).bipedRightLeg, modelArmor.bipedRightLeg);
    	copyAngles(((ModelBiped)this.mainModel).bipedLeftArm, modelArmorChestplate.bipedLeftArm);
    	copyAngles(((ModelBiped)this.mainModel).bipedRightArm, modelArmorChestplate.bipedRightArm);
    	copyAngles(((ModelBiped)this.mainModel).bipedLeftLeg, modelArmorChestplate.bipedLeftLeg);
    	copyAngles(((ModelBiped)this.mainModel).bipedRightLeg, modelArmorChestplate.bipedRightLeg);
    	
    	double d3 = p_doRender_4_ - p_doRender_1_.yOffset;
    	doPlayerRender((EntityLootableBody)p_doRender_1_, p_doRender_2_, d3, p_doRender_6_, p_doRender_8_, p_doRender_9_);
    }
    
    private void copyAngles(ModelRenderer src,
			ModelRenderer dest) {
		dest.rotateAngleX = src.rotateAngleX;
		dest.rotateAngleY = src.rotateAngleY;
		dest.rotateAngleZ = src.rotateAngleZ;
		
	}

	public void doLivingRender(final EntityLivingBase p_doRender_1_, final double p_doRender_2_, final double p_doRender_4_, final double p_doRender_6_, final float p_doRender_8_, final float p_doRender_9_) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre(p_doRender_1_, this, p_doRender_2_, p_doRender_4_, p_doRender_6_))) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glDisable(2884);
        
        this.mainModel.onGround = this.renderSwingProgress(p_doRender_1_, p_doRender_9_);
        if (this.renderPassModel != null) {
            this.renderPassModel.onGround = this.mainModel.onGround;
        }
        this.mainModel.isRiding = p_doRender_1_.isRiding();
        if (this.renderPassModel != null) {
            this.renderPassModel.isRiding = this.mainModel.isRiding;
        }
        this.mainModel.isChild = p_doRender_1_.isChild();
        if (this.renderPassModel != null) {
            this.renderPassModel.isChild = this.mainModel.isChild;
        }
        
        
        try {
            float f2 = this.interpolateRotation(p_doRender_1_.prevRenderYawOffset, p_doRender_1_.renderYawOffset, p_doRender_9_);
            f2 = this.interpolateRotation(p_doRender_1_.prevRotationYawHead, p_doRender_1_.rotationYawHead, p_doRender_9_);
            if (p_doRender_1_.isRiding() && p_doRender_1_.ridingEntity instanceof EntityLivingBase) {
                final EntityLivingBase entitylivingbase1 = (EntityLivingBase)p_doRender_1_.ridingEntity;
                f2 = this.interpolateRotation(entitylivingbase1.prevRenderYawOffset, entitylivingbase1.renderYawOffset, p_doRender_9_);
                float f3 = MathHelper.wrapAngleTo180_float(f2 - f2);
                if (f3 < -85.0f) {
                    f3 = -85.0f;
                }
                if (f3 >= 85.0f) {
                    f3 = 85.0f;
                }
                f2 -= f3;
                if (f3 * f3 > 2500.0f) {
                    f2 += f3 * 0.2f;
                }
            }
            final float f4 = p_doRender_1_.prevRotationPitch + (p_doRender_1_.rotationPitch - p_doRender_1_.prevRotationPitch) * p_doRender_9_;
            this.renderLivingAt(p_doRender_1_, p_doRender_2_, p_doRender_4_, p_doRender_6_);
            float f3 = this.handleRotationFloat(p_doRender_1_, p_doRender_9_);
            this.rotateCorpse(p_doRender_1_, f3, f2, p_doRender_9_);
            final float f5 = 0.0625f;
            GL11.glEnable(32826);
            GL11.glScalef(-1.0f, -1.0f, 1.0f);
            this.preRenderCallback(p_doRender_1_, p_doRender_9_);
            GL11.glTranslatef(0.0f, -24.0f * f5 - 0.0078125f, 0.0f);
            float f6 = p_doRender_1_.prevLimbSwingAmount + (p_doRender_1_.limbSwingAmount - p_doRender_1_.prevLimbSwingAmount) * p_doRender_9_;
            float f7 = p_doRender_1_.limbSwing - p_doRender_1_.limbSwingAmount * (1.0f - p_doRender_9_);
            if (p_doRender_1_.isChild()) {
                f7 *= 3.0f;
            }
            if (f6 > 1.0f) {
                f6 = 1.0f;
            }
            GL11.glEnable(3008);
            
            // rotate face-down
            GL11.glColor3f(0.8f, 0.8f, 0.8f);
            GL11.glRotatef(90f, 1f, 0f, 0f);
            GL11.glTranslatef(0f, -0.5f, -1.375f);
            
            this.mainModel.setLivingAnimations(p_doRender_1_, f7, f6, p_doRender_9_);
            this.renderModel(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
            for (int i = 0; i < 4; ++i) {
                final int j = this.shouldRenderPass(p_doRender_1_, i, p_doRender_9_);
                if (j > 0) {
                    this.renderPassModel.setLivingAnimations(p_doRender_1_, f7, f6, p_doRender_9_);
                    this.renderPassModel.render(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
                    if ((j & 0xF0) == 0x10) {
                        this.func_82408_c(p_doRender_1_, i, p_doRender_9_);
                        this.renderPassModel.render(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
                    }
                    if ((j & 0xF) == 0xF) {
                        final float f8 = p_doRender_1_.ticksExisted + p_doRender_9_;
                        this.bindTexture(RES_ITEM_GLINT);
                        GL11.glEnable(3042);
                        final float f9 = 0.5f;
                        GL11.glColor4f(f9, f9, f9, 1.0f);
                        GL11.glDepthFunc(514);
                        GL11.glDepthMask(false);
                        for (int k = 0; k < 2; ++k) {
                            GL11.glDisable(2896);
                            final float f10 = 0.76f;
                            GL11.glColor4f(0.5f * f10, 0.25f * f10, 0.8f * f10, 1.0f);
                            GL11.glBlendFunc(768, 1);
                            GL11.glMatrixMode(5890);
                            GL11.glLoadIdentity();
                            final float f11 = f8 * (0.001f + k * 0.003f) * 20.0f;
                            final float f12 = 0.33333334f;
                            GL11.glScalef(f12, f12, f12);
                            GL11.glRotatef(30.0f - k * 60.0f, 0.0f, 0.0f, 1.0f);
                            GL11.glTranslatef(0.0f, f11, 0.0f);
                            GL11.glMatrixMode(5888);
                            this.renderPassModel.render(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
                        }
                        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                        GL11.glMatrixMode(5890);
                        GL11.glDepthMask(true);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(5888);
                        GL11.glEnable(2896);
                        GL11.glDisable(3042);
                        GL11.glDepthFunc(515);
                    }
                    GL11.glDisable(3042);
                    GL11.glEnable(3008);
                }
            }
            GL11.glDepthMask(true);
            this.renderEquippedItems(p_doRender_1_, p_doRender_9_);
            final float f13 = p_doRender_1_.getBrightness(p_doRender_9_);
            final int j = this.getColorMultiplier(p_doRender_1_, f13, p_doRender_9_);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(3553);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            if ((j >> 24 & 0xFF) > 0 || p_doRender_1_.hurtTime > 0 || p_doRender_1_.deathTime > 0) {
                GL11.glDisable(3553);
                GL11.glDisable(3008);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDepthFunc(514);
                if (p_doRender_1_.hurtTime > 0 || p_doRender_1_.deathTime > 0) {
                    GL11.glColor4f(f13, 0.0f, 0.0f, 0.4f);
                    this.mainModel.render(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
                    for (int l = 0; l < 4; ++l) {
                        if (this.inheritRenderPass(p_doRender_1_, l, p_doRender_9_) >= 0) {
                            GL11.glColor4f(f13, 0.0f, 0.0f, 0.4f);
                            this.renderPassModel.render(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
                        }
                    }
                }
                if ((j >> 24 & 0xFF) > 0) {
                    final float f8 = (j >> 16 & 0xFF) / 255.0f;
                    final float f9 = (j >> 8 & 0xFF) / 255.0f;
                    final float f14 = (j & 0xFF) / 255.0f;
                    final float f10 = (j >> 24 & 0xFF) / 255.0f;
                    GL11.glColor4f(f8, f9, f14, f10);
                    this.mainModel.render(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
                    for (int i2 = 0; i2 < 4; ++i2) {
                        if (this.inheritRenderPass(p_doRender_1_, i2, p_doRender_9_) >= 0) {
                            GL11.glColor4f(f8, f9, f14, f10);
                            this.renderPassModel.render(p_doRender_1_, f7, f6, f3, f2 - f2, f4, f5);
                        }
                    }
                }
                GL11.glDepthFunc(515);
                GL11.glDisable(3042);
                GL11.glEnable(3008);
                GL11.glEnable(3553);
            }
            GL11.glDisable(32826);
        }
        catch (Exception exception) {
           logger.error("Couldn't render entity", (Throwable)exception);
        }
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glEnable(3553);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glEnable(2884);
        
        
        GL11.glPopMatrix();
        this.passSpecialRender(p_doRender_1_, p_doRender_2_, p_doRender_4_, p_doRender_6_);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(p_doRender_1_, this, p_doRender_2_, p_doRender_4_, p_doRender_6_));
    }
    
    protected void renderModel(final EntityLivingBase p_renderModel_1_, final float p_renderModel_2_, final float p_renderModel_3_, final float p_renderModel_4_, final float p_renderModel_5_, final float p_renderModel_6_, final float p_renderModel_7_) {
    	this.bindTexture(this.getEntityTexture(p_renderModel_1_));
        if (!p_renderModel_1_.isInvisible()) {
            this.mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
        }
        else if (!p_renderModel_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer)) {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.15f);
            GL11.glDepthMask(false);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glAlphaFunc(516, 0.003921569f);
            this.mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
            GL11.glDisable(3042);
            GL11.glAlphaFunc(516, 0.1f);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }
        else {
            this.mainModel.setRotationAngles(p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_, p_renderModel_1_);
        }
    }


    public void doPlayerRender(final EntityLootableBody body, final double p_doRender_2_, final double p_doRender_4_, final double p_doRender_6_, final float p_doRender_8_, final float p_doRender_9_) {
        
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        final ItemStack itemstack = body.getHeldItem();
        final ModelBiped modelArmorChestplate = this.modelArmorChestplate;
        final ModelBiped modelArmor = this.modelArmor;
        final ModelBiped modelBipedMain = this.modelBipedMain;
        final boolean heldItemRight;
        final boolean b = heldItemRight = (((itemstack != null) ? 1 : 0) != 0);
        modelBipedMain.heldItemRight = (b ? 1 : 0);
        modelArmor.heldItemRight = (b ? 1 : 0);
        modelArmorChestplate.heldItemRight = (heldItemRight ? 1 : 0);
        if (itemstack != null ) {
            final EnumAction enumaction = itemstack.getItemUseAction();
            if (enumaction == EnumAction.block) {
                final ModelBiped modelArmorChestplate2 = this.modelArmorChestplate;
                final ModelBiped modelArmor2 = this.modelArmor;
                final ModelBiped modelBipedMain2 = this.modelBipedMain;
                final int heldItemRight2 = 3;
                modelBipedMain2.heldItemRight = heldItemRight2;
                modelArmor2.heldItemRight = heldItemRight2;
                modelArmorChestplate2.heldItemRight = heldItemRight2;
            }
            else if (enumaction == EnumAction.bow) {
                final ModelBiped modelArmorChestplate3 = this.modelArmorChestplate;
                final ModelBiped modelArmor3 = this.modelArmor;
                final ModelBiped modelBipedMain3 = this.modelBipedMain;
                final boolean aimedBow = true;
                modelBipedMain3.aimedBow = aimedBow;
                modelArmor3.aimedBow = aimedBow;
                modelArmorChestplate3.aimedBow = aimedBow;
            }
        }
        final ModelBiped modelArmorChestplate4 = this.modelArmorChestplate;
        final ModelBiped modelArmor4 = this.modelArmor;
        final ModelBiped modelBipedMain4 = this.modelBipedMain;
        final boolean sneaking = body.isSneaking();
        modelBipedMain4.isSneak = sneaking;
        modelArmor4.isSneak = sneaking;
        modelArmorChestplate4.isSneak = sneaking;
        double d3 = p_doRender_4_ - body.yOffset;
        //super.doRender(body, p_doRender_2_, d3, p_doRender_6_, p_doRender_8_, p_doRender_9_);
        doLivingRender(body, p_doRender_2_, d3, p_doRender_6_, p_doRender_8_, p_doRender_9_);
        final ModelBiped modelArmorChestplate5 = this.modelArmorChestplate;
        final ModelBiped modelArmor5 = this.modelArmor;
        final ModelBiped modelBipedMain5 = this.modelBipedMain;
        final boolean aimedBow2 = false;
        modelBipedMain5.aimedBow = aimedBow2;
        modelArmor5.aimedBow = aimedBow2;
        modelArmorChestplate5.aimedBow = aimedBow2;
        final ModelBiped modelArmorChestplate6 = this.modelArmorChestplate;
        final ModelBiped modelArmor6 = this.modelArmor;
        final ModelBiped modelBipedMain6 = this.modelBipedMain;
        final boolean isSneak = false;
        modelBipedMain6.isSneak = isSneak;
        modelArmor6.isSneak = isSneak;
        modelArmorChestplate6.isSneak = isSneak;
        final ModelBiped modelArmorChestplate7 = this.modelArmorChestplate;
        final ModelBiped modelArmor7 = this.modelArmor;
        final ModelBiped modelBipedMain7 = this.modelBipedMain;
        final boolean heldItemRight3 = false;
        modelBipedMain7.heldItemRight = (heldItemRight3 ? 1 : 0);
        modelArmor7.heldItemRight = (heldItemRight3 ? 1 : 0);
        modelArmorChestplate7.heldItemRight = (heldItemRight3 ? 1 : 0);
       
    }
    
    
    @Override protected int shouldRenderPass(final EntityLivingBase p_shouldRenderPass_1_, final int p_shouldRenderPass_2_, final float p_shouldRenderPass_3_) {
        final ItemStack itemstack = ((EntityLootableBody)p_shouldRenderPass_1_).armorItemInSlot(3 - p_shouldRenderPass_2_);
       if (itemstack != null) {
            final Item item = itemstack.getItem();
            if (item instanceof ItemArmor) {
                final ItemArmor itemarmor = (ItemArmor)item;
                this.bindTexture(RenderBiped.getArmorResource(p_shouldRenderPass_1_, itemstack, p_shouldRenderPass_2_, null));
                ModelBiped modelbiped = (p_shouldRenderPass_2_ == 2) ? this.modelArmor : this.modelArmorChestplate;
                modelbiped.bipedHead.showModel = (p_shouldRenderPass_2_ == 0);
                modelbiped.bipedHeadwear.showModel = (p_shouldRenderPass_2_ == 0);
                modelbiped.bipedBody.showModel = (p_shouldRenderPass_2_ == 1 || p_shouldRenderPass_2_ == 2);
                modelbiped.bipedRightArm.showModel = (p_shouldRenderPass_2_ == 1);
                modelbiped.bipedLeftArm.showModel = (p_shouldRenderPass_2_ == 1);
                modelbiped.bipedRightLeg.showModel = (p_shouldRenderPass_2_ == 2 || p_shouldRenderPass_2_ == 3);
                modelbiped.bipedLeftLeg.showModel = (p_shouldRenderPass_2_ == 2 || p_shouldRenderPass_2_ == 3);
                modelbiped = ForgeHooksClient.getArmorModel(p_shouldRenderPass_1_, itemstack, p_shouldRenderPass_2_, modelbiped);
                this.setRenderPassModel(modelbiped);
                modelbiped.onGround = this.mainModel.onGround;
                modelbiped.isRiding = this.mainModel.isRiding;
                modelbiped.isChild = this.mainModel.isChild;
                final int j = itemarmor.getColor(itemstack);
                if (j != -1) {
                    final float f1 = (j >> 16 & 0xFF) / 255.0f;
                    final float f2 = (j >> 8 & 0xFF) / 255.0f;
                    final float f3 = (j & 0xFF) / 255.0f;
                    GL11.glColor3f(f1, f2, f3);
                    if (itemstack.isItemEnchanted()) {
                        return 31;
                    }
                    return 16;
                }
                else {
                    GL11.glColor3f(1.0f, 1.0f, 1.0f);
                    if (itemstack.isItemEnchanted()) {
                        return 15;
                    }
                    return 1;
                }
            }
        }
        return -1;
    }
    
	private ResourceLocation getTexture(final EntityLootableBody e) {
    	GameProfile gameprofile = e.getOwner();
    	if(gameprofile == null){
    		//return DefaultPlayerSkin.getDefaultSkinLegacy();
    		return TEXTURE_STEVE;
    	}
    	final Minecraft minecraft = Minecraft.getMinecraft();
        final Map cache = minecraft.func_152342_ad().func_152788_a(e.getOwner());
//        System.out.println(gameprofile.getName()+" cache: (size = "+cache.keySet().size()+")");
//        for(Object key : cache.keySet()){
//        	System.out.println(key+"="+cache.get(key));
//        }
        if (cache.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            return minecraft.func_152342_ad().func_152792_a(
            		(MinecraftProfileTexture)cache.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        } else {
            //return DefaultPlayerSkin.getDefaultSkin(EntityPlayer.getUUID(gameprofile));
        	return TEXTURE_STEVE;
        	
        }
    }
	
	 private float interpolateRotation(final float p_interpolateRotation_1_, final float p_interpolateRotation_2_, final float p_interpolateRotation_3_) {
	        float f3;
	        for (f3 = p_interpolateRotation_2_ - p_interpolateRotation_1_; f3 < -180.0f; f3 += 360.0f) {}
	        while (f3 >= 180.0f) {
	            f3 -= 360.0f;
	        }
	        return p_interpolateRotation_1_ + p_interpolateRotation_3_ * f3;
	    }
    
    @Override
    protected ResourceLocation getEntityTexture(final EntityLiving e){
        return this.getTexture((EntityLootableBody)e);
    }
    
    
    protected ResourceLocation getEntityTexture(final EntityLivingBase e){
        return this.getTexture((EntityLootableBody)e);
    }
    
    
    
}

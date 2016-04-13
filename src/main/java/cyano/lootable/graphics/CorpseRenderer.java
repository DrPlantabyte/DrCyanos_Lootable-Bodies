package cyano.lootable.graphics;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import cyano.lootable.entities.EntityLootableBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPigZombie;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.Map;

/**
 * Created by Chris on 4/10/2016.
 */


@SideOnly(Side.CLIENT)
public class CorpseRenderer extends RenderLivingBase<EntityLootableBody> {

	public CorpseRenderer(RenderManager renderManagerIn) {
		super(renderManagerIn,  new ModelPlayer(0.0F, true), 0.5F);
		this.addLayer(new LayerBipedArmor(this));
		this.addLayer(new LayerHeldItem(this));
		this.addLayer(new LayerArrow(this));
		this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
		RenderPlayer k;
		RenderPigZombie j;
	}

	public ModelPlayer getMainModel()
	{
		return (ModelPlayer)super.getMainModel();
	}
	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 *
	 * @param entity The entity to be rendered
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityLootableBody entity) {
		net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer l;


		GameProfile profile = entity.getGameProfile();
		if (profile != null && profile.getId() != null) {
			final Minecraft minecraft = Minecraft.getMinecraft();
			final Map loadSkinFromCache = minecraft.getSkinManager().loadSkinFromCache(profile); // returned map may or may not be typed
			if (loadSkinFromCache.containsKey(MinecraftProfileTexture.Type.SKIN)) {
				return minecraft.getSkinManager().loadSkin((MinecraftProfileTexture) loadSkinFromCache.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
			} else {
				return DefaultPlayerSkin.getDefaultSkin(EntityPlayer.getUUID(profile));
			}
		}

		return DefaultPlayerSkin.getDefaultSkinLegacy();
	}

	@Override
	public void doRender(EntityLootableBody entity, double x, double y, double z, float yaw, float partialTick) {
		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<EntityLootableBody>(entity, this, x, y, z)))
			return;

		// TODO: pose arms and legs
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		this.mainModel.swingProgress = 0;
		boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
		this.mainModel.isRiding = shouldSit;
		this.mainModel.isChild = entity.isChild();

		try {
			float rotationInterpolation = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTick);
			float headTotationInterpolation = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTick);
			float headYaw = 0;

//			if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase) {
//				EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
//				rotationInterpolation = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTick);
//				headYaw = headTotationInterpolation - rotationInterpolation;
//				float correctedRotation = MathHelper.wrapAngleTo180_float(headYaw);
//
//				if (correctedRotation < -85.0F) {
//					correctedRotation = -85.0F;
//				}
//
//				if (correctedRotation >= 85.0F) {
//					correctedRotation = 85.0F;
//				}
//
//				rotationInterpolation = headTotationInterpolation - correctedRotation;
//
//				if (correctedRotation * correctedRotation > 2500.0F) {
//					rotationInterpolation += correctedRotation * 0.2F;
//				}
//			}

			float headPitch = 0;//entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick;
			this.renderLivingAt(entity, x, y, z);
			float age = 0.0F;
			this.rotateCorpse(entity, age, rotationInterpolation, partialTick);
			float scale = this.prepareScale(entity, 0F);
			float armSwingAmount = 0.0F;
			float armSwing = 0.0F;


			GlStateManager.enableAlpha();
			this.mainModel.setLivingAnimations(entity, armSwing, armSwingAmount, 0);
			this.mainModel.setRotationAngles(armSwing, armSwingAmount, age, headYaw, headPitch, scale, entity);

			if (this.renderOutlines) {
				boolean flag1 = this.setScoreTeamColor(entity);
				GlStateManager.enableColorMaterial();
				GlStateManager.enableOutlineMode(this.getTeamColor(entity));

				if (!this.renderMarker) {
					this.renderModel(entity, armSwing, armSwingAmount, age, headYaw, headPitch, scale);
				}


				this.renderLayers(entity, armSwing, armSwingAmount, partialTick, age, headYaw, headPitch, scale);


				GlStateManager.disableOutlineMode();
				GlStateManager.disableColorMaterial();

				if (flag1) {
					this.unsetScoreTeamColor();
				}
			} else {
				boolean flag = this.setDoRenderBrightness(entity, partialTick);
				this.renderModel(entity, armSwing, armSwingAmount, age, headYaw, headPitch, scale);

				if (flag) {
					this.unsetBrightness();
				}

				GlStateManager.depthMask(true);


				this.renderLayers(entity, armSwing, armSwingAmount, partialTick, age, headYaw, headPitch, scale);

			}

			GlStateManager.disableRescaleNormal();
		} catch (Exception ex) {
			FMLLog.log(Level.ERROR, ex, "Couldn\'t render entity");
		}

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
		if (!this.renderOutlines) {
			this.renderName(entity, x, y, z);
		}
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<EntityLootableBody>(entity, this, x, y, z));
	}


	@Override
	protected void renderLivingAt(EntityLootableBody e, double x, double y, double z)
	{
		super.renderLivingAt(e,x,y,z); // translation
		GlStateManager.rotate(90,1F,0F,0F); // face-down
		GlStateManager.translate(0F, -0.85F, -0.125F); // center
	}

}

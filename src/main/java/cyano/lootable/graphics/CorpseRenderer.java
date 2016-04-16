package cyano.lootable.graphics;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import cyano.lootable.LootableBodies;
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

	protected final ModelPlayer thickArmsModel;
	protected final ModelPlayer thinArmsModel;

	public CorpseRenderer(RenderManager renderManagerIn) {
		super(renderManagerIn,  new ModelPlayer(0.0F, true), 0.5F);
		thinArmsModel = (ModelPlayer)this.mainModel;
		thickArmsModel =  new ModelPlayer(0.0F, false);
		//
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

	public void setModel(boolean thinArms){
		if(thinArms){
			this.mainModel = this.thinArmsModel;
		} else {
			this.mainModel = this.thickArmsModel;
		}
	}
	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 *
	 * @param entity The entity to be rendered
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityLootableBody entity) {


		GameProfile profile = entity.getGameProfile();
		if (profile != null && profile.getId() != null) {
			return getSkin(profile);
		}

		return DefaultPlayerSkin.getDefaultSkinLegacy();
	}

	public static ResourceLocation getSkin(GameProfile profile) {
		final Minecraft minecraft = Minecraft.getMinecraft();
		final Map loadSkinFromCache = minecraft.getSkinManager().loadSkinFromCache(profile); // returned map may or may not be typed
		if (loadSkinFromCache.containsKey(MinecraftProfileTexture.Type.SKIN)) {
			ResourceLocation skin = minecraft.getSkinManager().loadSkin((MinecraftProfileTexture) loadSkinFromCache.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
			return skin;
		} else {
			return DefaultPlayerSkin.getDefaultSkin(profile.getId());
		}
	}


	@Override
	public void doRender(EntityLootableBody entity, double x, double y, double z, float yaw, float partialTick) {
		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<EntityLootableBody>(entity, this, x, y, z)))
			return;

		this.setModel(entity.useThinArms());

		// render the model
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
			// sigh, unable to pose the model

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
		if (!this.renderOutlines && LootableBodies.displayNameTag) {
			this.renderName(entity, x, y, z);
		}

		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<EntityLootableBody>(entity, this, x, y, z));

		this.setModel(true);
	}


	@Override
	protected void renderLivingAt(EntityLootableBody e, double x, double y, double z)
	{
		super.renderLivingAt(e,x,y,z); // translation
		GlStateManager.rotate(90,1F,0F,0F); // face-down
		GlStateManager.rotate(e.getRotation(),0F,0F,1F); // turn
		GlStateManager.translate(0F, -0.85F, -0.125F); // center
	}

}

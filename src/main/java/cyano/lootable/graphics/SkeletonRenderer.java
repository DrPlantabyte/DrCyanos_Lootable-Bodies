package cyano.lootable.graphics;

import cyano.lootable.LootableBodies;
import cyano.lootable.entities.EntityLootableBody;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

/**
 * Created by Chris on 4/10/2016.
 */


@SideOnly(Side.CLIENT)
public class SkeletonRenderer extends RenderLivingBase<EntityLootableBody> {

	private static final ResourceLocation skeletonTexture = new ResourceLocation("textures/entity/skeleton/skeleton.png");

	public SkeletonRenderer(RenderManager renderManagerIn) {
		super(renderManagerIn,  new ModelSkeleton(), 0.5F);
		//
		this.addLayer(new LayerHeldItem(this));
		this.addLayer(new LayerBipedArmor(this)
		{
			protected void initArmor()
			{
				this.modelLeggings = new ModelSkeleton(0.5F, true);
				this.modelArmor = new ModelSkeleton(1.0F, true);
			}
		});
	}


	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 *
	 * @param entity The entity to be rendered
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityLootableBody entity) {
		return skeletonTexture;
	}


	@Override
	public void doRender(EntityLootableBody entity, double x, double y, double z, float yaw, float partialTick) {
		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<EntityLootableBody>(entity, this, x, y, z)))
			return;

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

	}


	@Override
	protected void renderLivingAt(EntityLootableBody e, double x, double y, double z)
	{
		super.renderLivingAt(e,x,y,z); // translation
		GlStateManager.rotate(90,1F,0F,0F); // face-down
		GlStateManager.translate(0F, -0.85F, -0.125F); // center
	}

}

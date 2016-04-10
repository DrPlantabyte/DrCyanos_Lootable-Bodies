package cyano.lootable.graphics;

import com.mojang.authlib.GameProfile;
import cyano.lootable.entities.EntityLootableBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
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
	 * @param entity
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityLootableBody entity) {
		net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer l;


		ResourceLocation skinRsrc = DefaultPlayerSkin.getDefaultSkinLegacy();
		GameProfile profile = entity.getGameProfile();
		if (profile != null && profile.getId() != null) {

			NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(profile.getId());
			FMLLog.info("Player Info = %s",playerInfo);// TODO: remove
			if(playerInfo != null){
				skinRsrc = playerInfo.getLocationSkin();
				FMLLog.info("Skin resource = %s");// TODO: remove
			} else {
				skinRsrc = DefaultPlayerSkin.getDefaultSkin(profile.getId());
			}

		}

		return skinRsrc;
	}
}

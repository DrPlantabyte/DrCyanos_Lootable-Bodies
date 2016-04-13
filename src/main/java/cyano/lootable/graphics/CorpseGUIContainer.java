package cyano.lootable.graphics;

import cyano.lootable.LootableBodies;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Created by Chris on 4/12/2016.
 */
@SideOnly(Side.CLIENT)
public class CorpseGUIContainer extends net.minecraft.client.gui.inventory.GuiContainer{

	private final ResourceLocation image = new ResourceLocation(LootableBodies.MODID+":textures/gui/corpse.png");

	public CorpseGUIContainer(InventoryPlayer playerItems, IInventory entity) {
		super(new CorpseContainer(playerItems,entity));
		this.xSize = 176;
		this.ySize = 222;
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 *
	 * @param partialTicks How far into the current tick the game is, with 0.0 being the start of the tick and 1.0 being
	 *                     the end.
	 * @param mouseX       Mouse x coordinate
	 * @param mouseY       Mouse y coordinate
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.mc.renderEngine.bindTexture(image);
		this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize); // x, y, textureOffsetX, textureOffsetY, width, height)
	}
}

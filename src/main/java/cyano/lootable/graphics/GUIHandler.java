package cyano.lootable.graphics;

import cyano.lootable.entities.EntityLootableBody;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Chris on 4/12/2016.
 */
public class GUIHandler implements IGuiHandler {
	private static final AtomicInteger guiIDCounter = new AtomicInteger(1);

	private GUIHandler() {
		// using singleton instantiation
	}

	private static final Lock initLock = new ReentrantLock();
	private static GUIHandler instance = null;

	/**
	 * Gets a singleton instance of MachineGUIRegistry
	 *
	 * @return A global instance of MachineGUIRegistry
	 */
	public static GUIHandler getInstance() {
		if (instance == null) {
			initLock.lock();
			try {
				if (instance == null) {
					// thread-safe singleton instantiation
					instance = new GUIHandler();
				}
			} finally {
				initLock.unlock();
			}
		}
		return instance;
	}

	/**
	 * Implementation of net.minecraftforge.fml.common.network.IGuiHandler
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int id, EntityPlayer player, World world,
									  int x, int y, int z) {
		switch (id) {
			case 0:
				Entity e = world.getEntityByID(x);
				if (e instanceof EntityLootableBody)
					return new CorpseGUIContainer(player.inventory, (IInventory) e);
		}
		return null;
	}

	/**
	 * Implementation of net.minecraftforge.fml.common.network.IGuiHandler
	 */
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world,
									  int x, int y, int z) {
		switch (id) {
			case 0:
				Entity e = world.getEntityByID(x);
				if (e instanceof EntityLootableBody)
					return new CorpseContainer(player.inventory, (IInventory) e);
		}
		return null;
	}
}

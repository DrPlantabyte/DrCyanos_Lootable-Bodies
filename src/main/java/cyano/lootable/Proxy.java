package cyano.lootable;


import net.minecraftforge.fml.common.event.*;


public class Proxy {

    public void preInit(FMLPreInitializationEvent e) {
    	// do nothing
    }

    public void init(FMLInitializationEvent e) {
    	// do nothing
    }

    public void postInit(FMLPostInitializationEvent e) {
    	// do nothing
    }
    
    public int getArmorRenderIndex(String armorSet){
		return 0; // Server don't care!
	}
}

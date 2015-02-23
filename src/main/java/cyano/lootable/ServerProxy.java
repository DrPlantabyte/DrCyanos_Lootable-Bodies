package cyano.lootable;

import net.minecraftforge.fml.common.event.*;


public class ServerProxy extends Proxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        // server-only pre-init code
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        // server-only init code
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        // server-only post-init code
    }

}


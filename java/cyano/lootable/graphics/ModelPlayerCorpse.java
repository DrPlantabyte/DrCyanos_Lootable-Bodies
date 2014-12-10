package cyano.lootable.graphics;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPlayerCorpse extends ModelPlayer{

	public ModelPlayerCorpse(){
        super(0.0f, false);
    }
	
	public ModelPlayerCorpse(final float f, final boolean b) {
        super(f, b);
	}
	

    @Override
    public void setLivingAnimations(final EntityLivingBase e, final float f1, final float f2, final float f3) {
        // do nothing
    }
    
   
    
    @Override
    public void setRotationAngles(final float f1, final float f2, final float f3, final float f4, final float f5, final float f6, final Entity e) {
    	//super.setRotationAngles(f1, f2, f3, f4, f5, f6, e);
    	super.setRotationAngles(0, 0, f3, f4, f5, f6, e);
    	this.bipedRightLeg.rotateAngleZ = 0.2f;
    	this.bipedLeftLeg.rotateAngleZ = -0.2f;
    	this.bipedRightArm.rotateAngleZ = 0.3f;
    	this.bipedLeftArm.rotateAngleZ = -0.3f;
    	this.bipedRightArm.rotateAngleY = 1.25f;
    	this.bipedLeftArm.rotateAngleY = -0.5f;
    	this.bipedRightArm.rotateAngleX = 0f; 
    	this.bipedLeftArm.rotateAngleX = 0f;
    	ModelBase.copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
        ModelBase.copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
        ModelBase.copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
        ModelBase.copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
        ModelBase.copyModelAngles(this.bipedBody, this.bipedBodyWear);
    }
}

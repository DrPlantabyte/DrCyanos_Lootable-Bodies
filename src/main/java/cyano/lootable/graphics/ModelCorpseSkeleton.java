package cyano.lootable.graphics;

import net.minecraftforge.fml.relauncher.*;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.*;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelCorpseSkeleton extends ModelZombie
{
    public ModelCorpseSkeleton() {
        this(0.0f, false);
    }
    
    public ModelCorpseSkeleton(final float f, final boolean b) {
        super(f, 0.0f, 64, 32);
        if (!b) {
            (this.bipedRightArm = new ModelRenderer(this, 40, 16)).addBox(-1.0f, -2.0f, -1.0f, 2, 12, 2, f);
            this.bipedRightArm.setRotationPoint(-5.0f, 2.0f, 0.0f);
            this.bipedLeftArm = new ModelRenderer(this, 40, 16);
            this.bipedLeftArm.mirror = true;
            this.bipedLeftArm.addBox(-1.0f, -2.0f, -1.0f, 2, 12, 2, f);
            this.bipedLeftArm.setRotationPoint(5.0f, 2.0f, 0.0f);
            (this.bipedRightLeg = new ModelRenderer(this, 0, 16)).addBox(-1.0f, 0.0f, -1.0f, 2, 12, 2, f);
            this.bipedRightLeg.setRotationPoint(-2.0f, 12.0f, 0.0f);
            this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
            this.bipedLeftLeg.mirror = true;
            this.bipedLeftLeg.addBox(-1.0f, 0.0f, -1.0f, 2, 12, 2, f);
            this.bipedLeftLeg.setRotationPoint(2.0f, 12.0f, 0.0f);
        }
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
    }
}
package io.github.aaronateataco.petsandpals.rendering.vanilla.sulfur_cube;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Geometry copied from the real vanilla {@code SulfurCubeModel} (26.2+, decompiled since
 * it doesn't exist in this version's client jar) - a translucent 18x18x18 outer shell and
 * a 16x16x16 inner core, both on a 128x128 texture, single cube each. One class serves
 * both, same as vanilla, baked from a different layer location per instance.
 */
public class PetSulfurCubeModel extends EntityModel<SlimeRenderState> {
    public PetSulfurCubeModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
    }

    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0f, -9.0f, -9.0f, 18.0f, 18.0f, 18.0f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 36).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 128);
    }
}

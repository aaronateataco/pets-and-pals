package com.jeff.pets.rendering.custom.minecraft_earth.sheep.horned_sheep;

import com.jeff.pets.rendering.vanilla.sheep.ClientSheepModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import org.jetbrains.annotations.NotNull;

public class HornedSheepModel extends ClientSheepModel {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart right_horn;
    private final ModelPart left_horn;

    public HornedSheepModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg0 = root.getChild("left_hind_leg");
        this.leg1 = root.getChild("right_hind_leg");
        this.leg2 = root.getChild("left_front_leg");
        this.leg3 = root.getChild("right_front_leg");
        this.right_horn = head.getChild("right_horn");
        this.left_horn = head.getChild("left_horn");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(22, 10).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, -8.0F));

        PartDefinition left_horn = head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(0, 45).addBox(-11.0F, 12.0F, -18.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 32).addBox(-11.0F, 9.0F, -15.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -13.0F, 10.0F));

        PartDefinition right_horn = head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, 9.0F, -15.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(14, 45).addBox(-1.0F, 12.0F, -18.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -13.0F, 10.0F));

        PartDefinition leg0 = partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 12.0F, 7.0F));

        PartDefinition leg1 = partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 12.0F, 7.0F));

        PartDefinition leg2 = partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 12.0F, -5.0F));

        PartDefinition leg3 = partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 12.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
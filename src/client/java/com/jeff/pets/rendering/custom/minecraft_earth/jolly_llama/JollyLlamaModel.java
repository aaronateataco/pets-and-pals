package com.jeff.pets.rendering.custom.minecraft_earth.jolly_llama;

import com.jeff.pets.rendering.vanilla.llama.ClientLlamaModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class JollyLlamaModel extends ClientLlamaModel {

    private final ModelPart right_chest;
    private final ModelPart left_chest;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart left_hind_leg;
    private final ModelPart right_hind_leg;
    private final ModelPart left_front_leg;
    private final ModelPart right_front_leg;

    public JollyLlamaModel(ModelPart root) {
        super(root);
        this.right_chest = root.getChild("right_chest");
        this.left_chest = root.getChild("left_chest");
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.left_hind_leg = root.getChild("left_hind_leg");
        this.right_hind_leg = root.getChild("right_hind_leg");
        this.left_front_leg = root.getChild("left_front_leg");
        this.right_front_leg = root.getChild("right_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition right_chest = partdefinition.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 10.0F, 6.0F));

        PartDefinition left_chest = partdefinition.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 10.0F, 6.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 14).addBox(-4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(17, 0).addBox(-4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 0).addBox(1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(96, 27).addBox(-4.0F, -10.0F, -6.1F, 8.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(96, 5).addBox(4.0F, -31.0F, -3.0F, 8.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, -6.0F));
        PartDefinition left_antlers_r1 = head.addOrReplaceChild("left_antlers_r1", CubeListBuilder.create().texOffs(96, 5).addBox(-1.0F, -15.0F, 1.0F, 8.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -16.0F, -2.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition left_reins_r1 = head.addOrReplaceChild("left_reins_r1", CubeListBuilder.create().texOffs(110, 28).addBox(-4.0F, -2.0F, 1.1F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -7.0F, -2.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition right_reins_r1 = head.addOrReplaceChild("right_reins_r1", CubeListBuilder.create().texOffs(110, 28).addBox(-4.0F, -2.0F, 0.9F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -7.0F, -2.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(93, 48).addBox(-6.0F, -5.0F, 3.1F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(82, 39).addBox(6.1F, -5.0F, -6.0F, 0.0F, 11.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(82, 39).addBox(-6.5F, -5.0F, -6.0F, 0.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition left_hind_leg = partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 10.0F, 6.0F));

        PartDefinition right_hind_leg = partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 10.0F, 6.0F));

        PartDefinition left_front_leg = partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 10.0F, -5.0F));

        PartDefinition right_front_leg = partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 10.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
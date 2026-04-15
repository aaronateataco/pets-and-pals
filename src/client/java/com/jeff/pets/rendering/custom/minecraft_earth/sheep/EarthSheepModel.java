package com.jeff.pets.rendering.custom.minecraft_earth.sheep;

import com.jeff.pets.rendering.vanilla.sheep.ClientSheepModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.SheepRenderState;

public class EarthSheepModel extends ClientSheepModel {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart leg0;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;

	public EarthSheepModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.leg0 = root.getChild("right_hind_leg");
		this.leg1 = root.getChild("left_hind_leg");
		this.leg2 = root.getChild("right_front_leg");
		this.leg3 = root.getChild("left_front_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(22, 10).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, -8.0F));

		PartDefinition leg0 = partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 12.0F, 7.0F));

		PartDefinition leg1 = partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 12.0F, 7.0F));

		PartDefinition leg2 = partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 12.0F, -5.0F));

		PartDefinition leg3 = partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 12.0F, -5.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}
}
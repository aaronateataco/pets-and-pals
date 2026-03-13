package com.jeff.pets.vanilla.cat;

import com.mojang.blaze3d.vertex.PoseStack;
import kotlin._Assertions;
import net.minecraft.client.model.animal.feline.CatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CatRenderState;

import static com.jeff.pets.Pet.CONFIG;

public class ClientCatModel extends CatModel {
    public ClientCatModel(ModelPart modelPart) {
        super(modelPart);
    }
    @Override
    public void setupAnim(CatRenderState state) {
        super.setupAnim(state);
        if (CONFIG.isBaby) {
            this.head.yScale = 1.5f;
            this.head.xScale = 1.5f;
            this.head.zScale = 1.5f;
            this.head.y -= 1.0f;
        }
    }
}

package com.jeff.pets.vanilla.rabbit;

import net.minecraft.client.model.animal.rabbit.RabbitModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Pet.CONFIG;

public class ClientRabbitModel extends RabbitModel {

    private final ModelPart head;

    public ClientRabbitModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
    }

    @Override
    public void setupAnim(@NotNull RabbitRenderState state) {
        super.setupAnim(state);
        if (CONFIG.isBaby) {
            head.xScale = 1.5f;
            head.yScale = 1.5f;
            head.zScale = 1.5f;
        }
    }
}

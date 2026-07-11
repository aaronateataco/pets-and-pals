package io.github.aaronateataco.petsandpals.mob;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

/** Never placed in the world; carries the raft wood style for rendering. */
public class PetRaftBlock extends Block {

    public static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped"};
    public static final IntegerProperty STYLE = IntegerProperty.create("style", 0, WOODS.length - 1);

    public PetRaftBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(STYLE, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(STYLE);
    }
}

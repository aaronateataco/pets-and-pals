package io.github.aaronateataco.petsandpals.mob;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

/** Never placed in the world; carries the raft wood style for rendering. */
public class PetRaftBlock extends Block {

    public static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped", "poplar"};
    public static final IntegerProperty STYLE = IntegerProperty.create("style", 0, WOODS.length - 1);
    public static final String[] DYES = {"white", "light_gray", "gray", "black", "brown", "red",
            "orange", "yellow", "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink"};
    // last cushion index = bare deck (no cushion)
    public static final int NO_CUSHION = DYES.length;
    public static final IntegerProperty CUSHION = IntegerProperty.create("cushion", 0, NO_CUSHION);

    public PetRaftBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(STYLE, 1).setValue(CUSHION, 5));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(STYLE, CUSHION);
    }
}

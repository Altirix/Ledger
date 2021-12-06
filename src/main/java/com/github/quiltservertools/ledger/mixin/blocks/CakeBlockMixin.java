package com.github.quiltservertools.ledger.mixin.blocks;

import com.github.quiltservertools.ledger.Ledger;
import com.github.quiltservertools.ledger.LedgerKt;
import com.github.quiltservertools.ledger.callbacks.BlockChangeCallback;
import com.github.quiltservertools.ledger.utility.Sources;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CakeBlock.class)
public abstract class CakeBlockMixin {
    @Shadow @Final public static IntProperty BITES;

    @Inject(method = "tryEat",at = @At(value = "RETURN",ordinal = 1))
            private static void ledgerLogCakeEat(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<ActionResult> cir) {
                    BlockChangeCallback.EVENT.invoker().changeBlock(
                            player.world,
                            pos,
                            state,
                            state.with(BITES, state.get(BITES) - 1),
                            null,
                            null,
                            Sources.CONSUME,
                            player);
                    //do candle log in own mixin.

    }
}
package com.github.quiltservertools.ledger.mixin;

import com.github.quiltservertools.ledger.LedgerKt;
import com.github.quiltservertools.ledger.callbacks.BlockChangeCallback;
import com.github.quiltservertools.ledger.utility.Sources;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(CauldronBehavior.class)
public interface CauldronBehaviorMixin {

    @Inject(method = "fillCauldron",at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
            private static void ledgerLogFillCauldron(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state,
                                                      SoundEvent soundEvent, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("fill");
        ledgerLogFillCauldron(world, pos, world.getBlockState(pos), state, player);
    }
    @Inject(method = "emptyCauldron",at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private static void ledgerLogFullDrainCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output,
                                              Predicate<BlockState> predicate, SoundEvent soundEvent, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("full drain");
        ledgerLogDrainCauldron(world, pos, state, player);
    }
    @Inject(method = "method_32209",at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/LeveledCauldronBlock;decrementFluidLevel(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
            shift = At.Shift.AFTER))
    private static void ledgerLogLeatherDrainCauldron(BlockState state, World world, BlockPos pos,
                                                      PlayerEntity player, Hand hand, ItemStack stack, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("leather clean");
        //ledgerLogDrainCauldron(world, pos, state, player);
    }
    @Inject(method = "method_32214",at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/LeveledCauldronBlock;decrementFluidLevel(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
            shift = At.Shift.AFTER))
    private static void ledgerLogBannerDrainCauldron(BlockState state, World world, BlockPos pos,
                                                     PlayerEntity player, Hand hand, ItemStack stack, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("banner clean");
       // ledgerLogDrainCauldron(world, pos, state, player);
    }
    @Inject(method = "method_32215",at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/LeveledCauldronBlock;decrementFluidLevel(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
            shift = At.Shift.AFTER))
    private static void ledgerLogShulkerDrainCauldron(BlockState state, World world, BlockPos pos,
                                                      PlayerEntity player, Hand hand, ItemStack stack, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("shulker clean");
       // ledgerLogDrainCauldron(world, pos, state, player);
    }
    @Inject(method = "method_32219",at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
            shift = At.Shift.AFTER))
    private static void ledgerLogBottleFillWaterCauldron(BlockState state, World world, BlockPos pos,
                                                         PlayerEntity player, Hand hand, ItemStack stack, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("water bottle bruh");
        ledgerLogFillCauldron(world, pos, state,world.getBlockState(pos), player);
    }
    @Inject(method = "method_32222",at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
            shift = At.Shift.AFTER))
    private static void ledgerLogBottleFillEmptyCauldron(BlockState state, World world, BlockPos pos,
                                                         PlayerEntity player, Hand hand, ItemStack stack, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("water bottle");
        ledgerLogFillCauldron(world, pos, state,world.getBlockState(pos), player);
    }
    @Inject(method = "method_32220",at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/LeveledCauldronBlock;decrementFluidLevel(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
            shift = At.Shift.AFTER))
    private static void ledgerLogBottleDrainCauldron(BlockState state, World world, BlockPos pos,
                                                     PlayerEntity player, Hand hand, ItemStack stack, CallbackInfoReturnable<ActionResult> cir) {
        LedgerKt.logInfo("empty bottle drain");
        //ledgerLogDrainCauldron(world, pos, state, player);
    }

    private static void ledgerLogDrainCauldron(World world, BlockPos pos, BlockState oldState, PlayerEntity player){
        BlockChangeCallback.EVENT.invoker().changeBlock(
                world,
                pos,
                oldState,
                Blocks.CAULDRON.getDefaultState(),
                null,
                null,
                Sources.REMOVE,
                player);
    }

    private static void ledgerLogFillCauldron(World world, BlockPos pos, BlockState oldState, BlockState newState, PlayerEntity player){
        BlockChangeCallback.EVENT.invoker().changeBlock(
                world,
                pos,
                oldState,
                newState,
                null,
                null,
                Sources.FILL,
                player);
    }
}
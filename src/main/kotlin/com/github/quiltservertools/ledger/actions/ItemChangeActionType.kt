package com.github.quiltservertools.ledger.actions

import com.github.quiltservertools.ledger.utility.TextColorPallet
import com.github.quiltservertools.ledger.utility.getWorld
import com.github.quiltservertools.ledger.utility.literal
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Util
import net.minecraft.util.registry.Registry

abstract class ItemChangeActionType : AbstractActionType() {
    override fun getTranslationType(): String {
        val item = Registry.ITEM.get(objectIdentifier)
        return if (item is BlockItem) {
            "block"
        } else {
            "item"
        }
    }

    override fun getObjectMessage(): Text {
        val stack = ItemStack.fromNbt(StringNbtReader.parse(extraData))

        return "${stack.count} ".literal().append(
            TranslatableText(
                Util.createTranslationKey(
                    getTranslationType(),
                    objectIdentifier
                )
            )
        ).setStyle(TextColorPallet.secondaryVariant).styled {
            it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_ITEM,
                    HoverEvent.ItemStackContent(stack)
                )
            )
        }
    }

    protected fun getInventory(world: ServerWorld): Inventory? {
        var inventory: Inventory? = null
        val blockState = world.getBlockState(pos)
        val block = blockState.block
        if (block is InventoryProvider) {
            inventory = (block as InventoryProvider).getInventory(blockState, world, pos)
        } else if (world.getBlockEntity(pos) != null) {
            val blockEntity = world.getBlockEntity(pos)!!
            if (blockEntity is Inventory) {
                inventory = blockEntity
                if (inventory is ChestBlockEntity && block is ChestBlock) {
                    inventory = ChestBlock.getInventory(block, blockState, world, pos, true)
                }
            }
        }

        return inventory
    }

    protected fun removeMatchingItem(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)
        val inventory = world?.let { getInventory(it) }

        if (world != null && inventory != null) {
            val rollbackStack = ItemStack.fromNbt(StringNbtReader.parse(extraData))
            var rbStackSize = rollbackStack.count
            for (i in 0 until inventory.size()) {
                val stack = inventory.getStack(i)

                if (stack.isItemEqual(rollbackStack)) { // item matches
                    if (stack.count == rbStackSize) {
                        inventory.removeStack(i)
                        return true
                    } // stack matches removal amount
                    else if (stack.count < rbStackSize ) { // stack is smaller than rollback amount
                        rbStackSize =- stack.count
                        inventory.removeStack(i)
                    } // need to loop again as stacksize cant be 0 yet
                    else {
                        stack.count = stack.count - rbStackSize
                        return true
                    } //stack is greater than removal
                }
            }
        }

        return false
    }


    protected fun addItem(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)
        val inventory = world?.let { getInventory(it) }

        if (world != null && inventory != null) {
            val rollbackStack = ItemStack.fromNbt(StringNbtReader.parse(extraData))
            var rbStackSize = rollbackStack.count
            for (i in 0 until inventory.size()) {
                val stack = inventory.getStack(i)

                if (stack.isItemEqual(rollbackStack)) {
                    if (stack.count == stack.maxCount) {
                        continue
                    } // stack is already full skip
                    else if (stack.count + rbStackSize == stack.maxCount){
                        stack.count = stack.maxCount
                        return true
                    } //stack fits perfectly
                    else if (stack.count + rbStackSize > stack.maxCount) { // stack can only accept partial
                        rbStackSize = -(stack.maxCount - stack.count) //reduce by number of items allocated
                        stack.count = stack.maxCount
                    } // should always be set to max value for stack
                    else {
                        stack.increment(rbStackSize)
                        return true
                    } // stack wont exceed max so just increment
                }else if (stack.isEmpty) {
                    rollbackStack.count = rbStackSize // update count to edited value
                    inventory.setStack(i, rollbackStack)
                    return true
                }
            }
        }

        return false
    }
}

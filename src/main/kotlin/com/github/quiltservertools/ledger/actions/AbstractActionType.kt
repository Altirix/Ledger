package com.github.quiltservertools.ledger.actions

import com.github.quiltservertools.ledger.utility.Sources
import com.github.quiltservertools.ledger.utility.TextColorPallet
import com.github.quiltservertools.ledger.utility.literal
import com.mojang.authlib.GameProfile
import net.minecraft.block.BlockState
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

abstract class AbstractActionType : ActionType {
    override var timestamp: Instant = Instant.now()
    override var pos: BlockPos = BlockPos.ORIGIN
    override var world: Identifier? = null
    override var objectIdentifier: Identifier = Identifier("air")
    override var oldObjectIdentifier: Identifier = Identifier("air")
    override var blockState: BlockState? = null
    override var oldBlockState: BlockState? = null
    override var sourceName: String = Sources.UNKNOWN
    override var sourceProfile: GameProfile? = null
    override var extraData: String? = null
    override var rolledBack: Boolean = false

    override fun rollback(server: MinecraftServer): Boolean = false
    override fun previewRollback(player: ServerPlayerEntity) = Unit
    override fun previewRestore(player: ServerPlayerEntity) = Unit
    override fun restore(server: MinecraftServer): Boolean = false

    @ExperimentalTime
    override fun getMessage(): Text {
        val message = TranslatableText(
            "text.ledger.action_message",
            getTimeMessage(),
            getSourceMessage(),
            getActionMessage(),
            getObjectMessage(),
            getLocationMessage()
        )
        message.style = TextColorPallet.light

        if (rolledBack) {
            message.formatted(Formatting.STRIKETHROUGH)
        }

        return message
    }

    @ExperimentalTime
    open fun getTimeMessage(): Text {
        val duration = Duration.between(timestamp, Instant.now()).toKotlinDuration()
        val text: MutableText = "".literal()

        duration.toComponents { days, hours, minutes, seconds, _ ->

            when {
                days > 0 -> text.append(days.toString()).append("d")
                hours > 0 -> text.append(hours.toString()).append("h")
                minutes > 0 -> text.append(minutes.toString()).append("m")
                else -> text.append(seconds.toString()).append("s")
            }
        }

        val message = TranslatableText("text.ledger.action_message.time_diff", text)

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val timeMessage = formatter.format(timestamp.atZone(TimeZone.getDefault().toZoneId())).literal()

        message.styled {
            it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    timeMessage
                )
            )
        }

        return message
    }

    open fun getSourceMessage(): Text {
        if (sourceProfile == null) {
            return "@$sourceName".literal().setStyle(TextColorPallet.secondary)
        }

        if (sourceName == Sources.PLAYER) {
            return sourceProfile!!.name.literal().setStyle(TextColorPallet.secondary)
        }

        return "@$sourceName (${sourceProfile!!.name})".literal().setStyle(TextColorPallet.secondary)
    }

    open fun getActionMessage(): Text = TranslatableText("text.ledger.action.$identifier")
        .styled {
            it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    identifier.literal()
                )
            )
        }

    open fun getObjectMessage(): Text = TranslatableText(
        Util.createTranslationKey(
            this.getTranslationType(),
            objectIdentifier
        )
    ).setStyle(TextColorPallet.secondaryVariant).styled {
        it.withHoverEvent(
            HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                objectIdentifier.toString().literal()
            )
        )
    }

    open fun getLocationMessage(): Text = "${pos.x} ${pos.y} ${pos.z}".literal()
        .setStyle(TextColorPallet.secondary)
        .styled {
            it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    LiteralText(world?.let { "$it\n" } ?: "")
                        .append(TranslatableText("text.ledger.action_message.location.hover"))
                )
            ).withClickEvent(
                ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/lg tp ${world ?: World.OVERWORLD.value} ${pos.x} ${pos.y} ${pos.z}"
                )
            )
        }
}

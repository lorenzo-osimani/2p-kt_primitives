package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.primitives.sideEffects.AlterChannelsMsg
import it.unibo.tuprolog.primitives.sideEffects.AlterCustomDataMsg
import it.unibo.tuprolog.primitives.sideEffects.AlterFlagsMsg
import it.unibo.tuprolog.primitives.sideEffects.AlterOperatorsMsg
import it.unibo.tuprolog.primitives.sideEffects.AlterRuntimeMsg
import it.unibo.tuprolog.primitives.sideEffects.SetClausesOfKBMsg
import it.unibo.tuprolog.primitives.sideEffects.SideEffectMsg
import it.unibo.tuprolog.primitives.sideEffects.WriteOnOutputChannelMsg
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.sideffects.SideEffect

fun SideEffectMsg.deserialize(): SideEffect {
    return when (this.msgCase) {
        SideEffectMsg.MsgCase.CLAUSES -> {
            this.clauses.deserialize()
        }
        SideEffectMsg.MsgCase.FLAGS -> {
            this.flags.deserialize()
        }
        SideEffectMsg.MsgCase.RUNTIME -> {
            this.runtime.deserialize()
        }
        SideEffectMsg.MsgCase.OPERATORS -> {
            this.operators.deserialize()
        }
        SideEffectMsg.MsgCase.CHANNELS -> {
            this.channels.deserialize()
        }
        SideEffectMsg.MsgCase.CUSTOMDATA -> {
            this.customData.deserialize()
        }
        else -> throw ParsingException(this)
    }
}

private fun SetClausesOfKBMsg.deserialize(): SideEffect.SetClausesOfKb {
    val clauses = this.clausesList
        .map { it.deserialize().castToClause() }
    val effect = when (this.kbType) {
        SetClausesOfKBMsg.KbType.STATIC -> {
            when (this.operationType) {
                SetClausesOfKBMsg.OpType.RESET ->
                    SideEffect.ResetStaticKb(clauses)
                SetClausesOfKBMsg.OpType.ADD ->
                    SideEffect.AddStaticClauses(clauses, this.onTop)
                SetClausesOfKBMsg.OpType.REMOVE ->
                    SideEffect.RemoveStaticClauses(clauses)
                else -> { null }
            }
        }
        SetClausesOfKBMsg.KbType.DYNAMIC -> {
            when (this.operationType) {
                SetClausesOfKBMsg.OpType.RESET ->
                    SideEffect.ResetDynamicKb(clauses)
                SetClausesOfKBMsg.OpType.ADD ->
                    SideEffect.AddDynamicClauses(clauses, this.onTop)
                SetClausesOfKBMsg.OpType.REMOVE ->
                    SideEffect.RemoveDynamicClauses(clauses)
                else -> { null }
            }
        }
        else -> { null }
    }

    if (effect != null) {
        return effect
    } else {
        throw ParsingException(this)
    }
}

private fun AlterFlagsMsg.deserialize(): SideEffect.AlterFlags {
    val flags = this.flagsMap.map {
        Pair(it.key, it.value.deserialize())
    }.toMap()
    return when (this.operationType) {
        AlterFlagsMsg.OpType.RESET ->
            SideEffect.ResetFlags(flags)
        AlterFlagsMsg.OpType.SET ->
            SideEffect.SetFlags(flags)
        AlterFlagsMsg.OpType.CLEAR ->
            SideEffect.ClearFlags(flags.keys)
        else -> throw ParsingException(this)
    }
}

private fun AlterRuntimeMsg.deserialize(): SideEffect.AlterRuntime {
    return when (this.operationType) {
        AlterRuntimeMsg.OpType.LOAD ->
            SideEffect.LoadLibrary(Library.of(this.getLibraries(0)))
        AlterRuntimeMsg.OpType.UNLOAD ->
            SideEffect.UnloadLibraries(this.librariesList)
        AlterRuntimeMsg.OpType.RESET ->
            SideEffect.ResetRuntime(Runtime.empty())
        else -> throw ParsingException(this)
    }
}

private fun AlterOperatorsMsg.deserialize(): SideEffect.AlterOperators {
    val operators = this.operatorsList.map { it.deserialize() }
    return when (this.operationType) {
        AlterOperatorsMsg.OpType.SET ->
            SideEffect.SetOperators(operators)
        AlterOperatorsMsg.OpType.RESET ->
            SideEffect.ResetOperators(operators)
        AlterOperatorsMsg.OpType.REMOVE ->
            SideEffect.RemoveOperators(operators)
        else -> throw ParsingException(this)
    }
}

private fun AlterChannelsMsg.deserialize(): SideEffect.AlterChannels {
    val effect: SideEffect.AlterChannels? = if (this.hasClose()) {
        closeChannels(this.close.channelType, this.close.channelsList)
    } else if (this.hasModify()) {
        modifyChannels(this.modify.channelType, this.modify.opType, this.modify.channelsMap)
    } else if (this.hasWrite()) {
        writeOnChannels(this.write.messagesMap)
    } else { null }

    if (effect != null) {
        return effect
    } else {
        throw ParsingException(this)
    }
}

private fun closeChannels(
    channelType: AlterChannelsMsg.ChannelType,
    channelsList: Iterable<String>
): SideEffect.AlterChannels? =
    when (channelType) {
        AlterChannelsMsg.ChannelType.INPUT -> {
            SideEffect.CloseInputChannels(channelsList)
        }
        AlterChannelsMsg.ChannelType.OUTPUT -> {
            SideEffect.CloseOutputChannels(channelsList)
        }
        else -> { null }
    }

private fun modifyChannels(
    channelType: AlterChannelsMsg.ChannelType,
    opType: AlterChannelsMsg.ModifyChannels.OpType,
    channelsMap: Map<String, String>
): SideEffect.AlterChannels? =
    when (channelType) {
        AlterChannelsMsg.ChannelType.INPUT -> {
            val inputs = channelsMap.map {
                Pair(
                    it.key,
                    InputChannel.of(it.value)
                )
            }.toMap()
            when (opType) {
                AlterChannelsMsg.ModifyChannels.OpType.OPEN -> {
                    SideEffect.OpenInputChannels(inputs)
                }
                AlterChannelsMsg.ModifyChannels.OpType.RESET -> {
                    SideEffect.ResetInputChannels(inputs)
                }
                else -> { null }
            }
        }
        AlterChannelsMsg.ChannelType.OUTPUT -> {
            val outputs = channelsMap.map {
                Pair(it.key, OutputChannel.of<String> { })
            }
                .toMap()
            when (opType) {
                AlterChannelsMsg.ModifyChannels.OpType.OPEN -> {
                    SideEffect.OpenOutputChannels(outputs)
                }
                AlterChannelsMsg.ModifyChannels.OpType.RESET -> {
                    SideEffect.ResetOutputChannels(outputs)
                }
                else -> { null }
            }
        }
        else -> { null }
    }

private fun writeOnChannels(
    messagesMap: Map<String, WriteOnOutputChannelMsg.Messages>
): SideEffect.AlterChannels =
    SideEffect.WriteOnOutputChannels(
        messagesMap.mapValues {
            it.value.messageList
        }
    )
private fun AlterCustomDataMsg.deserialize(): SideEffect.AlterCustomData {
    return when (this.type) {
        AlterCustomDataMsg.OpType.SET_PERSISTENT ->
            SideEffect.SetPersistentData(this.dataMap)
        AlterCustomDataMsg.OpType.SET_DURABLE ->
            SideEffect.SetDurableData(this.dataMap)
        AlterCustomDataMsg.OpType.SET_EPHEMERAL ->
            SideEffect.SetEphemeralData(this.dataMap)
        else -> throw ParsingException(this)
    }
}

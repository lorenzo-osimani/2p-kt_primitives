package it.unibo.tuprolog.primitives.server.session.event

import it.unibo.tuprolog.primitives.PrimitiveMsg
import it.unibo.tuprolog.primitives.SubResponseMsg

interface SubRequestEvent {

    val message: PrimitiveMsg

    val id: String

    fun signalResponse(msg: SubResponseMsg)

    fun awaitResult(): Any?
}

package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.PrimitiveMsg
import it.unibo.tuprolog.primitives.ResponseMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.serialization.deserializers.distribuited.deserializeAsDistributed
import it.unibo.tuprolog.primitives.serialization.serializers.distribuited.buildSubSolveMsg
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class SingleSubSolveEvent(
    override val id: String,
    query: Struct,
    timeout: Long
) : SubRequestEvent {

    override val message: PrimitiveMsg = buildSubSolveMsg(query, id, timeout = timeout)

    private val result: CompletableDeferred<ResponseMsg> = CompletableDeferred()
    private var hasNext: Boolean? = null

    fun hasNext(): Boolean? = hasNext

    override fun awaitResult(): DistributedResponse {
        val response = runBlocking {
            result.await()
        }
        hasNext = response.solution.hasNext
        return response.deserializeAsDistributed()
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if (msg.hasSolution()) {
            this.result.complete(msg.solution)
        } else {
            throw IllegalArgumentException("The message received is not of a SubSolve")
        }
    }
}

package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.primitives.PrimitiveMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.serialization.deserializers.deserializeAsClause
import it.unibo.tuprolog.primitives.serialization.serializers.distribuited.buildInspectKbMsg
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class SingleInspectKbEvent(
    override val id: String,
    kbType: Session.KbType,
    maxClauses: Long,
    vararg filters: Pair<Session.KbFilter, String>
) : SubRequestEvent {

    override val message: PrimitiveMsg = buildInspectKbMsg(id, kbType, maxClauses, *filters)

    private val result: CompletableDeferred<Clause?> = CompletableDeferred()

    override fun awaitResult(): Clause? {
        val clause = runBlocking {
            result.await()
        }
        return clause
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if (msg.hasClause()) {
            this.result.complete(
                msg.clause.deserializeAsClause()
            )
        } else {
            throw IllegalArgumentException("The argument received is not a InspectKb")
        }
    }
}

package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.parsers.serializers.distribuited.buildReadLineMsg
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import it.unibo.tuprolog.primitives.utils.END_OF_READ_EVENT
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class ReadLineEvent(
    override val id: String,
    channelName: String
) : SubRequestEvent {

    override val message: GeneratorMsg = buildReadLineMsg(id, channelName)

    private val result: CompletableDeferred<LineMsg> = CompletableDeferred()

    override fun awaitResult(): String {
        val result = runBlocking {
            result.await()
        }
        return if (result.hasContent()) {
            result.content
        } else {
            END_OF_READ_EVENT
        }
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if (msg.hasLine()) {
            this.result.complete(msg.line)
        } else {
            throw IllegalArgumentException("The message received is not of a ReadLine")
        }
    }
}

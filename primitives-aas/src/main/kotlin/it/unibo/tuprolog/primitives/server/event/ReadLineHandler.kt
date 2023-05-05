package it.unibo.tuprolog.primitives.server.event

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.SubSolveResponse
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildReadLineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveMsg
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ReadLineHandler(private val responseObserver: StreamObserver<GeneratorMsg>):
    ServerEvent<String, LineMsg, String> {

    private val readLineMap: MutableMap<String, BlockingQueue<LineMsg>> = mutableMapOf()

    override fun sendRequest(input: String): String {
        readLineMap.putIfAbsent(input, LinkedBlockingQueue())
        responseObserver.onNext(
            buildReadLineMsg(input)
        )
        return readLineMap[input]!!.take().content
    }

    override fun handleResponse(response: LineMsg) {
        readLineMap[response.channelName]?.add(response)
    }
}
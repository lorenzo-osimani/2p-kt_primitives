package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.distribuited.deserializeAsDistributed
import it.unibo.tuprolog.primitives.parsers.serializers.distribuited.serialize
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedRequest
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.ReadLineEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.SingleSubSolveEvent
import it.unibo.tuprolog.primitives.utils.checkType
import it.unibo.tuprolog.primitives.utils.idGenerator

/**
 * Represent the observer of a connection between the Primitive Server and a client,
 * generated from a call of the primitive
 */
class ServerSessionImpl(
    primitive: DistributedPrimitive,
    request: RequestMsg,
    private val responseObserver: StreamObserver<GeneratorMsg>
) : ServerSession {

    private val stream: Iterator<DistributedResponse>
    private val ongoingSubRequests: MutableList<SubRequestEvent> = mutableListOf()
    private val request: DistributedRequest

    init {
        this.request = request.deserializeAsDistributed(this)
        stream = primitive.solve(
            this.request
        ).iterator()
    }
    override fun handleMessage(msg: SolverMsg) {
        /** Handling Next Request */
        if (msg.hasNext()) {
            try {
                val solution = stream.next().serialize(stream.hasNext())
                responseObserver.onNext(
                    GeneratorMsg.newBuilder().setResponse(solution).build()
                )
                if (!solution.solution.hasNext) responseObserver.onCompleted()
            } catch (_: NoSuchElementException) {
                responseObserver.onNext(
                    GeneratorMsg.newBuilder()
                        .setResponse(
                            request.replyFail().serialize(false)
                        ).build()
                )
                responseObserver.onCompleted()
            }
        }
        /** Handling SubRequest Event */
        else if (msg.hasResponse()) {
            ongoingSubRequests.find { it.id == msg.response.id }.let {
                it?.signalResponse(msg.response)
            }
        }
        /** Throws error if it tries to initialize again */
        else if (msg.hasRequest()) {
            throw IllegalArgumentException("The request has already been initialized")
        }
    }

    override fun subSolve(query: Struct, timeout: Long): Sequence<DistributedResponse> =
        object : Iterator<DistributedResponse> {
            val id: String = idGenerator()
            private var hasNext: Boolean = true

            override fun hasNext(): Boolean =
                hasNext

            override fun next(): DistributedResponse {
                if (hasNext()) {
                    val request = SingleSubSolveEvent(id, query, timeout)
                    return enqueueRequestAndAwait(request)
                        .checkType<DistributedResponse>()
                        .also {
                            hasNext = request.hasNext()!!
                        }
                } else {
                    throw NoSuchElementException()
                }
            }
        }.asSequence()

    override fun readLine(channelName: String): String {
        val request = ReadLineEvent(idGenerator(), channelName)
        return enqueueRequestAndAwait(request).checkType()
    }

    override fun enqueueRequestAndAwait(
        request: SubRequestEvent
    ): Any? {
        ongoingSubRequests.add(request)
        responseObserver.onNext(request.message)
        return request.awaitResult().also {
            ongoingSubRequests.remove(request)
        }
    }
}

package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitive
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent

interface ServerSession : Session {

    fun handleMessage(msg: SolverMsg)

    fun enqueueRequestAndAwait(request: SubRequestEvent): Any?

    companion object {
        fun of(
            primitive: DistributedPrimitive,
            request: RequestMsg,
            responseObserver: StreamObserver<GeneratorMsg>
        ): ServerSession =
            ServerSessionImpl(primitive, request, responseObserver)
    }
}

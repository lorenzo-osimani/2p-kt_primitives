package it.unibo.tuprolog.primitives.server.session.impl

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.server.distribuited.DistributedRuntime
import it.unibo.tuprolog.primitives.server.session.ContextRequester
import it.unibo.tuprolog.primitives.server.session.ServerSession
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.primitives.server.session.event.impl.GetEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.SingleInspectKbEvent
import it.unibo.tuprolog.primitives.utils.checkType
import it.unibo.tuprolog.primitives.utils.idGenerator
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.unify.Unificator

class ContextRequesterImpl(
    private val session: ServerSession
) : ContextRequester {

    override fun inspectKB(
        kbType: Session.KbType,
        maxClauses: Long,
        vararg filters: Pair<Session.KbFilter, String>
    ): Sequence<Clause?> =
        object : Iterator<Clause?> {
            private val id = idGenerator()
            private var hasNext: Boolean = true

            override fun hasNext(): Boolean =
                hasNext

            override fun next(): Clause? {
                if (hasNext()) {
                    val request = SingleInspectKbEvent(id, kbType, maxClauses, *filters)
                    return session.enqueueRequestAndAwait(request)
                        .also {
                            hasNext = (it != null)
                        }
                        .checkType<Clause?>()
                } else {
                    throw NoSuchElementException()
                }
            }
        }.asSequence()

    override fun getLogicStackTrace(): List<Struct> =
        session.enqueueRequestAndAwait(
            GetEvent.ofLogicStackTrace(idGenerator())
        ).checkType()

    override fun getCustomDataStore(): CustomDataStore =
        session.enqueueRequestAndAwait(
            GetEvent.ofCustomDataStore(idGenerator())
        ).checkType()

    override fun getUnificator(): Unificator =
        session.enqueueRequestAndAwait(
            GetEvent.ofUnificator(idGenerator())
        ).checkType()

    override fun getLibraries(): DistributedRuntime =
        session.enqueueRequestAndAwait(
            GetEvent.ofLibraries(idGenerator())
        ).checkType()

    override fun getFlagStore(): FlagStore =
        session.enqueueRequestAndAwait(
            GetEvent.ofFlagStore(idGenerator())
        ).checkType()

    override fun getOperators(): OperatorSet =
        session.enqueueRequestAndAwait(
            GetEvent.ofOperators(idGenerator())
        ).checkType()

    override fun getInputStoreAliases(): Set<String> =
        session.enqueueRequestAndAwait(
            GetEvent.ofInputChannels(idGenerator())
        ).checkType()

    override fun getOutputStoreAliases(): Set<String> =
        session.enqueueRequestAndAwait(
            GetEvent.ofOutputChannels(idGenerator())
        ).checkType()
}

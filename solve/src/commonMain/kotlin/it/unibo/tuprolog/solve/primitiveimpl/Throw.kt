package it.unibo.tuprolog.solve.primitiveimpl

import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.primitive.Primitive
import it.unibo.tuprolog.primitive.Signature
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solve
import it.unibo.tuprolog.solve.exception.HaltException
import it.unibo.tuprolog.solve.exception.PrologError
import it.unibo.tuprolog.solve.exception.prologerror.ErrorUtils
import it.unibo.tuprolog.solve.exception.prologerror.InstantiationError
import it.unibo.tuprolog.solve.exception.prologerror.SystemError
import it.unibo.tuprolog.solve.solver.DeclarativeImplExecutionContext
import it.unibo.tuprolog.solve.solver.ExecutionContextImpl
import it.unibo.tuprolog.unify.Unification.Companion.matches
import it.unibo.tuprolog.unify.Unification.Companion.mguWith

/**
 * Implementation of primitive handling `throw/1` behaviour
 *
 * @author Enrico
 */
object Throw : PrimitiveWrapper(Signature("throw", 1)) {

    override val uncheckedImplementation: Primitive = { mainRequest ->
        // TODO: 25/09/2019 remove that
        mainRequest as Solve.Request<ExecutionContextImpl>

        when (val throwArgument = mainRequest.arguments.single().freshCopy()) {
            // throw/1 argument is a Variable
            is Var -> throw InstantiationError(
                    "throw/1 argument should not be a not-instantiated variable",
                    context = mainRequest.context,
                    extraData = throwArgument
            )

            else -> {
                val ancestorCatchesRequests = mainRequest.context.logicalParentRequests.filter { it.signature == Catch.signature }
                val ancestorCatch = ancestorCatchesRequests.find { it.arguments[1].matches(throwArgument) }
                // performance: inefficient because it recomputes mgu two times, when match found

                when (val catcherUnifyingSubstitution = ancestorCatch?.arguments?.get(1)?.mguWith(throwArgument)) {

                    // no catch found that can handle thrown exception
                    null, is Substitution.Fail -> {
                        val errorCause = extractErrorCauseChain(throwArgument, mainRequest.context)
                        when {
                            isSystemErrorStruct(throwArgument) -> throw HaltException(
                                    "Unhandled system_error, halting inferential machine",
                                    errorCause,
                                    mainRequest.context
                            )
                            // current unhandled exception is some other error
                            else -> throw SystemError(
                                    "Exception thrown, but no compatible catch/3 found",
                                    errorCause,
                                    mainRequest.context,
                                    throwArgument
                            )
                        }
                    }

                    // catch, to handle exception, found
                    is Substitution.Unifier -> sequenceOf(with(mainRequest) {
                        val newSubstitution = (context.substitution + catcherUnifyingSubstitution)
                                as Substitution.Unifier

                        Solve.Response(
                                Solution.Yes(signature, arguments, newSubstitution),
                                context = context.copy(
                                        substitution = newSubstitution,
                                        throwRelatedToCutContextsParent = ancestorCatch.context
                                )
                        )
                    })
                }
            }
        }
    }

    /** Utility function to check if throwArgument is an `error(system_error, ...)` */
    private fun isSystemErrorStruct(aTerm: Term) = with(aTerm as? Struct) {
        this?.functor == ErrorUtils.errorWrapperFunctor
                && this.arity == 2
                && (this.args.firstOrNull() as? Atom)?.value == SystemError.typeFunctor
    }

    /** Utility function to extract error type from a term that should be `error(TYPE_STRUCT, ...)` */
    private fun extractErrorTypeAndExtra(aTerm: Term) = with(aTerm as? Struct) {
        when {
            this?.functor == ErrorUtils.errorWrapperFunctor && this.arity == 2 ->
                (this.args.firstOrNull() as? Struct)?.let { Pair(it, this.args[1]) }
            else -> null
        }
    }

    /** Utility function to extract error, with filled cause field, till the error root */
    private fun extractErrorCauseChain(aTerm: Term, context: DeclarativeImplExecutionContext): PrologError? =
            extractErrorTypeAndExtra(aTerm)?.let { (type, extra) ->
                PrologError.of(
                        context = context,
                        type = type,
                        extraData = extra,
                        cause = extractErrorCauseChain(extra, context)
                )
            }

}

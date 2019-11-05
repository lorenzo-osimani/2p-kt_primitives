package it.unibo.tuprolog.solve.solver.fsm.state.impl

import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solve
import it.unibo.tuprolog.solve.forEachWithLookahead
import it.unibo.tuprolog.solve.solver.ExecutionContextImpl
import it.unibo.tuprolog.solve.solver.SolverUtils.moreThanOne
import it.unibo.tuprolog.solve.solver.SolverUtils.newSolveRequest
import it.unibo.tuprolog.solve.solver.extendParentScopeWith
import it.unibo.tuprolog.solve.solver.fsm.StateMachineExecutor
import it.unibo.tuprolog.solve.solver.fsm.state.AlreadyExecutedState
import it.unibo.tuprolog.solve.solver.fsm.state.FinalState
import it.unibo.tuprolog.solve.solver.fsm.state.State
import it.unibo.tuprolog.solve.solver.fsm.state.asAlreadyExecuted
import it.unibo.tuprolog.solve.solver.orderWithStrategy
import it.unibo.tuprolog.solve.solver.shouldCutExecuteInRuleSelection
import it.unibo.tuprolog.unify.Unification.Companion.mguWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * State responsible of selecting a rule to be solved to demonstrate a goal
 *
 * @author Enrico
 */
internal class StateRuleSelection( // TODO: 04/11/2019 remove state package flattening to fsm as for solve-classic
        override val solve: Solve.Request<ExecutionContextImpl>,
        override val executionStrategy: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AbstractTimedState(solve, executionStrategy) {

    /** The execute function to be used when a [State] needs, internally, to execute sub-[State]s behaviour */
    private val subStateExecute: (State) -> Sequence<AlreadyExecutedState> = StateMachineExecutor::executeWrapping

    override fun behaveTimed(): Sequence<State> = sequence {
        val currentGoal = solve.query
        val matchingRules = solve.context.retrieveRulesMatching(currentGoal)
        val isChoicePoint = moreThanOne(matchingRules)

        when {
            matchingRules.none() -> yield(stateEndFalse())

            else -> with(solve.context) { matchingRules.orderWithStrategy(this, solverStrategies::clauseChoiceStrategy) }
                    .map { it.freshCopy() }
                    .map { refreshedRule ->
                        val unifyingSubstitution = currentGoal mguWith refreshedRule.head

                        val wellFormedRuleBody = refreshedRule.body.apply(unifyingSubstitution) as Struct

                        solve.newSolveRequest(wellFormedRuleBody, unifyingSubstitution, isChoicePointChild = isChoicePoint)

                    }.forEachWithLookahead { subSolveRequest, hasAlternatives ->
                        val subInitialState = StateInit(subSolveRequest.initializeForSubRuleScope(), executionStrategy)
                                .also { yield(it.asAlreadyExecuted()) }

                        var cutNextSiblings = false

                        // execute internally the sub-request in a sub-state-machine, to see what it will respond
                        subStateExecute(subInitialState).forEach {
                            yield(it)

                            val subState = it.wrappedState

                            // find in sub-goal state sequence, the final state responding to current solveRequest
                            if (subState is FinalState && subState.solve.solution.query == subSolveRequest.query) {

                                if (subState.solve.sideEffectManager.shouldCutExecuteInRuleSelection())
                                    cutNextSiblings = true

                                // yield only non-false states or false states when there are no open alternatives (because no more or cut)
                                if (subState !is StateEnd.False || !hasAlternatives || cutNextSiblings) {
                                    val extendedScopeSideEffectManager = subState.solve.sideEffectManager
                                            .extendParentScopeWith(solve.context.sideEffectManager)

                                    yield(stateEnd(subState.solve.copy(sideEffectManager = extendedScopeSideEffectManager)))
                                }

                                if (subState is StateEnd.Halt) return@sequence // if halt reached, overall computation should stop
                            }
                        }
                        if (cutNextSiblings) return@sequence // cut here other matching rules trial
                    }
        }
    }

    private companion object {

        /**
         * Retrieves from receiver [ExecutionContext] those rules whose head matches [currentGoal]
         *
         * 1) It searches for matches inside libraries, if nothing found
         * 2) it looks inside both staticKB and dynamicKB
         */
        private fun ExecutionContext.retrieveRulesMatching(currentGoal: Struct): Sequence<Rule> =
                currentGoal.freshCopy().let { refreshedGoal ->
                    libraries.theory[refreshedGoal].takeIf { it.any() }
                            ?: sequenceOf(staticKB, dynamicKB).flatMap { it[refreshedGoal] }
                }

        /** Prepares provided solveRequest "side effects manager" to enter this "rule body sub-scope" */
        private fun Solve.Request<ExecutionContextImpl>.initializeForSubRuleScope() =
                copy(context = with(context) { copy(sideEffectManager = sideEffectManager.enterRuleSubScope()) })
    }
}

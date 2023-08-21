package sideeffects

import KotlinPrimitivesTestSuite
import examples.filterKBPrimitive
import examples.getEventsPrimitive
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.theory.Theory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestGetEvents : KotlinPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> =
        listOf(filterKBPrimitive, getEventsPrimitive)

    /** Testing Basic Primitive **/
    @Test
    @Throws(Exception::class)
    fun testEvents() {
        logicProgramming {
            val query = Struct.of("testEvents")
            assertTrue(solver.solveOnce(query).isYes)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testStartsWithFilterKB() {
        logicProgramming {
            solver.appendStaticKb(
                Theory.of(
                    Clause.of("p"("a")),
                    Clause.of("f"("b")),
                    Clause.of("f"("c"))
                )
            )
            val query = "filterKB"(Term.parse("f"), Atom.of(Session.KbFilter.STARTS_WITH.name), X)
            val solution = solver.solveList(query)
            solution.forEach {
                println(it)
                assertTrue(it.isYes)
            }
            assertEquals(2, solution.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testContainsFunctorFilterKB() {
        logicProgramming {
            solver.appendDynamicKb(
                Theory.of(
                    Clause.of("p"("f"("a"))),
                    Clause.of("f"("b"))
                )
            )
            val query = "filterKB"(Term.parse("f"), Atom.of(Session.KbFilter.CONTAINS_FUNCTOR.name), X)
            val solution = solver.solveList(query)
            solution.forEach {
                println(it)
                assertTrue(it.isYes)
            }
            assertEquals(2, solution.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testContainsTermFilterKB() {
        logicProgramming {
            solver.appendStaticKb(
                Theory.of(
                    Clause.of("p"("f"("a")))
                )
            )
            solver.appendDynamicKb(
                Theory.of(
                    Clause.of("f"("b"))
                )
            )
            val query = "filterKB"("f"("a"), Atom.of(Session.KbFilter.CONTAINS_TERM.name), X)
            val solution = solver.solveList(query)
            solution.forEach {
                println(it)
                assertTrue(it.isYes)
            }
            assertEquals(1, solution.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyFilterKB() {
        logicProgramming {
            val query = "filterKB"(Term.parse("f"), X)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isNo)
        }
    }
}

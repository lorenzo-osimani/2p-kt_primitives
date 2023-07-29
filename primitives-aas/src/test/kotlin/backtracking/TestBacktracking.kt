package backtracking

import KotlinPrimitivesTestSuite
import examples.innestedPrimitive
import examples.ntPrimitive
import examples.readerPrimitive
import examples.throwablePrimitive
import examples.writerPrimitive
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.channel.InputStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBacktracking : KotlinPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> =
        listOf(innestedPrimitive, ntPrimitive, readerPrimitive, throwablePrimitive, writerPrimitive)

    /** Testing Basic Primitive **/
    @Test
    @Throws(Exception::class)
    fun testNt() {
        logicProgramming {
            val query = "nt"(X)
            val solutions = solver.solve(query).take(4).map {
                it.solvedQuery!!
            }.toList()
            assertEquals(
                listOf(
                    "nt"(0),
                    "nt"(1),
                    "nt"(2),
                    "nt"(3)
                ).toList(),
                solutions
            )
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testSubSolve() {
        logicProgramming {
            val query = "solve"("natural"(X))
            val solutions = solver.solve(query).take(2).map {
                it.solvedQuery!!
            }.toList()
            assertEquals(
                listOf(
                    "solve"("natural"(0)),
                    "solve"("natural"(1))
                ).toList(),
                solutions
            )
        }
    }

    /** Testing ReadLine **/
    @Test
    @Throws(Exception::class)
    fun testReadLine() {
        val solutions = logicProgramming {
            val query = "readLine"(InputStore.STDIN, X)
            solver.solve(query).take(6).toList()
        }
        assertTrue { solutions.last().isNo }
        assertEquals(
            listOf("h", "e", "l", "l", "o").toList(),
            solutions.take(5).map {
                it.substitution.values.first().toString()
            }
        )
    }
}

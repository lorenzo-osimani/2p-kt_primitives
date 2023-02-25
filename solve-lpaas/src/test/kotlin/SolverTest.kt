import io.grpc.internal.testing.StreamRecorder
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.SolutionListReply
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.client.SimpleSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientPrologSolverImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SolverTest {

    private val basicSolver: SimpleSolver = SimpleSolver.prolog.basicClient()

    /*@Throws(IOException::class)
    @BeforeTest
    fun beforeEach() {
    }*/

    /** Testing Simple Solve **/
    @Test
    @Throws(Exception::class)
    fun simpleSolveQuery() {
        val result = basicSolver.solve("f(X)")
        assertEquals(
            Struct.Companion.of("f", Term.parse("b")),
            result.iterator().next().solvedQuery
        )
    }
    /*
    /** Testing Factory of Solvers **/
    @Test
    @Throws(Exception::class)
    fun createSolver() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(a).
                   """.trimIndent())
        client1.solve("p(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("p(a)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing Stream-Like Nature of Solve **/
    @Test
    @Throws(Exception::class)
    fun testStreamLikeResponse() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.solve("f(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("f(b)"),
            responseStream.values.map { it.solvedQuery }
        )

        val responseStream2: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.getNextSolution(responseStream2)
        responseStream2.awaitCompletion()
        assertContentEquals(
            listOf("f(d)"),
            responseStream2.values.map { it.solvedQuery }
        )

        val responseStream3: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.getNextSolution(responseStream3)
        responseStream3.awaitCompletion()
        assert(responseStream3.values.first().isNo)
    }

    /** Testing async-nature of requests **/
    @Test
    @Throws(Exception::class)
    fun asyncRequests() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(X):-p(X).
                   """.trimIndent())
        client1.solve("p(X)")
        client2.solve("f(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("f(b)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing SolveAsList **/
    @Test
    @Throws(Exception::class)
    fun solveQueryList() {
        val responseStream: StreamRecorder<SolutionListReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(a) :- sleep(3000).
                   p(c).
                   """.trimIndent())
        client1.solveList("p(X)", responseStream)
        runBlocking {
            delay(1000)
        }
        assert(responseStream.values.isEmpty())
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("p(a)", "p(c)"),
            responseStream.values.first().solutionList.map {
                it.solvedQuery
            }
        )
    }

    /** Testing Solve Once **/
    @Test
    @Throws(Exception::class)
    fun solveOnceQuery() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.solveOnce("f(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("f(b)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing Solve With Timeout **/
    @Test
    @Throws(Exception::class)
    fun solveQueryWithTimeout() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(X):-p(X).
                   """.trimIndent())
        client1.solve("p(X)", 50, responseStream)
        responseStream.awaitCompletion()
        val results = responseStream.values.map {
            it.error
        }
        assertContains(
            results.first(), "TimeOutException"
        )
    }

    @Test
    @Throws(Exception::class)
    fun solveQueryAsListWithTimeout() {
        val responseStream: StreamRecorder<SolutionListReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(a).
                   p(X):-p(X).
                   """.trimIndent())
        client1.solveList("p(X)", SolveOptions.allEagerlyWithTimeout(50),
            responseStream)
        responseStream.awaitCompletion()
        val results = responseStream.values.first().solutionList.map {
            Pair(it.solvedQuery, it.error)
        }
        assertContains(
            results.map { it.first }, "p(a)"
        )
        assertContains(
            results.last().second,"TimeOutException"
        )
    }
    */
}



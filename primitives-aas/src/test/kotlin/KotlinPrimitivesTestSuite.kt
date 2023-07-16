import io.grpc.Server
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import java.util.concurrent.CountDownLatch
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class KotlinPrimitivesTestSuite: AbstractPrimitivesTestSuite() {

    private val activeServices = mutableMapOf<Int, Server>()
    abstract val primitives: List<DistributedPrimitiveWrapper>

    override fun getActivePorts(): Set<Int> = activeServices.keys

    @BeforeTest
    override fun beforeEach() {
        var port = 8080
        val latch = CountDownLatch(primitives.size)
        primitives.forEach {
            val service = PrimitiveServerFactory.startService(it, port, "customLibrary")
            latch.countDown()
            activeServices[port] = service
            port++
            executor.submit {
                service.awaitTermination()
            }
        }
        latch.await()
        super.beforeEach()
    }

    @AfterTest
    override fun afterEach() {
        activeServices.values.forEach {
            it.shutdownNow()
            it.awaitTermination()
        }
        activeServices.clear()
        super.afterEach()
    }
}
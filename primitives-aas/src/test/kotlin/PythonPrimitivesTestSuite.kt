import java.math.BigInteger
import java.util.concurrent.ExecutorService

abstract class PythonPrimitivesTestSuite: AbstractPrimitivesTestSuite() {


    private lateinit var startingPort: BigInteger
    private lateinit var maxPort: BigInteger
    private lateinit var serverProcess: Process

    override fun getActivePorts(): Set<Int> =
        (startingPort.toInt() until maxPort.toInt() + 1).toSet()

    private fun ExecutorService.pythonModuleExec(moduleName: String, healthCheck: String): Process {
        val process = ProcessBuilder("python", "-m", moduleName).start()
        Runtime.getRuntime().addShutdownHook(Thread {
            process.destroyForcibly()
            process.waitFor()
        })
        val healthCheckPattern = healthCheck.toRegex()
        submit {
            process.errorStream.bufferedReader().useLines {
                it.forEach(System.err::println)
            }
        }

        val healthy = process.inputStream.bufferedReader().lineSequence().firstOrNull {
            println(it)
            if(it.matches(healthCheckPattern)) {
                val ports = "[0-9]+".toRegex().findAll(it).map {num -> num.value.toBigInteger() }
                startingPort = ports.first()
                maxPort = ports.last()
                true
            } else false
        }
        submit {
            process.inputStream.bufferedReader().useLines {
                it.forEach(System.out::println)
            }
        }
        return if (healthy != null) process else error("Failed to start ml-lib server")
    }

    override fun beforeEach() {
        startingPort = BigInteger.valueOf(8080)
        maxPort = BigInteger.valueOf(8106)
        serverProcess = executor.pythonModuleExec(
           "prolog_primitives.ml_lib",
            "^Servers listening from \\d+ to \\d+")
        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()
        serverProcess.destroyForcibly()
        serverProcess.waitFor()
    }
}
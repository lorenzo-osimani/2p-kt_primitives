
package pythontests

import PythonPrimitivesTestSuite
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.dsl.logicProgramming
import it.unibo.tuprolog.theory.Theory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DemoTest : PythonPrimitivesTestSuite() {

    private fun readStrictCsv(path: String): List<Map<String, Number>> {
        val reader = javaClass.getResourceAsStream(path)!!.bufferedReader()
        val headers = reader.readLine().split(',')
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                headers.zip(
                    it.split(',')
                        .map { value ->
                            try {
                                value.toBigDecimal()
                            } catch (_: NumberFormatException) {
                                0
                            }
                        }
                ).toMap()
            }.toList()
    }

    private fun fromCSVtoTheory(
        title: String,
        csv: List<Map<String, Number>>,
        targets: Array<String> = emptyArray()
    ): Theory {
        return logicProgramming {
            val theory = mutableListOf<Clause>(
                fact { "schema_name"(title) },
                fact { "schema_target"(targets.map { Atom.of(it) }) }
            )

            val attributes = mutableListOf<Clause>()
            for (attr in csv.first().keys) {
                attributes.add(
                    fact { "attribute"(attributes.size, Atom.of(attr), "real") }
                )
            }
            theory.addAll(attributes)
            theory.addAll(
                csv.map {
                    fact {
                        Struct.of(
                            title,
                            it.values.map { value -> value.toTerm() }
                        )
                    }
                }
            )
            return@logicProgramming Theory.of(theory)
        }
    }

    override fun beforeEach() {
        super.beforeEach()
        logicProgramming {
            solver.appendStaticKb(
                Theory.Companion.of(
                    rule {
                        "createModel"("NInput", "NOutput", E) `if` (
                            "input_layer"("NInput", A) and
                                "dense_layer"(A, 128, "relu", B) and
                                "dense_layer"(B, 64, "relu", C) and
                                "output_layer"(C, "NOutput", "linear", D) and
                                "neural_network"(D, E)
                            )
                    },
                    rule {
                        "getDataset"(X) `if` (
                            "theory_to_dataset"(schemaName, X)
                            )
                    },
                    /* Trains a NN multiple times, over Dataset, using the provided Params. */
                    /* Returns the AveragePerformance over a 10-fold CV. */
                    rule {
                        "train_cv"("Dataset", "LearnParams", "AllPerformances") `if` (
                            "findall"(
                                "Performance",
                                "train_cv_fold"("Dataset", 5, "LearnParams", "Performance"),
                                "AllPerformances"
                            )
                            )
                    },
                    /* Trains a NN once, for the k-th round of CV. */
                    /* Returns the Performance over the k-th validation set. */
                    rule {
                        "train_cv_fold"("Dataset", K, "LearnParams", "Performance") `if` (
                            "fold"("Dataset", K, "Train", "Validation") and
                                "train_validate"("Train", "Validation", "LearnParams", "Performance")
                            )
                    },
                    /* Trains a NN on the provided TrainingSet, using the provided Params, */
                    /* and computes its Performance over the provided ValidationSet. */
                    rule {
                        "train_validate"("TrainingSet", "ValidationSet", "LearnParams", "Performance") `if` (
                            "createModel"(7, 1, "NN") and
                                "train"("NN", "TrainingSet", "LearnParams", "TrainedNN") and
                                "test"("NN", "ValidationSet", "Performance")
                            )
                    },
                    // Computes the Performance of the provided NN against the provided ValidationSet
                    rule {
                        "test"("NN", "ValidationSet", "Performance") `if` (
                            "predict"("NN", "ValidationSet", "ActualPredictions") and
                                "mae"("ActualPredictions", "ValidationSet", "Performance")
                            )
                    },
                    // Computes the Performance of the provided NN against the provided ValidationSet
                    rule {
                        "preprocessing"("Dataset", "Labels", "Transformed") `if` (
                            "theory_to_schema"("Schema") and
                                "schema_transformation"("Schema", A) and
                                "normalize"(A, "Labels", B) and
                                "fit"(B, "Dataset", C) and
                                "transform"("Dataset", C, "Transformed")
                            )
                    }
                )
            )
        }
    }

    private val schemaName = "autoMpg"
    val path = "/auto-mpg.csv"

    @Test
    fun testDemo() {
        val startingTime = System.currentTimeMillis()
        val csv = readStrictCsv(path)
        solver.appendStaticKb(
            fromCSVtoTheory(
                schemaName,
                csv,
                arrayOf(csv.first().keys.first())
            )
        )
        logicProgramming {
            val performancesVar = Var.of("AllPerformances")
            solver.solveOnce(
                "getDataset"("Dataset") and
                    "preprocessing"("Dataset", csv.first().keys.drop(1).map { Atom.of(it) }, "Transformed") and
                    "train_cv"(
                        "Transformed",
                        arrayOf("max_epoch"(100), "loss"("mse")),
                        performancesVar
                    )
            ).let {
                println(it)
                assertTrue(it.isYes)
                assertFalse(it.substitution[performancesVar]!!.castToList().isEmptyList)
            }
        }
        println("Execution time was ${(System.currentTimeMillis() - startingTime) / 1000.0}")
    }
}

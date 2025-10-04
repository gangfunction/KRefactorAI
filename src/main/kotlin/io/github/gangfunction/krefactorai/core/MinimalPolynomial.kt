package io.github.gangfunction.krefactorai.core

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.EigenDecomposition
import org.apache.commons.math3.linear.RealMatrix
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Calculates the minimal polynomial of a matrix and uses it to determine module complexity
 */
class MinimalPolynomial {

    /**
     * Calculate complexity scores for modules based on the adjacency matrix
     * Uses eigenvalues and matrix properties to determine module importance
     */
    fun calculateComplexityScores(adjacencyMatrix: Array<DoubleArray>): DoubleArray {
        if (adjacencyMatrix.isEmpty()) {
            return doubleArrayOf()
        }

        val n = adjacencyMatrix.size
        logger.info { "Calculating complexity scores for $n modules" }

        try {
            val matrix = Array2DRowRealMatrix(adjacencyMatrix)
            
            // Calculate various metrics
            val eigenScores = calculateEigenvalueBasedScores(matrix)
            val degreeScores = calculateDegreeScores(adjacencyMatrix)
            val centralityScores = calculateCentralityScores(adjacencyMatrix)
            
            // Combine scores with weights
            val complexityScores = DoubleArray(n) { i ->
                0.4 * eigenScores[i] +
                0.3 * degreeScores[i] +
                0.3 * centralityScores[i]
            }
            
            // Normalize scores to [0, 1]
            val maxScore = complexityScores.maxOrNull() ?: 1.0
            if (maxScore > 0) {
                for (i in complexityScores.indices) {
                    complexityScores[i] /= maxScore
                }
            }
            
            logger.info { "Complexity scores calculated successfully" }
            return complexityScores
            
        } catch (e: Exception) {
            logger.error(e) { "Error calculating complexity scores" }
            // Return uniform scores as fallback
            return DoubleArray(n) { 0.5 }
        }
    }

    /**
     * Calculate scores based on eigenvalues and eigenvectors
     */
    private fun calculateEigenvalueBasedScores(matrix: RealMatrix): DoubleArray {
        val n = matrix.rowDimension
        
        try {
            val eigenDecomp = EigenDecomposition(matrix)
            val eigenvalues = eigenDecomp.realEigenvalues
            val eigenvectors = eigenDecomp.v
            
            // Use the dominant eigenvector (corresponding to largest eigenvalue)
            val maxEigenvalueIndex = eigenvalues.indices.maxByOrNull { eigenvalues[it] } ?: 0
            val dominantEigenvector = eigenvectors.getColumn(maxEigenvalueIndex)
            
            // Normalize to positive values
            val scores = DoubleArray(n) { i ->
                kotlin.math.abs(dominantEigenvector[i])
            }
            
            return normalizeScores(scores)
            
        } catch (e: Exception) {
            logger.warn(e) { "Failed to calculate eigenvalue-based scores, using fallback" }
            return DoubleArray(n) { 0.5 }
        }
    }

    /**
     * Calculate scores based on in-degree and out-degree
     */
    private fun calculateDegreeScores(adjacencyMatrix: Array<DoubleArray>): DoubleArray {
        val n = adjacencyMatrix.size
        val scores = DoubleArray(n)
        
        for (i in 0 until n) {
            // Out-degree: number of dependencies
            val outDegree = adjacencyMatrix[i].sum()
            
            // In-degree: number of dependents
            var inDegree = 0.0
            for (j in 0 until n) {
                inDegree += adjacencyMatrix[j][i]
            }
            
            // Higher score for modules with many dependents (high in-degree)
            // and few dependencies (low out-degree)
            scores[i] = inDegree * 2.0 + (n - outDegree)
        }
        
        return normalizeScores(scores)
    }

    /**
     * Calculate centrality scores (PageRank-like algorithm)
     */
    private fun calculateCentralityScores(adjacencyMatrix: Array<DoubleArray>): DoubleArray {
        val n = adjacencyMatrix.size
        val dampingFactor = 0.85
        val iterations = 20
        
        // Initialize scores
        var scores = DoubleArray(n) { 1.0 / n }
        
        // Power iteration
        repeat(iterations) {
            val newScores = DoubleArray(n)
            
            for (i in 0 until n) {
                var sum = 0.0
                for (j in 0 until n) {
                    if (adjacencyMatrix[j][i] > 0) {
                        val outDegree = adjacencyMatrix[j].sum()
                        if (outDegree > 0) {
                            sum += scores[j] * adjacencyMatrix[j][i] / outDegree
                        }
                    }
                }
                newScores[i] = (1 - dampingFactor) / n + dampingFactor * sum
            }
            
            scores = newScores
        }
        
        return normalizeScores(scores)
    }

    /**
     * Normalize scores to [0, 1] range
     */
    private fun normalizeScores(scores: DoubleArray): DoubleArray {
        val max = scores.maxOrNull() ?: 1.0
        val min = scores.minOrNull() ?: 0.0
        val range = max - min
        
        if (range == 0.0) {
            return DoubleArray(scores.size) { 0.5 }
        }
        
        return DoubleArray(scores.size) { i ->
            (scores[i] - min) / range
        }
    }

    /**
     * Calculate the characteristic polynomial coefficients
     * This is used for advanced analysis of the dependency structure
     */
    fun calculateCharacteristicPolynomial(adjacencyMatrix: Array<DoubleArray>): DoubleArray {
        if (adjacencyMatrix.isEmpty()) {
            return doubleArrayOf()
        }

        try {
            val matrix = Array2DRowRealMatrix(adjacencyMatrix)
            val eigenDecomp = EigenDecomposition(matrix)
            val eigenvalues = eigenDecomp.realEigenvalues
            
            logger.info { "Eigenvalues: ${eigenvalues.joinToString(", ") { "%.3f".format(it) }}" }
            
            return eigenvalues
            
        } catch (e: Exception) {
            logger.error(e) { "Error calculating characteristic polynomial" }
            return doubleArrayOf()
        }
    }

    /**
     * Analyze the dependency structure using matrix properties
     */
    fun analyzeStructure(adjacencyMatrix: Array<DoubleArray>): StructureAnalysis {
        val n = adjacencyMatrix.size
        
        if (n == 0) {
            return StructureAnalysis(
                moduleCount = 0,
                totalDependencies = 0,
                averageDependencies = 0.0,
                maxDependencies = 0,
                density = 0.0,
                isAcyclic = true
            )
        }

        val totalDeps = adjacencyMatrix.sumOf { row -> row.count { it > 0 } }
        val avgDeps = totalDeps.toDouble() / n
        val maxDeps = adjacencyMatrix.maxOf { row -> row.count { it > 0 } }
        val density = totalDeps.toDouble() / (n * (n - 1))
        
        // Check for cycles using matrix powers
        val isAcyclic = checkAcyclic(adjacencyMatrix)
        
        return StructureAnalysis(
            moduleCount = n,
            totalDependencies = totalDeps,
            averageDependencies = avgDeps,
            maxDependencies = maxDeps,
            density = density,
            isAcyclic = isAcyclic
        )
    }

    /**
     * Check if the graph is acyclic using matrix multiplication
     */
    private fun checkAcyclic(adjacencyMatrix: Array<DoubleArray>): Boolean {
        val n = adjacencyMatrix.size
        val matrix = Array2DRowRealMatrix(adjacencyMatrix)
        
        // If A^n has non-zero diagonal elements, there are cycles
        var power = matrix
        repeat(n) {
            for (i in 0 until n) {
                if (power.getEntry(i, i) > 0) {
                    return false
                }
            }
            power = power.multiply(matrix)
        }
        
        return true
    }
}

/**
 * Results of structure analysis
 */
data class StructureAnalysis(
    val moduleCount: Int,
    val totalDependencies: Int,
    val averageDependencies: Double,
    val maxDependencies: Int,
    val density: Double,
    val isAcyclic: Boolean
) {
    override fun toString(): String = buildString {
        appendLine("Structure Analysis:")
        appendLine("  Modules: $moduleCount")
        appendLine("  Total Dependencies: $totalDependencies")
        appendLine("  Average Dependencies: ${"%.2f".format(averageDependencies)}")
        appendLine("  Max Dependencies: $maxDependencies")
        appendLine("  Density: ${"%.2f".format(density)}")
        appendLine("  Acyclic: $isAcyclic")
    }
}


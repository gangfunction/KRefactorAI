package io.github.gangfunction.krefactorai.model

import kotlinx.serialization.Serializable

/**
 * Represents a dependency relationship between two modules
 */
@Serializable
data class Dependency(
    val from: Module,
    val to: Module,
    val weight: Double = 1.0,
    val type: DependencyType = DependencyType.DIRECT
) {
    override fun toString(): String = "$from -> $to (weight=$weight, type=$type)"
}

/**
 * Type of dependency
 */
@Serializable
enum class DependencyType {
    DIRECT,      // Direct import/dependency
    TRANSITIVE,  // Indirect dependency
    CIRCULAR     // Part of a circular dependency
}


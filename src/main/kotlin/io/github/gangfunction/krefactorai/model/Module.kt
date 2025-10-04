package io.github.gangfunction.krefactorai.model

import kotlinx.serialization.Serializable

/**
 * Represents a module or package in the dependency graph
 */
@Serializable
data class Module(
    val name: String,
    val path: String,
    val type: ModuleType = ModuleType.PACKAGE,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Module) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "Module(name='$name', path='$path', type=$type)"
}

/**
 * Type of module
 */
@Serializable
enum class ModuleType {
    PACKAGE,
    CLASS,
    GRADLE_MODULE,
    MAVEN_MODULE,
}

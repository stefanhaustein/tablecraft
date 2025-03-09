package org.kobjects.tablecraft.model

interface Dependable {
    val dependencies: MutableSet<Expression>
}
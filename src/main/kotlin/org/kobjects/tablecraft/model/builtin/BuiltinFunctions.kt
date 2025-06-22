package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.*

object BuiltinFunctions : Plugin {
    override val operationSpecs = listOf<AbstractArtifactSpec>(
        FunctionSpec(
            Type.DATE,
            "now",
            "The current local time",
            listOf(ParameterSpec("interval", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            NowFunction::create),

        FunctionSpec(
            Type.REAL,
            "pi",
            "The value of pi",
            emptyList()) { PiFunction },

        FunctionSpec(
            Type.BOOL,
            "toff",
            "Timed Off",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createToff),

        FunctionSpec(
            Type.BOOL,
            "ton",
            "Timed On",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createTon),

        FunctionSpec(
            Type.BOOL,
            "tp",
            "Timed Pulse",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedPulse::create),

        FunctionSpec(
            Type.BOOL,
            "rs",
            "RS-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL),
                ParameterSpec("r",  Type.BOOL)),
            emptySet(),
            0) { FlipflopFunction.createRs() },

        FunctionSpec(
            Type.BOOL,
            "sr",
            "SR-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL),
                ParameterSpec("r", Type.BOOL)),
            emptySet(),
            0) { FlipflopFunction.createSr() },

        FunctionSpec(
            Type.STRING,
            "statemachine",
            """A state machine specified by the given cell range. 
                |Rows consist of the current state, the transition condition and the new state""".trimMargin(),
            listOf(ParameterSpec("transitions", Type.RANGE, setOf(ParameterSpec.Modifier.REFERENCE)))
        ) {
            StateMachine.create(it)
        },

        RestOut.SPEC,

        OutputPortSpec(
            Type.RANGE,
            "NamedRange",
            "A named range of cells",
            emptyList(),
            emptySet(),
            0) {
                object : OutputPortInstance {
                    override fun setValue(value: Any) {}
                    override fun attach() {}
                    override fun detach() {}
                }
            }
    )
}
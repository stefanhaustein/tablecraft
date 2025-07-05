package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.*

object BuiltinFunctions : Plugin {
    override val operationSpecs = listOf<AbstractArtifactSpec>(
        FunctionSpec(
            "Time",
            Type.DATE,
            "now",
            "The current local time",
            listOf(ParameterSpec("interval", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            NowFunction::create,
        ),

        FunctionSpec(
            "Math",
            Type.REAL,
            "pi",
            "The value of pi",
            emptyList(),
            createFn = { PiFunction },
        ),

        FunctionSpec(
            "PLC",
            Type.BOOL,
            "toff",
            "Timed Off",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createToff,
        ),

        FunctionSpec(
            "PLC",

            Type.BOOL,
            "ton",
            "Timed On",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createTon,
        ),

        FunctionSpec(
            "PLC",

            Type.BOOL,
            "tp",
            "Timed Pulse",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedPulse::create,
        ),

        FunctionSpec(
            "PLC",

            Type.BOOL,
            "rs",
            "RS-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL),
                ParameterSpec("r",  Type.BOOL)),
            emptySet(),
            0,
        ) { FlipflopFunction.createRs() },

        FunctionSpec(
            "PLC",
            Type.BOOL,
            "sr",
            "SR-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL),
                ParameterSpec("r", Type.BOOL)),
            emptySet(),
            0,
        ) { FlipflopFunction.createSr() },

        FunctionSpec(
            "PLC",
            Type.STRING,
            "statemachine",
            """A state machine specified by the given cell range. 
                |Rows consist of the current state, the transition condition and the new state""".trimMargin(),
            listOf(ParameterSpec("transitions", Type.RANGE, setOf(ParameterSpec.Modifier.REFERENCE))),
            createFn = {
                StateMachine.create(it)
            },
        ),

        RestOut.SPEC,

        OutputPortSpec(
            Type.RANGE,
            "NamedCells",
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
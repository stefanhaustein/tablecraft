package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.*

object BuiltinFunctions : Plugin {
    override val operationSpecs = listOf(
        OperationSpec(
            OperationKind.FUNCTION,
            Type.DATE,
            "now",
            "The current local time",
            listOf(ParameterSpec("interval", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            NowFunction::create),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.REAL,
            "pi",
            "The value of pi",
            emptyList()) { PiFunction },

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOL,
            "toff",
            "Timed Off",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createToff),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOL,
            "ton",
            "Timed On",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createTon),


        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOL,
            "tp",
            "Timed Pulse",
            listOf(ParameterSpec("input", Type.BOOL), ParameterSpec("delay", Type.REAL, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedPulse::create),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOL,
            "rs",
            "RS-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL),
                ParameterSpec("r",  Type.BOOL)),
            emptySet(),
            0) { FlipflopFunction.createRs() },

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOL,
            "sr",
            "SR-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL),
                ParameterSpec("r", Type.BOOL)),
            emptySet(),
            0) { FlipflopFunction.createSr() },

        OperationSpec(
          OperationKind.FUNCTION,
            Type.STRING,
            "statemachine",
            """A state machine specified by the given cell range. 
                |Rows consist of the current state, the transition condition and the new state""".trimMargin(),
            listOf(ParameterSpec("transitions", Type.RANGE, setOf(ParameterSpec.Modifier.REFERENCE)))
        ) {
            StateMachine.create(it)
        },

        RestOut.SPEC

    )
}
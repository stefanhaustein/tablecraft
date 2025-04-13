package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.*

object BuiltinFunctions : Plugin {
    override val operationSpecs = listOf(
        OperationSpec(
            OperationKind.FUNCTION,
            Type.DATE,
            "now",
            "The current local time",
            listOf(ParameterSpec("interval", Type.NUMBER, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            NowFunction::create),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.NUMBER,
            "pi",
            "The value of pi",
            emptyList()) { PiFunction },

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "toff",
            "Timed Off",
            listOf(ParameterSpec("input", Type.BOOLEAN), ParameterSpec("delay", Type.NUMBER, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createToff),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "ton",
            "Timed On",
            listOf(ParameterSpec("input", Type.BOOLEAN), ParameterSpec("delay", Type.NUMBER, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedOnOff::createTon),


        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "tp",
            "Timed Pulse",
            listOf(ParameterSpec("input", Type.BOOLEAN), ParameterSpec("delay", Type.NUMBER, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            0,
            TimedPulse::create),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "rs",
            "RS-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOLEAN),
                ParameterSpec("r",  Type.BOOLEAN)),
            emptySet(),
            0) { FlipflopFunction.createRs() },

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "sr",
            "SR-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOLEAN),
                ParameterSpec("r", Type.BOOLEAN)),
            emptySet(),
            0) { FlipflopFunction.createSr() },

        OperationSpec(
          OperationKind.FUNCTION,
            Type.TEXT,
            "statemachine",
            """A state machine specified by the given cell range. 
                |Rows consist of the current state, the transition condition and the new state""".trimMargin(),
            listOf(ParameterSpec("transitions", Type.RANGE, setOf(ParameterSpec.Modifier.REFERENCE)))
        ) {
            StateMachine.create(it)
        },


        OperationSpec(
            OperationKind.FUNCTION,
            Type.IMAGE,
            "image",
            "Image reference",
            listOf(
                ParameterSpec("source", Type.TEXT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("state",  Type.BOOLEAN, setOf(ParameterSpec.Modifier.OPTIONAL))
                ),
            emptySet(),
            0,
            ImageFunction::create),


    )
}
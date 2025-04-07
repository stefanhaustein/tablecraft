package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.*

object BuiltinFunctions : Plugin {
    override val operationSpecs = listOf(
        OperationSpec(
            OperationKind.FUNCTION,
            Type.DATE,
            "now",
            "The current local time",
            listOf(ParameterSpec("interval", ParameterKind.CONFIGURATION, Type.NUMBER)),
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
            listOf(ParameterSpec("input", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("delay", ParameterKind.CONFIGURATION, Type.NUMBER)),
            0,
            TimedOnOff::createToff),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "ton",
            "Timed On",
            listOf(ParameterSpec("input", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("delay", ParameterKind.CONFIGURATION, Type.NUMBER)),
            0,
            TimedOnOff::createTon),


        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "tp",
            "Timed Pulse",
            listOf(ParameterSpec("input", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("delay", ParameterKind.CONFIGURATION, Type.NUMBER)),
            0,
            TimedPulse::create),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "rs",
            "RS-Flipflop",
            listOf(ParameterSpec("s", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("r", ParameterKind.RUNTIME, Type.BOOLEAN)),
            0) { FlipflopFunction.createRs() },

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "sr",
            "SR-Flipflop",
            listOf(ParameterSpec("s", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("r", ParameterKind.RUNTIME, Type.BOOLEAN)),
            0) { FlipflopFunction.createSr() },

        OperationSpec(
          OperationKind.INPUT_PORT,
            Type.TEXT,
            "stateMachine",
            """A state machine specified by the given cell range. 
                |Rows consist of the current state, the transition condition and the new state""".trimMargin(),
            listOf(ParameterSpec("transitions", ParameterKind.CONFIGURATION, Type.RANGE, true))
        ) {
            StateMachine.create(it)
        },


        OperationSpec(
            OperationKind.FUNCTION,
            Type.IMAGE,
            "image",
            "Image reference",
            listOf(
                ParameterSpec("source", ParameterKind.CONFIGURATION, Type.TEXT),
                ParameterSpec("state", ParameterKind.RUNTIME, Type.BOOLEAN, required = false)
                ),
            0,
            ImageFunction::create),


    )
}
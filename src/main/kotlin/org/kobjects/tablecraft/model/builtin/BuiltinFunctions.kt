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
            "Timed off",
            listOf(ParameterSpec("input", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("delay", ParameterKind.CONFIGURATION, Type.NUMBER)),
            0,
            TOnOffFunction::createToff),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "ton",
            "Timed on",
            listOf(ParameterSpec("input", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("delay", ParameterKind.CONFIGURATION, Type.NUMBER)),
            0,
            TOnOffFunction::createTon),


        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "rs",
            "RS-Flipflop",
            listOf(ParameterSpec("r", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("s", ParameterKind.RUNTIME, Type.BOOLEAN)),
            0,
            FlipflopFunction::createRs),

        OperationSpec(
            OperationKind.FUNCTION,
            Type.BOOLEAN,
            "sr",
            "SR-Flipflop",
            listOf(ParameterSpec("r", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("s", ParameterKind.RUNTIME, Type.BOOLEAN)),
            0,
            FlipflopFunction::createSr),


        OperationSpec(
            OperationKind.FUNCTION,
            Type.IMAGE,
            "image",
            "Image reference",
            listOf(ParameterSpec("source", ParameterKind.CONFIGURATION, Type.TEXT)),
            0,
            ImageFunction::create),


    )
}
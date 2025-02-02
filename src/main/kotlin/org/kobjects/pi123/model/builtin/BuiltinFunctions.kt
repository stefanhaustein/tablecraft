package org.kobjects.pi123.model.builtin

import org.kobjects.pi123.pluginapi.*

object BuiltinFunctions : Plugin {
    override val functionSpecs = listOf(
        FunctionSpec(
            "now",
            "The current local time",
            listOf(ParameterSpec("interval", ParameterKind.CONFIGURATION, Type.NUMBER)),
            NowFunction::create),

        FunctionSpec(
            "pi",
            "The value of pi",
            emptyList()) { PiFunction },

        FunctionSpec(
            "toff",
            "Delayed off",
            listOf(ParameterSpec("input", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("delay", ParameterKind.CONFIGURATION, Type.NUMBER)),
            TOnOffFunction::createToff),

        FunctionSpec(
            "ton",
            "Delayed on",
            listOf(ParameterSpec("input", ParameterKind.RUNTIME, Type.BOOLEAN), ParameterSpec("delay", ParameterKind.CONFIGURATION, Type.NUMBER)),
            TOnOffFunction::createTon),

        FunctionSpec(
            "image",
            "Image reference",
            listOf(ParameterSpec("source", ParameterKind.CONFIGURATION, Type.TEXT)),
            ImageFunction::create),


    )
}
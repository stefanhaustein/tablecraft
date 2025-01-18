package org.kobjects.pi123.model.expression

import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalInputConfig
import com.pi4j.io.gpio.digital.DigitalOutputConfig
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder
import com.pi4j.io.gpio.digital.PullResistance
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext

class DigitalOutputExpression(
    val parameters: Map<String, Expression>
) : Expression() {
    val address: Int

    override val children: Collection<Expression>
        get() = parameters.values

    init {

        val addressParameter = (parameters["address"] ?: parameters["0"])
        require(addressParameter is LiteralExpression)
        address = (addressParameter.value as Number).toInt()

    }

    override fun eval(context: RuntimeContext): Boolean {
        val state = parameters["state"] ?: parameters["1"]!!.evalDouble(context) != 0.0
        val digitalOutput = Model.digitalOutput.getOrPut(address){
            val digitalOutputConfig = DigitalOutput.newConfigBuilder(Model.pi4J).address(address).id("din"+(++number)).build()
            Model.pi4J.create(digitalOutputConfig)
        }
        println("***** setinggg state $state  for $digitalOutput")
        digitalOutput.setState(state)
        return state

    }

/*
    override fun attach() {
        digitalOutput =
    }

    override fun detach() {
        println("***** detaching $digitalOutput")
        digitalOutput.shutdown(Model.pi4J)

    }
*/

    companion object {
        var number = 0
}
}
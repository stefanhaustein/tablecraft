package org.kobjects.pi123.model.expression

import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalInputConfig
import com.pi4j.io.gpio.digital.PullResistance
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext

class DigitalInputExpression(
    val parameters: Map<String, Expression>
) : Expression() {
    lateinit var digitalInput: DigitalInput
    val config: DigitalInputConfig

    override val children: Collection<Expression>
        get() = parameters.values

    init {

        val addressParameter = (parameters["address"] ?: parameters["0"])
        require(addressParameter is LiteralExpression)

        val address = (addressParameter.value as Number).toInt()

        config = DigitalInput.newConfigBuilder(Model.pi4J)
            .address(address)
            .pull(PullResistance.PULL_DOWN)
            .debounce(1000L)
            .build()

    }

    override fun eval(context: RuntimeContext) =  digitalInput.isOn()

    override fun attach() {
        digitalInput = Model.pi4J.create(config)
        super.attach()

    }

    override fun detach() {
        digitalInput.shutdown(Model.pi4J)
        super.detach()
    }

}
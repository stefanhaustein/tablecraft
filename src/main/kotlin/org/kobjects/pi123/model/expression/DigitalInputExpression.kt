package org.kobjects.pi123.model.expression

import com.pi4j.io.gpio.digital.*
import org.kobjects.pi123.model.Cell
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext

class DigitalInputExpression(
    val target: Cell,
    val parameters: Map<String, Expression>
) : Expression() {
    lateinit var digitalInput: DigitalInput
    lateinit var listener: DigitalStateChangeListener
    val config: DigitalInputConfig

    override val children: Collection<Expression>
        get() = parameters.values

    init {

        val addressParameter = (parameters["address"] ?: parameters["0"])
        require(addressParameter is LiteralExpression)

        val address = (addressParameter.value as Number).toInt()

        config = DigitalInput.newConfigBuilder(Model.pi4J)
            .id("DIN-$address")
            .name("DIN-$address")
            .address(address)
            .pull(PullResistance.PULL_DOWN)
            .debounce(1000L)
            .build()

    }

    override fun eval(context: RuntimeContext) =  digitalInput.isOn()

    override fun attach() {
        digitalInput = Model.pi4J.create(config)
        println("digitatlinput $digitalInput create for $config; adding listener")
        listener = object : DigitalStateChangeListener {
            override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
                Model.withLock {
                    println("din update: $event")
                    target.updateAllDependencies(it)
                    Model.notifyContentUpdated(it)
                }
            }
        }
        digitalInput.addListener(listener)
    }

    override fun detach() {
        digitalInput.removeListener(listener)
        println("shuttonhg down digitatlinput $digitalInput")
        digitalInput.shutdown(Model.pi4J)
    }

}
package org.kobjects.pi123.model.expression

import com.pi4j.io.gpio.digital.*
import org.kobjects.pi123.model.Cell
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext

class PwmInputExpression(
    val target: Cell,
    val parameters: Map<String, Expression>
) : Expression() {
    lateinit var digitalInput: DigitalInput
    lateinit var listener: DigitalStateChangeListener
    val config: DigitalInputConfig
    var value: Double = 0.0
    var t0: Long = 0L

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
            .build()
    }

    override fun eval(context: RuntimeContext) =  value

    override fun attach() {
        digitalInput = Model.pi4J.create(config)
        listener = object : DigitalStateChangeListener {
            override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
                if (event!!.state().isHigh) {
                    t0 = System.currentTimeMillis()
                    println("pwm went high at $t0")
                } else {
                    value = (System.currentTimeMillis() - t0).toDouble()
                    println("pwm went low; dt: $value")
                    Model.withLock {
                        println("din update: $value")
                        target.updateAllDependencies(it)
                        Model.notifyContentUpdated(it)
                    }
                }
            }
        }
        digitalInput.addListener(listener)
    }

    override fun detach() {
        println("Detach: $digitalInput")
        digitalInput.removeListener(listener)
        Model.pi4J.shutdown<DigitalInput>(digitalInput.id())
        digitalInput.shutdown(Model.pi4J)
        println("Sucessfully detached")
    }

}
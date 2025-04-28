/**
 * Schema fields:
 *
 * - name: The name of the input element.
 * - options: Input options for this element
 * - type: The type of this input element. One of Boolean, Integer, Number, String, Enum.
 */

export class InputController {

    constructor(schema, inputElement) {
        this.schema = schema

        this.labelElement = document.createElement("label")
        this.labelElement.textContent = schema.name + (this.getType() != null ? " (" +  this.getType() + "):" : ":")

        this.inputElement = inputElement

        this.messageElement = document.createElement("div")
        this.messageElement.innerHTML = "&nbsp;"
        this.messageElement.style.fontSize = "smaller"
        this.messageElement.style.color = "red"
        this.messageElement.style.position = "relative"
        this.messageElement.style.top = "-0.25em"
        this.messageElement.style.lineHeight = "normal"
        this.messageElement.style.textAlign = "right"

        this.listeners = []

        this.inputElement.addEventListener("input", () => {
            this.validate()
            for (let listener of this.listeners) {
                listener(this.getValue(), this)
            }
        })

        this.validate()
    }

    static create(schema) {
        if (schema.modifiers != null && schema.modifiers.indexOf("CONSTANT") != -1) {
            if (schema.options != null) {
                return new EnumInputController(schema, schema.options)
            }
            if (schema.type.toLowerCase() == "bool") {
                return new EnumInputController(schema, ["True", "False"])
            }
        }

        return new TextInputController(schema)
    }

    addListener(listener) {
        this.listeners.push(listener)
    }


    isConstant() {
        let modifiers = this.schema.modifiers || []
        return modifiers.indexOf("CONSTANT") != -1
    }

    validate() {}

    getValue() {
        if (this.isConstant()) {
            switch (this.getType()) {
                case "bool":
                    return this.inputElement.value == "True"
                case "real":
                    return Number.parseFloat(this.inputElement.value)
                case "int":
                    return Number.parseInt(this.inputElement.value)

            }
        }
        return this.inputElement.value
    }

    setValue(value) {
        switch (this.getType()) {
            case "bool":
                this.inputElement.value = value ? "True" : "False"
                break
            default:
                this.inputElement.value = value
        }
        this.validate()
    }

    getType() {
        if (this.schema.type) {
            return this.schema.type.toString().toLowerCase()
        }
        if (this.schema.options) {
            return "enum"
        }
        return null
    }
}

class TextInputController extends InputController {

    constructor(schema) {
        super(schema, document.createElement("input"))

        switch(this.getType()) {
            case "real":
                this.validations = {"Number expected": /^[+-]?(\d+([.]\d*)?([eE][+-]?\d+)?|[.]\d+([eE][+-]?\d+)?)$/}
                break
            case "int":
                this.validations = {"Integer expected": /^[-+]?[0-9]+$/}
                break
        }
    }

    validate() {
        if (this.inputElement.value == "") {
            this.messageElement.textContent = "Required Field"
        } else {
            if (this.validations) {
                for (const msg in this.validations) {
                    if (!this.inputElement.value.match(this.validations[msg])) {
                        this.messageElement.textContent = msg
                        return
                    }
                }
            }
            this.messageElement.innerHTML = "&nbsp"
        }
    }
}


class EnumInputController extends InputController {

    constructor(schema, options) {
        super(schema, document.createElement("select"));

        for (let i in options) {
            let optionElement = document.createElement("option")
            optionElement.textContent = options[i]
            this.inputElement.appendChild(optionElement)
        }
    }


}

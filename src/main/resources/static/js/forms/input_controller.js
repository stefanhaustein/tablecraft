import {getType, getOptions, containsModifier} from "./input_schema.js";


/**
 * Schema fields:
 *
 * - options: Array of Input options for this element
 * - type: The type of this input element. One of Bool, Integer, Real, String, Enum.
 */

export class InputController {

    constructor(schema, inputElement) {
        this.schema = schema
        this.inputElement = inputElement


        this.labelElement = document.createElement("label")
        let label = schema.label != null ? schema.label
            : schema.name == null ? getType(schema) : schema.name + " (" + getType(schema) + ")"
        this.labelElement.textContent = label + ": "

        this.messageElement = document.createElement("div")
        this.messageElement.innerHTML = "&nbsp;"
        this.messageElement.style.fontSize = "smaller"
        this.messageElement.style.color = "red"
        this.messageElement.style.position = "relative"
        this.messageElement.style.top = "-0.25em"
        this.messageElement.style.lineHeight = "normal"
        this.messageElement.style.textAlign = "right"

        this.validation = schema.validation || {}

        this.inputElement.addEventListener("input", () => { this.validate() })

        this.validate()
    }

    static create(schema) {
        if (containsModifier(schema, "CONSTANT")) {
            let options = getOptions(schema)
            if (options != null) {
                return new EnumInputController(schema, schema.options || schema.type)
            }
            if (getType(schema).toLowerCase() == "bool") {
                return new EnumInputController(schema, ["True", "False"])
            }
        }
        return new TextInputController(schema)
    }

    isConstant() {
        return containsModifier(this.schema, "CONSTANT")
    }

    validate() {
        return true
    }

    getValue() {
        if (this.isConstant()) {
            switch (getType(this.schema).toLowerCase()) {
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
        if (this.isConstant() && getType(this.schema).toLowerCase() == "bool") {
            this.inputElement.value = value ? "True" : "False"
        } else {
            this.inputElement.value = value == null ? "" : value.toString()
        }
        this.validate()
    }
}

class TextInputController extends InputController {

    constructor(schema) {
        super(schema, document.createElement("input"))

        switch(getType(this.schema).toLowerCase()) {
            case "real":
                this.validation["Number expected"] = /^[+-]?(\d+([.]\d*)?([eE][+-]?\d+)?|[.]\d+([eE][+-]?\d+)?)$/
                break
            case "int":
                this.validation["Integer expected"] = /^[-+]?[0-9]+$/
                break
        }
    }

    validate() {
        let errorMessage = "\u00a0"
        if (this.inputElement.value == "") {
            errorMessage = "Required Field"
        } else {
            if (this.validation) {
                for (const msg in this.validation) {
                    let check = this.validation[msg]
                    if (check instanceof RegExp) {
                        if (!this.inputElement.value.match(check)) {
                            errorMessage = msg
                            break
                        }
                    } else if (!check(this.inputElement.value)) {
                        errorMessage = msg
                        break
                    }
                }
            }
        }
        this.messageElement.textContent = errorMessage
        return errorMessage == "\u00a0"
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

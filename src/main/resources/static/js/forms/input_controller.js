import {getType, getOptions, isLiteral, getTypeLabel} from "./input_schema.js";


/**
 * Schema fields:
 *
 * - options: Array of Input options for this element
 * - type: The type of this input element. One of Bool, Integer, Real, String, Enum.
 */

let labelCounter = 0

export class InputController {

    constructor(schema, inputElement, messageElement) {
        this.schema = schema
        this.inputElement = inputElement

        let id = "lbl" + (++labelCounter);
        this.inputElement.setAttribute("id", id)

        this.labelElement = document.createElement("label")
        this.labelElement.setAttribute("for", id)

        let label = schema.label != null ? schema.label
            : schema.name == null ? getTypeLabel(schema) : schema.name + " (" + getTypeLabel(schema) + ")"
        this.labelElement.textContent = label + ": "

        if (messageElement != null) {
            this.messageElement = messageElement
        } else {
            this.messageElement = document.createElement("div")
            this.messageElement.className = "errorMessage"
        }
        this.validation = schema.validation || {}

        this.inputElement.addEventListener("input", () => { this.validate() })

        this.validate()
    }

    static create(schema, messageElement) {
        if (!schema.isExpression && !schema.isReference) {
            let options = getOptions(schema)
            if (options != null) {
                return new EnumInputController(schema, options, messageElement)
            }
            if (getType(schema) == "Bool") {
                return new EnumInputController(schema, ["True", "False"], messageElement)
            }
        }
        return new TextInputController(schema, messageElement)
    }

    validate() {
        return true
    }

    getValue() {
        if (isLiteral(this.schema)) {
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
        /*if (isLiteral(this.schema) && getType(this.schema) == "Bool" && this.schema.options == null) {
            this.inputElement.value = value ? "True" : "False"
        } else {*/
            this.inputElement.value = value == null ? "" : value.toString()
        //}
        this.validate()
    }
}

class TextInputController extends InputController {

    constructor(schema, messageElement) {
        super(schema, document.createElement(schema?.isMultiLine ? "textarea" : "input"), messageElement)

        if (schema.isReference) {
            switch (getType(schema).toLowerCase()) {
                case "range":
                case "unspecified":
                    this.validation["Cell (range) reference expected"] = /^([a-zA-Z]+[a-zA-Z_0-9]*!)?[a-zA-Z]+[0-9]+(:[a-zA-Z]+[0-9]+)?$/
                    break

                default:
                    this.validation["Cell reference expected"] = /^([a-zA-Z]+[a-zA-Z_0-9]*!)?[a-zA-Z]+[0-9]+$/
            }

        } else if (isLiteral(this.schema)) {
            switch (getType(this.schema).toLowerCase()) {
                case "real":
                    this.validation["Number expected"] = /^[+-]?(\d+([.]\d*)?([eE][+-]?\d+)?|[.]\d+([eE][+-]?\d+)?)$/
                    break
                case "int":
                    this.validation["Integer expected"] = /^[-+]?[0-9]+$/
                    break
            }
        }
    }

    validate() {
        let errorMessage = "\u00a0"
        if (this.inputElement.value == "" && this.schema?.isRequired) {
            errorMessage = "Required Field"
        } else {
            if (this.getValue() != null) {
                if (this.schema.max != null && this.getValue() > this.schema.max) {
                    errorMessage = "Max. " + this.schema.max
                } else if (this.schema.min != null && this.getValue() < this.schema.min) {
                    errorMessage = "Min. " + this.schema.min
                }
            }

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
        let hasError = errorMessage != "\u00a0"
        if (hasError) {
            this.messageElement.classList.add("error")
            this.inputElement.classList.add("error")
        } else {
            this.messageElement.classList.remove("error")
            this.inputElement.classList.remove("error")
        }
        return hasError
    }
}


class EnumInputController extends InputController {

    constructor(schema, options) {
        super(schema, document.createElement("select"));

        for (let option of options) {
            let optionElement = document.createElement("option")
            if (option.label != null && option.value != null) {
                optionElement.textContent = option.label
                optionElement.value = option.value
            } else {
                optionElement.textContent = option
            }
            this.inputElement.appendChild(optionElement)
        }
    }


}

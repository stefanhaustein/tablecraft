import {getType, getOptions, isLiteral, getTypeLabel} from "./input_schema.js";


/**
 * Schema fields:
 *
 * - options: Array of Input options for this element
 * - type: The type of this input element. One of Bool, Integer, Real, String, Enum.
 */

let labelCounter = 0

export class InputController {

    constructor(schema, inputElement) {
        this.schema = schema
        this.inputElement = inputElement

        let id = "lbl" + (++labelCounter);
        this.inputElement.setAttribute("id", id)

        this.labelElement = document.createElement("label")
        this.labelElement.setAttribute("for", id)

        let label = schema.label != null ? schema.label
            : schema.name == null ? getTypeLabel(schema) : schema.name + " (" + getTypeLabel(schema) + ")"
        this.labelElement.textContent = label + ": "

        this.messageElement = document.createElement("div")
        this.messageElement.innerHTML = "&nbsp;"
        this.messageElement.style.fontSize = "12px"
        this.messageElement.style.color = "red"
        this.messageElement.style.position = "relative"
//        this.messageElement.style.top = "-0.25em"
  //      this.messageElement.style.lineHeight = "0.5"
        this.messageElement.style.textAlign = "right"
        this.messageElement.style.fontFamily = "PT Sans"

        this.validation = schema.validation || {}

        this.inputElement.addEventListener("input", () => { this.validate() })

        this.validate()
    }

    static create(schema) {
        if (!schema.isExpression && !schema.isReference) {
            let options = getOptions(schema)
            if (options != null) {
                return new EnumInputController(schema, options)
            }
            if (getType(schema) == "Bool") {
                return new EnumInputController(schema, ["True", "False"])
            }
        }
        return new TextInputController(schema)
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

    constructor(schema) {
        super(schema, document.createElement("input"))

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

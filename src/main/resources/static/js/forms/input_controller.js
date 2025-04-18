/**
 * Schema fields:
 *
 * - name: The name of the input element.
 * - options: Input options for this element
 * - type: The type of this input element. One of Boolean, Integer, Number, String, Enum.
 */
export class InputController {

    constructor(schema) {
        this.schema = schema
        this.listeners = []

        let options = this.getOptions()

        if (options) {
            this.element = document.createElement("select")
            for (let i in options) {
                let optionElement = document.createElement("option")
                optionElement.textContent = options[i]
                this.element.appendChild(optionElement)
            }
        } else {
            this.element = document.createElement("input")
            console.log("input element schema: ", schema)
            switch(this.getType()) {
                case "number":
                    break
                case "int":
                    this.element.setAttribute("pattern", "[\\-\\+]?[0-9]+")
                    break
            }
        }

        this.element.addEventListener("change", () => {
            for (let listener of this.listeners) {
                listener(this.getValue(), this)
            }
        })
    }


    addListener(listener) {
        this.listeners.push(listener)
    }

    isConstant() {
        let modifiers = this.schema.modifiers || []
        return modifiers.indexOf("CONSTANT") != -1
    }

    getValue() {
        if (this.isConstant()) {
            switch (this.getType()) {
                case "boolean":
                    return this.element.value == "True"
                case "number":
                    return Number.parseFloat(this.element.value)
                case "int":
                    return Number.parseInt(this.element.value)

            }
        }
        return this.element.value
    }

    setValue(value) {
        switch (this.getType()) {
            case "boolean":
                this.element.value = value ? "True" : "False"
                break
            default:
                this.element.value = value
        }
    }

    getType() {
        if (this.schema.type) {
            return this.schema.type.toString().toLowerCase()
        }
        let options = this.getOptions()
        if (options) {
            return "enum"
        }
        return "string"
    }

    getOptions() {
        if (Array.isArray(this.schema)) {
            return this.schema
        }
        if (this.isConstant() && this.schema.type == "Boolean") {
            return ["True", "False"]
        }
        return this.schema.options
    }

}

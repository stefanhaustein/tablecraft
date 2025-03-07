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

    getValue() {
        switch(this.getType()) {
            case "Boolean":
                return this.element.value == "True"
            case "Number":
                return Number.parseFloat(this.element.value)
            case "Integer":
                return Number.parseInt(this.element.value)
            default:
                return this.element.value

        }
    }

    setValue(value) {
        switch (this.getType()) {
            case "Boolean":
                this.element.value = value ? "True" : "False"
                break
            default:
                this.element.value = value
        }
    }

    getType() {
        if (this.schema.type) {
            return this.schema.type
        }
        let options = this.getOptions()
        if (options) {
            return "Enum"
        }
        return this.schema || "String"
    }

    getOptions() {
        if (Array.isArray(this.schema)) {
            return this.schema
        }
        if (this.schema.type == "Boolean") {
            return ["True", "False"]
        }
        return this.schema.options
    }

}

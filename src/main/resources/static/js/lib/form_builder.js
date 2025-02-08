
export class InputController {
    constructor(elementControllers, listeners) {
        this.elementControllers = elementControllers
        this.listeners = listeners
    }

    addListener(listener) {
        this.listeners.push(listener)
    }

    setValues(map) {
        for (let key in map) {
            let elementController = this.elementControllers[key]
            if (elementController) {
                elementController.setValue(map[key])
            }
        }
    }

    getValues() {
        let result = {}
        for (let name in this.elementControllers) {
            result[name] = this.elementControllers[name].getValue()
        }
        return result
    }



    static create(rootElement, schema) {
        let result = {}
        let listeners = []
        for (const entry of schema) {
            let label = document.createElement("label")
            label.style.display = "block"
            let name = entry["name"]
            label.textContent = name
            rootElement.appendChild(label)

            let inputController = createFormElement(entry, name)
            rootElement.appendChild(inputController.element)
            result[name] = inputController

            inputController.element.addEventListener("change", () => {
                console.log("change event")
                for (let listener of listeners) {
                    listener(name, inputController.getValue())
                }
            })
        }
        return new InputController(result, listeners)
    }
}


class InputElementController {

    constructor(element, name, schema) {
        this.element = element
        this.name = name
        this.schema = schema
    }


    getValue() {
        return this.element.value
    }

    setValue(value) {
        this.element.value = value
    }

}

function createFormElement(schema, key) {

    let options = getOptions(schema)

    let result = null
    if (options) {
        result = document.createElement("select")
        for (let i in options) {
            let optionElement = document.createElement("option")
            optionElement.textContent = options[i]
            result.appendChild(optionElement)
        }
        if (schema.required) {
            value[key] = options[0]
        }
    } else {
        result = document.createElement("input")
    }

    return new InputElementController(result, key)
}

function getType(elementSchema) {
    if (elementSchema.type) {
        return elementSchema.type
    }
    let options = getOptions(elementSchema)
    if (options) {
        return "Enum"
    }
    return elementSchema || "String"
}

function getOptions(elementSchema) {
    if (Array.isArray(elementSchema)) {
        return elementSchema
    }
    return elementSchema.options
}

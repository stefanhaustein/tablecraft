function addInputElements(rootElement, schema, value) {
    for (const key in schema) {
        let label = document.createElement("label")
        label.style.display = "block"
        label.innerText = key
        rootElement.appendChild(label)

        let elementSchema = schema[key]
        let inputElement = createInputElement(elementSchema, key, value)
        rootElement.appendChild(inputElement)

    }
}

function createInputElement(schema, key, value) {

    let options = getOptions(schema)
    let elementValue = value[key]
    if (elementValue == null) {
        elementValue = schema.defaultValue
    }
    let result = null
    if (options) {
        result = document.createElement("select")
        for (let i in options) {
            let optionElement = document.createElement("option")
            optionElement.innerText = options[i]
            result.appendChild(optionElement)
        }
        if (schema.required) {
            value[key] = options[0]
        }
    } else {
        result = document.createElement("input")
    }
    if (elementValue != null && schema.required) {
        result.value = elementValue
    }
    result.addEventListener("change", () => {
        value[key] = result.value
        console.log("updated result: ", value)
    })
    return result
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

export function addInputElements(rootElement, schema, values) {
    for (const entry of schema) {
        let label = document.createElement("label")
        label.style.display = "block"
        let name = entry["name"]
        label.innerText = name
        rootElement.appendChild(label)

        let inputElement = createInputElement(entry, name, values)
        rootElement.appendChild(inputElement)

    }
}

function createInputElement(schema, key, values) {

    let options = getOptions(schema)
    let elementValue = values[key]
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
        values[key] = result.value
        console.log("updated result: ", values)
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

import {FormController} from "./forms/form_builder.js";
import {addCellSelectionListener, currentCell, setCurrentCellValidation} from "./shared_state.js";
import {getType} from "./forms/input_schema.js";

let inputTypeSelect = document.getElementById("inputType")

let currentValidation = {}

inputTypeSelect.addEventListener("change", () => {
    currentValidation.type = inputTypeSelect.value
    updateValidation(false)
})

addCellSelectionListener(initValidation)

function initValidation() {
    currentValidation = currentCell?.v == null ? {} : {...currentCell.v}
    // Map fields for input form
    switch (getType(currentValidation)) {
        case "Bool":
            currentValidation["true"] = currentValidation?.options[0]?.label
            currentValidation["false"] = currentValidation?.options[1]?.label
            break
        case "String":
            if (Array.isArray(currentValidation.options)) {
                currentValidation.optionsString = currentValidation.options.join(", ")
            }
            break
    }

    updateValidation(true)
}


function updateValidation(cellChanged) {
    let schema = []
    let type = currentValidation.type || "No User Input"
    inputTypeSelect.value = type
    let explanationDiv = document.getElementById("ValidationExplanation")
    explanationDiv.textContent = ""

    switch (type) {
        case "Bool":
            schema = [
                {name: "true", label: "True Label"},
                {name: "false", label: "False Label"},
            ]
            currentValidation["true"] = currentValidation["true"] || "True"
            currentValidation["false"] = currentValidation["false"] || "False"
            break
        case "String":
            schema = [{name: "optionsString", label: "Permitted values", isMultiLine: true}]
            explanationDiv.textContent = "Comma separated values; leave empty for unrestricted text input."
            break
        case "Int":
            schema = [{name: "min", type: "Int"}, {name: "max", type: "Int"}]
            break
        case "Real":
            schema = [{name: "min", type: "Real"}, {name: "max", type: "Real"}]
            break
    }

    let validationFormElement = document.getElementById("ValidationForm")
    validationFormElement.textContent = ""

    let formController = FormController.create(validationFormElement, schema)
    formController.setValue(currentValidation)

    let saveFunction = () => {
        let newValues = formController.getValue()
        for (let key in newValues) {
            currentValidation[key] = newValues[key]
        }

        if (inputTypeSelect.selectedIndex == 0) {
            setCurrentCellValidation(null)
        } else {
            let sendValues = {type: inputTypeSelect.value}
            switch (getType(currentValidation)) {
                case "Bool":
                    sendValues.options = [
                        {label: currentValidation["true"], value: true},
                        {label: currentValidation["false"], value: false}]
                    break
                case "String":
                    if (currentValidation.optionsString != null &&
                        currentValidation.optionsString.toString().trim() != "") {
                        sendValues.options = currentValidation.optionsString.split(",").map(s => s.trim())
                    }
                    break
                case "Int":
                case "Real":
                    if (currentValidation.min != null && currentValidation.min.toString().trim() != "") {
                        sendValues.min = currentValidation.min
                    }
                    if (currentValidation.max != null && currentValidation.max.toString().trim() != "") {
                        sendValues.max = currentValidation.max
                    }
                    break
            }
            setCurrentCellValidation(sendValues)
        }
    }

    formController.addListener(saveFunction)

    if (!cellChanged) {
        saveFunction()
    }
}

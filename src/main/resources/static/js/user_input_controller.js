import {FormController} from "./forms/form_builder.js";
import {addCellSelectionListener, currentCell, setCurrentCellValidation} from "./shared_state.js";
import {getType} from "./forms/input_schema.js";

let inputTypeSelect = document.getElementById("inputType")

// TODO: listen on selections; add something to be called by sync

inputTypeSelect.addEventListener("change", () => {
    setValidation({type: inputTypeSelect.value}, true)
})

addCellSelectionListener(() => {
    let validation = currentCell.v || {}
    setValidation(validation)
})


function setValidation(validation, saveImmediately) {
    let schema = []
    validation = validation == null ? {} : {...validation}
    let type = validation.type || "No User Input"
    inputTypeSelect.value = type

    if (Array.isArray(validation.options)) {
        if (getType(validation) == "Bool") {
            validation["true"] = validation.options[0].label
            validation["false"] = validation.options[1].label
        } else {
            validation.options = validation.options.join(", ")
        }
    }

    switch (type) {
        case "Bool":
            schema = [
                {name: "true", label: "True Label"},
                {name: "false", label: "False Label"},
            ]
            break
        case "String":
            schema = [{name: "options", label: "Permitted values", isMultiLine: true}]
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
    formController.setValues(validation)

    let saveFunction = () => {
        if (inputTypeSelect.selectedIndex == 0) {
            setCurrentCellValidation(null)
        } else {
            let values = formController.getValues()
            values.type = inputTypeSelect.value
            if (getType(values) == "Bool") {
                values.options = [{label: values["true"], value: true}, {label: values["false"], value: false}]
                delete values["true"]
                delete values["false"]
            } else if (values.options != null) {
                if (values.options.trim() == "") {
                    delete values.options
                } else {
                    values.options = values.options.split(",").map(s => s.trim())
                }
            }
            setCurrentCellValidation(values)
        }
    }

    formController.addListener(saveFunction)

    if (saveImmediately) {
        saveFunction()
    }
}
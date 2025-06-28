import {FormController} from "./forms/form_builder.js";
import {addCellSelectionListener, currentCell, setCurrentCellValidation} from "./shared_state.js";

let inputTypeSelect = document.getElementById("inputType")

// TODO: listen on selections; add something to be called by sync

inputTypeSelect.addEventListener("change", () => {
    setValidation({type: inputTypeSelect.value})
})

addCellSelectionListener(() => {
    let validation = currentCell.v || {}
    setValidation(validation)
})


function setValidation(validation) {
    let schema = []
    validation = validation == null ? {} : {...validation}
    let type = validation.type || "No User Input"
    inputTypeSelect.value = type

    if (Array.isArray(validation.options)) {
        validation.options = validation.options.join()
    }

    switch (type.toLowerCase()) {
        case "boolean":
            schema = [
                {"name": "true", "label": "True Label"},
                {"name": "false", "label": "False Label"},
            ]
            break
        case "string":
            schema = [{"name": "options", "label": "Permitted values"}]
            break
        case "integer":
            schema = [{"name": "min"}, {"name": "max"}]
            break
        case "real":
            schema = [{"name": "min"}, {"name": "max"}]
            break
    }

    let validationFormElement = document.getElementById("ValidationForm")
    validationFormElement.textContent = ""

    let formController = FormController.create(validationFormElement, schema)
    formController.setValues(validation)

    formController.addListener(() => {
        if (inputTypeSelect.selectedIndex == 0) {
            setCurrentCellValidation(null)
        } else {
            let values = formController.getValues()
            values.type = inputTypeSelect.value
            if (values.options != null) {
                if (values.options.trim() == "") {
                    delete values.options
                } else {
                    values.options = values.options.split(",").map(s => s.trim())
                }
            }
            setCurrentCellValidation(values)
        }
    })
}
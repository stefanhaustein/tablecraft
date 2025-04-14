import {FormController} from "./forms/form_builder.js";
import {sendJson} from "./lib/util.js";

let portListContainer = document.getElementById("portListContainer")
let portEditorContainer = document.getElementById("portEditorContainer")

function hidePortDialog() {
    portEditorContainer.style.display = "none"
    portListContainer.style.display = "block"
}

export function showPortDialog(constructorSpec, portSpec) {

    portEditorContainer.style.display = "block"
    portListContainer.style.display = "none"

    let instanceSpec = portSpec != null ? portSpec["configuration"] : {}

    portEditorContainer.textContent = ""
    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = "IO-Port Specification"
    portEditorContainer.appendChild(dialogTitleElement)

    let inputDiv = document.createElement("div")
    //inputDiv.className = "dialogFields"

    let portSchema = [{"name": "name"}]
    if (constructorSpec["kind"] == "OUTPUT_PORT") {
        portSchema.push({"name": "expression"})
    }
    let previousName = portSpec == null ? null : portSpec["name"]

    let portFormController = FormController.create(inputDiv, portSchema)
    portFormController.setValues(portSpec)

    let typeLabelElement = document.createElement("label")
    typeLabelElement.textContent = "binding"
    inputDiv.appendChild(typeLabelElement)

    let typeNameElement = document.createElement("div")
    typeNameElement.textContent = constructorSpec.name
    inputDiv.appendChild(typeNameElement)

    let bindingFormController = FormController.create(inputDiv, constructorSpec["params"])

    if (instanceSpec != null) {
        bindingFormController.setValues(instanceSpec)
    }

    portEditorContainer.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = "Ok"
    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        let values = portFormController.getValues()
        values["configuration"] = bindingFormController.getValues()
        values["type"] = constructorSpec["name"]
        values["previousName"] = previousName
        if (sendPort(values)) {
            hidePortDialog()
        }
    })
    buttonDiv.appendChild(okButton)

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "Cancel"
    cancelButton.className = "dialogButton"
    cancelButton.addEventListener("click", () => { hidePortDialog() })
    buttonDiv.appendChild(cancelButton)

    if (name != null) {
        let deleteButton = document.createElement("button")
        deleteButton.textContent = "Delete"
        deleteButton.className = "dialogButton"
        deleteButton.addEventListener("click", () => {
            sendJson("updatePort", {previousName: name})
            hidePortDialog()
        })
        buttonDiv.appendChild(deleteButton)
    }

    portEditorContainer.appendChild(buttonDiv)
}


function sendPort(definition) {
    sendJson("updatePort?name=" + definition["name"], definition)
    return true
}
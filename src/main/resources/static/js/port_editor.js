import {FormController} from "./forms/form_builder.js";
import {sendJson} from "./lib/util.js";
import {functions} from "./shared_state.js";

let portListContainer = document.getElementById("portListContainer")
let portEditorContainer = document.getElementById("portEditorContainer")

function hidePortDialog() {
    portEditorContainer.style.display = "none"
    portListContainer.style.display = "block"
}

function renderBinding(targetDiv, constructorSpec, instanceSpec) {
    targetDiv.textContent = ""

    let bindingFormController = FormController.create(targetDiv, constructorSpec["params"])

    if (instanceSpec != null) {
        bindingFormController.setValues(instanceSpec)
    }

    return bindingFormController
}

export function showPortDialog(constructorSpec, portSpec) {
    let kind = constructorSpec.kind
    let instanceSpec = portSpec != null ? portSpec.configuration : {}

    portEditorContainer.style.display = "block"
    portEditorContainer.textContent = ""

    portListContainer.style.display = "none"

    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = portSpec == null ? "Add " : "Edit "

    let inputDiv = document.createElement("div")
    //inputDiv.className = "dialogFields"

    let portSchema = [{"name": "name"}]
    if (kind == "OUTPUT_PORT") {
        portSchema.push({"name": "expression"})
        dialogTitleElement.append("Output Port")
    } else {
        dialogTitleElement.append("Input Port")
    }
    portEditorContainer.appendChild(dialogTitleElement)

    let previousName = portSpec == null ? null : portSpec["name"]

    let portFormController = FormController.create(inputDiv, portSchema)
    portFormController.setValues(portSpec == null ? {name: kind.substring(0, 1).toLowerCase() + "_"} : portSpec)

    let typeLabelElement = document.createElement("label")
    typeLabelElement.textContent = "binding"
    inputDiv.appendChild(typeLabelElement)

    let bindingDiv = document.createElement("div")

    let typeSelectElement = document.createElement("select")

    let okButton = document.createElement("button")
    if (portSpec == null) {
        okButton.textContent = "Create"
    } else {
        okButton.textContent = "Ok"
    }


    for (let name in functions) {
        let f = functions[name]
        if (f.kind == kind) {
            let typeOptionElement = document.createElement("option")
            typeOptionElement.textContent = name
            if (constructorSpec != null && name == constructorSpec.name) {
                typeOptionElement.setAttribute("selected", "true")
            }
            typeSelectElement.appendChild(typeOptionElement)
        }
    }
    let bindingFormController = renderBinding(bindingDiv, constructorSpec, instanceSpec)

    typeSelectElement.addEventListener("input", () => {
        let type = typeSelectElement.value
        constructorSpec = functions[type]
        if (constructorSpec != null) {
            bindingFormController = renderBinding(bindingDiv, constructorSpec, instanceSpec)
            if (typeSelectElement.firstElementChild.textContent == "(select)") {
                typeSelectElement.removeChild(typeSelectElement.firstElementChild)
                okButton.disabled = false
            }
        }
    })

    let typeSelectContainerElement = document.createElement("div")
    typeSelectContainerElement.className = "inputContainer"
    typeSelectContainerElement.style.paddingBottom = "18px"
    typeSelectContainerElement.appendChild(typeSelectElement)

    inputDiv.appendChild(typeSelectContainerElement)
    inputDiv.appendChild(bindingDiv)

    portEditorContainer.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

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

    if (previousName != null) {
        let deleteButton = document.createElement("button")
        deleteButton.textContent = "Delete"
        deleteButton.className = "dialogButton"
        deleteButton.addEventListener("click", () => {
            sendJson("updatePort", {previousName: previousName})
            hidePortDialog()
        })
        buttonDiv.appendChild(deleteButton)
    }

    portEditorContainer.appendChild(buttonDiv)
}


function sendPort(definition) {
    sendJson("updatePort", definition)
    return true
}
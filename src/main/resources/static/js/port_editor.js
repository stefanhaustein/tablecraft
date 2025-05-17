import {FormController} from "./forms/form_builder.js";
import {postJson} from "./lib/util.js";
import {getAllFactories, getFactory, getFunction, getIntegrationInstance, getPortInstance} from "./shared_model.js";

let portListContainer = document.getElementById("portListContainer")
let portEditorContainer = document.getElementById("portEditorContainer")

function hidePortDialog() {
    portEditorContainer.style.display = "none"
    portListContainer.style.display = "block"
}

function renderBinding(targetDiv, constructorSpec, instanceSpec) {
    targetDiv.textContent = constructorSpec.description
    targetDiv.appendChild(document.createElement("p"))

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

    let portSchema = [{
        "name": "name",
        "validation": {
            "Integration name conflict": (name) => getIntegrationInstance(name) == null,
            "Factory name conflict": (name) => getFactory(name) == null,
            "Function name conflict": (name) => getFunction(name) == null,
            "Port name conflict": (name) => getPortInstance(name) == null || (portSpec != null && name.toLowerCase() == portSpec.name.toLowerCase()),
            "Valid: letters, '_', digits after '_'": /^[a-zA-Z]+(_[a-zA-Z0-9_]*)?$/
        }}]

    if (kind == "OUTPUT_PORT") {
        portSchema.push({"name": "source"})
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
    okButton.textContent = portSpec == null ? "Create" : "Ok"

    for (let f in getAllFactories()) {
        let name = f.name
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
        constructorSpec = factories[type.toLowerCase()]
        if (constructorSpec != null) {
            bindingFormController = renderBinding(bindingDiv, constructorSpec, instanceSpec)
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
        postJson("ports/" + values["name"], values)
        hidePortDialog()
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
            postJson("ports/" + previousName, {deleted: true})
            hidePortDialog()
        })
        buttonDiv.appendChild(deleteButton)
    }

    portEditorContainer.appendChild(buttonDiv)
}


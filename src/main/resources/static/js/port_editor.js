import {FormController} from "./forms/form_builder.js";
import {getAllFactories, getFactory, getFunction, getIntegrationInstance, getPortInstance} from "./shared_model.js";
import {currentSheet} from "./shared_state.js";
import {selectPanel} from "./menu_controller.js";
import {post} from "./lib/utils.js";


let portEditorContainer = document.getElementById("portEditorContainer")
let previousPanel = ""

function hidePortDialog() {
   selectPanel(previousPanel)
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
    previousPanel = selectPanel("PortEditor")

    let kind = constructorSpec.kind
    let instanceSpec = portSpec != null ? portSpec.configuration : {}

    portEditorContainer.textContent = ""

    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = portSpec == null ? "Add " : "Edit "

    let inputDiv = document.createElement("div")
    //inputDiv.className = "dialogFields"

    let portSchema = [{
        "name": "name",
        "type": "String",
        "modifiers": ["CONSTANT"],
        "validation": {
            "Integration name conflict": (name) => getIntegrationInstance(name) == null,
            "Factory name conflict": (name) => getFactory(name) == null,
            "Function name conflict": (name) => getFunction(name) == null,
            "Port name conflict": (name) => getPortInstance(name) == null || (portSpec != null && name.toLowerCase() == portSpec.name.toLowerCase()),
            "Valid: letters, '_', digits after '_'": /^[a-zA-Z]+(_[a-zA-Z0-9_]*)?$/
        }}]


    if (kind == "OUTPUT_PORT") {
        portSchema.push({"name": "source", modifiers: ["REFERENCE"]})
        dialogTitleElement.append(constructorSpec.name == "NamedCells" ? "Named Cell(s)" : "Output Port")
    } else {
        dialogTitleElement.append("Input Port")
    }
    portEditorContainer.appendChild(dialogTitleElement)

    let previousName = portSpec == null ? null : portSpec["name"]

    let portFormController = FormController.create(inputDiv, portSchema)
    portFormController.setValues(portSpec == null ? {name: kind.substring(0, 1).toLowerCase() + "_"} : portSpec)

    let bindingFormController = null
    if (constructorSpec.name != "NamedCells") {
        let typeLabelElement = document.createElement("label")
        typeLabelElement.textContent = "binding"
        inputDiv.appendChild(typeLabelElement)

        let bindingDiv = document.createElement("div")
        let typeSelectElement = document.createElement("select")

        for (let f of getAllFactories()) {
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
        bindingFormController = renderBinding(bindingDiv, constructorSpec, instanceSpec)

        typeSelectElement.addEventListener("input", () => {
            let type = typeSelectElement.value
            constructorSpec = getFactory(type)
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
    }
    portEditorContainer.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = portSpec == null ? "Create" : "Ok"

    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        let values = portFormController.getValues()
        let source = values["source"]
        if (source != null && source.indexOf("!") == -1) {
            values["source"] = currentSheet.name + "!" + source
        }
        if (bindingFormController != null) {
            values["configuration"] = bindingFormController.getValues()
        }
        values["type"] = constructorSpec["name"]
        values["previousName"] = previousName
        post("ports/" + values["name"], values)
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
            post("ports/" + previousName, {deleted: true})
            hidePortDialog()
        })
        buttonDiv.appendChild(deleteButton)
    }

    portEditorContainer.appendChild(buttonDiv)
}


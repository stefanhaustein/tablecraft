import {functions, integrations} from "./shared_state.js";
import {FormController} from "./forms/form_builder.js";
import {sendJson} from "./lib/util.js";

let integrationSelectElement = document.getElementById("integrationSelect")
let integrationListElement = document.getElementById("integrationList")
let dialogElement = document.getElementById("dialog")

integrationSelectElement.addEventListener("change", addIntegration)
integrationListElement.addEventListener("click", event => editIntegration(event))

function addIntegration() {

    let type = integrationSelectElement.value
    integrationSelectElement.selectedIndex = 0

    let typeSpec = functions[type]

    console.log("add integration", type, typeSpec)

    showIntegrationDialog(typeSpec)
}


function editIntegration(event) {
    let entryElement = event.target
    let id = entryElement.id
    if (!id.startsWith("integration.")) {
        // Clicked on input; not handled here.
        // console.log("Target element id not recognized: ", entryElement)
        return
    }
    let name = id.substring("integration.".length)
    let integrationSpec = integrations[name]
    let constructorSpec = functions[integrationSpec.type]

    showIntegrationDialog(constructorSpec, integrationSpec)
}

function showIntegrationDialog(constructorSpec, integrationSpec) {

    let configuration = integrationSpec != null ? integrationSpec["configuration"] : {}
    dialogElement.textContent = ""
    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = "Integration Specification"
    dialogElement.appendChild(dialogTitleElement)

    let inputDiv = document.createElement("div")
    inputDiv.className = "dialogFields"

    let integrationSchema = [{"name": "name"}]

    let previousName = integrationSpec == null ? null : integrationSpec["name"]

    let integrationFormController = FormController.create(inputDiv, integrationSchema)
    integrationFormController.setValues(integrationSpec)

    let typeLabelElement = document.createElement("label")
    typeLabelElement.textContent = "binding"
    inputDiv.appendChild(typeLabelElement)

    let typeNameElement = document.createElement("div")
    typeNameElement.textContent = constructorSpec.name
    inputDiv.appendChild(typeNameElement)

    let bindingFormController = FormController.create(inputDiv, constructorSpec["params"])

    if (configuration != null) {
        bindingFormController.setValues(configuration)
    }

    dialogElement.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = "Ok"
    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        let values = integrationFormController.getValues()
        values["configuration"] = bindingFormController.getValues()
        values["type"] = constructorSpec["name"]
        values["previousName"] = previousName
        if (sendPort(values)) {
            dialogElement.close()
        }
    })
    buttonDiv.appendChild(okButton)

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "Cancel"
    cancelButton.className = "dialogButton"
    cancelButton.addEventListener("click", () => { dialogElement.close() })
    buttonDiv.appendChild(cancelButton)

    if (previousName != null) {
        let deleteButton = document.createElement("button")
        deleteButton.textContent = "Delete"
        deleteButton.className = "dialogButton"
        deleteButton.addEventListener("click", () => {
            sendJson("updateIntegration", {previousName: previousName})
            dialogElement.close()
        })
        buttonDiv.appendChild(deleteButton)
    }

    dialogElement.appendChild(buttonDiv)
    dialogElement.showModal()
}


function sendPort(definition) {
    sendJson("updateIntegration?name=" + definition["name"], definition)
    return true
}
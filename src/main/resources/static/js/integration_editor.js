import {FormController} from "./forms/form_builder.js";

import {post, transformSchema} from "./lib/utils.js";

let integrationListElement = document.getElementById("integrationList")
let dialogElement = document.getElementById("dialog")


export function showIntegrationInstanceConfigurationDialog(spec, instance) {
    let instanceConfiguration = instance["configuration"]
    let isNewInstance = instanceConfiguration == null
    if (isNewInstance) {
        instanceConfiguration = {}
    }

    dialogElement.textContent = ""
    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = "Configure " + instance.name + (instance.name != instance.type ? " (" + instance.type + ")" : "")
    dialogElement.appendChild(dialogTitleElement)

    let inputDiv = document.createElement("div")
    inputDiv.className = "dialogFields"

    let bindingFormController = FormController.create(inputDiv, transformSchema(spec["params"]))

    bindingFormController.setValue(instanceConfiguration)

    dialogElement.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = "Ok"
    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        instance["configuration"] = bindingFormController.getValue()
        if (sendIntegration(instance)) {
            dialogElement.close()
        }
    })
    buttonDiv.appendChild(okButton)

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "Cancel"
    cancelButton.className = "dialogButton"
    cancelButton.addEventListener("click", () => { dialogElement.close() })
    buttonDiv.appendChild(cancelButton)

    if (!isNewInstance) {
        let deleteButton = document.createElement("button")
        deleteButton.textContent = "Delete"
        deleteButton.className = "dialogButton"
        deleteButton.addEventListener("click", () => {
            post("integrations/" + instance["name"], {deleted: true})
            dialogElement.close()
        })
        buttonDiv.appendChild(deleteButton)
    }

    dialogElement.appendChild(buttonDiv)
    dialogElement.showModal()
}


function sendIntegration(instance) {
    post("integrations/" + instance["name"], instance)
    return true
}
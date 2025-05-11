import {InputController} from "../forms/input_controller.js"

export async function confirmDialog(title, message = "") {
    let confirmDialog = document.getElementById("confirmDialog")
    document.getElementById("confirmDialogTitle").textContent = title
    document.getElementById("confirmDialogText").textContent = message
    confirmDialog.showModal()
    return new Promise(resolve => {
        document.getElementById("confirmDialogOkButton").onclick = () => {
            confirmDialog.close()
            resolve(true)
        }
        document.getElementById("confirmDialogCancelButton").onclick = () => {
            confirmDialog.close()
            resolve(false)
        }
    })
}

export async function promptDialog(title, initialValue = "", schema = {}) {
    let promptDialog = document.getElementById("promptDialog")
    document.getElementById("promptDialogTitle").textContent = title

    let okButton = document.getElementById("promptDialogOkButton")

    let contentDiv = document.getElementById("promptDialogContent")
    contentDiv.textContent = ""

    let inputController = InputController.create(schema)
    if (initialValue != null) {
        inputController.setValue(initialValue)
    }

    inputController.inputElement.addEventListener("input", () => {
        okButton.disabled = !inputController.validate()
    })

    if (schema && schema.label) {
        let labelElement = document.createElement("label")
        labelElement.textContent = schema.label + ": "
        contentDiv.appendChild(labelElement)
    }

    contentDiv.appendChild(inputController.inputElement)
    contentDiv.appendChild(inputController.messageElement)

    promptDialog.showModal()
    return new Promise(resolve => {
        okButton.onclick = () => {
            promptDialog.close()
            resolve(inputController.getValue())
        }
        document.getElementById("promptDialogCancelButton").onclick = () => {
            promptDialog.close()
            resolve(null)
        }
    })
}


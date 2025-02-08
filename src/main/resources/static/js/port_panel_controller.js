import {functions} from "./shared_state.js";
import {InputController} from "./lib/form_builder.js";
import {sendJson} from "./lib/util.js";

let portPanelElement = document.getElementById("PortsPanel")
let portSelectElement = document.getElementById("portSelect")
let dialogElement = document.getElementById("dialog")

portSelectElement.addEventListener("change", addPort)


function addPort() {
    console.log("add port")

    let type = portSelectElement.value
    portSelectElement.selectedIndex = 0

    let operation = functions[type]
    dialogElement.textContent = "Add " + type


    let inputDiv = document.createElement("div")

    let nameLabelElement = document.createElement("label")
    nameLabelElement.textContent = "Name"
    inputDiv.appendChild(nameLabelElement)

    let nameInputElement = document.createElement("input")
    inputDiv.appendChild(nameInputElement)

    let configurationController = InputController.create(inputDiv, operation["params"])

    dialogElement.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = "ok"
    okButton.style.float = "right"
    okButton.addEventListener("click", () => {
        let values = configurationController.getValues()
        if (sendPort(nameInputElement.value.trim(), type, values)) {
            dialogElement.close()
        }
    })
    buttonDiv.appendChild(okButton)

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "cancel"
    cancelButton.style.float = "right"
    cancelButton.addEventListener("click", () => { dialogElement.close() })
    buttonDiv.appendChild(cancelButton)

    dialogElement.appendChild(buttonDiv)
    dialogElement.showModal()
}

function sendPort(name, type, definition) {
    sendJson("definePort/" + name, {type: type, configuration: definition})
    return true
}
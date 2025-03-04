import {functions, ports} from "./shared_state.js";
import {FormController} from "./forms/form_builder.js";
import {sendJson} from "./lib/util.js";

let portSelectElement = document.getElementById("portSelect")
let portListElement = document.getElementById("portList")
let dialogElement = document.getElementById("dialog")

portSelectElement.addEventListener("change", addPort)
portListElement.addEventListener("click", event => editPort(event))

function addPort() {
    console.log("add port")

    let type = portSelectElement.value
    portSelectElement.selectedIndex = 0

    let typeSpec = functions[type]

    showPortDialog(typeSpec)
}


function editPort(event) {
    let entryElement = event.target.parentNode
    let id = entryElement.id
    if (!id.startsWith("port.")) {
        console.log("Target element id not recognized: ", entryElement)
    }
    let name = id.substring("port.".length)
    let portSpec = ports[name]
    let constructorSpec = functions[portSpec.type]

    showPortDialog(constructorSpec, portSpec)
}

function showPortDialog(constructorSpec, portSpec) {

    let instanceSpec = portSpec != null ? portSpec["configuration"] : {}
    dialogElement.textContent = ""
    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = "IO-Port Specification"
    dialogElement.appendChild(dialogTitleElement)

    let inputDiv = document.createElement("div")
    inputDiv.className = "dialogFields"

    let portSchema = [{"name": "name"}]
    if (constructorSpec["kind"] == "OUTPUT_PORT") {
        portSchema.push({"name": "expression"})
    }
    let previousName = portSpec["name"]

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

    dialogElement.appendChild(inputDiv)

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
            dialogElement.close()
        }
    })
    buttonDiv.appendChild(okButton)

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "Cancel"
    cancelButton.className = "dialogButton"
    cancelButton.addEventListener("click", () => { dialogElement.close() })
    buttonDiv.appendChild(cancelButton)

    if (name != null) {
        let deleteButton = document.createElement("button")
        deleteButton.textContent = "Delete"
        deleteButton.className = "dialogButton"
        deleteButton.addEventListener("click", () => {
            sendJson("updatePort", {previousName: name})
            dialogElement.close()
        })
        buttonDiv.appendChild(deleteButton)
    }

    dialogElement.appendChild(buttonDiv)
    dialogElement.showModal()
}


function sendPort(definition) {
    sendJson("updatePort?name=" + definition["name"], definition)
    return true
}
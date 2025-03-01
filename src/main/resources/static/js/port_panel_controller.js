import {functions, ports} from "./shared_state.js";
import {FormController} from "./forms/form_builder.js";
import {sendJson} from "./lib/util.js";
import {extractParameters} from "./lib/expressions.js";

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
    let instanceSpec = portSpec.configuration
    let constructorSpec = null
    if (instanceSpec == null) {
        let description = portSpec.description
        let cut = description.indexOf(";")
        let constructorName = description.substring(0, cut).trim()
        instanceSpec = extractParameters(description.substring(cut + 1), constructorSpec["params"])
        constructorSpec = functions[constructorName]
    } else {
        constructorSpec = functions[portSpec.type]
    }

    showPortDialog(constructorSpec, name, instanceSpec)
}

function showPortDialog(constructorSpec, name, instanceSpec) {

    dialogElement.textContent = ""
    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = "IO-Port Specification"
    dialogElement.appendChild(dialogTitleElement)

    let inputDiv = document.createElement("div")
    inputDiv.className = "dialogFields"


    let nameLabelElement = document.createElement("label")
    nameLabelElement.textContent = "Name"
    inputDiv.appendChild(nameLabelElement)

    let nameInputElement = document.createElement("input")
    nameInputElement.value = name || ""
    inputDiv.appendChild(nameInputElement)

    let typeLabelElement = document.createElement("label")
    typeLabelElement.textContent = "Type"
    inputDiv.appendChild(typeLabelElement)

    let typeNameElement = document.createElement("div")
    typeNameElement.textContent = constructorSpec.name
    inputDiv.appendChild(typeNameElement)


    let configurationController = FormController.create(inputDiv, constructorSpec["params"])

    if (instanceSpec != null) {
        configurationController.setValues(instanceSpec)
    }

    dialogElement.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = "Ok"
    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        let values = configurationController.getValues()
        if (sendPort(nameInputElement.value.trim(), constructorSpec.name, values, name)) {
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


function sendPort(name, constructorName, definition, previousName) {
    sendJson("updatePort?name=" + name, {
        type: constructorName,
        previousName: previousName,
        configuration: definition
    })
    return true
}
import {functions, ports, portValues, showDependencies, simulationValues} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {InputController} from "./forms/input_controller.js";
import {camelCase, insertById, sendJson, updateSpec} from "./lib/util.js";

let inputPortSpecListElement = document.getElementById("inputPortSpecList")
let outputPortSpecListElement = document.getElementById("outputPortSpecList")

export function deletePortSpec(name) {
    let element = document.getElementById("portspec." + name)
    if (element) {
        element.parentElement.removeChild(element)
    }
}

export function processPortSpec(spec) {
    let container = spec.kind == "OUTPUT_PORT" ? outputPortSpecListElement : inputPortSpecListElement
    updateSpec(container, "portspec.", spec, () => {
        showPortDialog(spec)
    })
}

export function processPortValue(key, map) {
    let value = map[key]
    portValues[key] = value
    let target = document.getElementById("port." + key + ".value")
    if (target != null) {
        target.textContent = value
    }
}

export function processSimulationValue(key, map) {
    let value = map[key]
    simulationValues[key] = value
    let port = ports[key]
    if (port != null) {
        let controller = port.valueController
        if (controller != null) {
            controller.setValue(value)
        }
    }
}

export function processPortUpdate(name, f) {
    if (f.type == "TOMBSTONE") {
        let entryElement = document.getElementById("port." + name)
        if (entryElement != null) {
            entryElement.parentElement.removeChild(entryElement)
        }
    } else {
        let spec = functions[f.type]
        let entryElement = document.createElement("div")
        entryElement.id = "port." + f.name
        entryElement.className = "port"
        insertById(document.getElementById(spec.kind == "OUTPUT_PORT" ? "outputPortList" : "inputPortList"), entryElement)

        let entryConfigElement = document.createElement("img")
        entryConfigElement.src = "/img/settings.svg"
        entryConfigElement.className = "portConfig"
        entryConfigElement.onclick = () => {
            showPortDialog(spec, f)
        }

        let entryTitleElement = document.createElement("div")
        entryTitleElement.className = "portTitle"
        let nameElement = document.createElement("b")
        nameElement.textContent = name

        entryTitleElement.append(nameElement, ": " + f.type + "")

        entryElement.append(entryConfigElement, entryTitleElement)

        let modifiers = spec["modifiers"] || []
        // console.log("adding port", f, spec)

        let entryContentElement = document.createElement("div")
        entryContentElement.style.paddingLeft = "10px"
        entryContentElement.style.clear = "both"

        let showValue = true
        if (spec.kind == "INPUT_PORT") {
            let entryValueElement = document.createElement("span")
            entryValueElement.id = "port." + name + ".simulationValue"
            entryValueElement.className = "portSimulationValue"
            let controller = f.valueController = InputController.create({
                type: camelCase(spec.returnType),
                modifiers: ["CONSTANT"]})
            entryValueElement.appendChild(controller.inputElement)
            controller.addListener((value, source) => {
                sendJson("portSimulation?name=" + name, value)
            })
            showValue = !document.getElementById("simulationMode").checked
            entryValueElement.style.display = showValue ? "none" : "inline"
            entryContentElement.appendChild(entryValueElement)
        } else {
            let sourceElement = document.createElement("div")
            sourceElement.style.float = "right"
            sourceElement.style.paddingRight = "5px"
            sourceElement.textContent = "(" + f.expression + ")"

            entryContentElement.append(sourceElement)
        }

        let entryValueElement = document.createElement("span")
        entryValueElement.id = "port." + name + ".value"
        entryValueElement.className = "portValue"
        entryValueElement.style.display = showValue ? "inline" : "none"
        entryContentElement.appendChild(entryValueElement)

        entryElement.appendChild(entryContentElement)

        entryElement.onclick = () => {
            if (f.equivalent == null) {
                f.equivalent = []
            }
            if (f.equivalent.indexOf(f.name) == -1) {
                f.equivalent.push(name)
            }
            showDependencies(f)
        }
    }

    // console.log("received function spec", f)
    ports[name] = f
}


/*
function editPort(event) {
    if (event.target.className != "portConfig") {
        return;
    }
    let entryElement = event.target.parentNode
    let id = entryElement.id
    if (!id.startsWith("port.")) {
        // Clicked on input; not handled here.
        // console.log("Target element id not recognized: ", entryElement)
        return
    }
    let name = id.substring("port.".length)
    let portSpec = ports[name]

    showPortDialog(functions[portSpec.type].kind, portSpec)
}
*/
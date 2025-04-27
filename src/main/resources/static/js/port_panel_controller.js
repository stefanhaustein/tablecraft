import {functions, ports, portValues, simulationValues} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {InputController} from "./forms/input_controller.js";
import {camelCase, sendJson} from "./lib/util.js";

let portListConteiner = document.getElementById("portListContainer")

document.getElementById("addInputPort").addEventListener("click", () => showPortDialog("INPUT_PORT"))
document.getElementById("addOutputPort").addEventListener("click", () => showPortDialog("OUTPUT_PORT"))

portListContainer.addEventListener("click", event => editPort(event))


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
    let entryElement = document.getElementById("port." + name)
    if (f.type == "TOMBSTONE") {
        if (entryElement != null) {
            entryElement.parentElement.removeChild(entryElement)
        }
    } else {
        let spec = functions[f.type]

        if (entryElement == null) {
            entryElement = document.createElement("div")
            entryElement.id = "port." + f.name
            entryElement.className = "port"
            document.getElementById(
                spec.kind == "OUTPUT_PORT" ? "outputPortList" : "inputPortList"
            ).appendChild(entryElement)
        } else {
            entryElement.textContent = ""
        }
        let title = name + " (" + f.type + ")"

        let entryConfigElement = document.createElement("img")
        entryConfigElement.src = "/img/settings.svg"
        entryConfigElement.className = "portConfig"
        entryElement.appendChild(entryConfigElement)

        let entryTitleElement = document.createElement("div")
        entryTitleElement.className = "portTitle"
        entryTitleElement.textContent = title
        entryElement.appendChild(entryTitleElement)

        let modifiers = spec["modifiers"] || []
        // console.log("adding port", f, spec)

        let showValue = true
        if (spec.kind == "OUTPUT_PORT") {
            let entryExpressionElement = document.createElement("div")
            entryExpressionElement.className = "portExpression"
            entryExpressionElement.textContent = f["expression"]
            entryElement.appendChild(entryExpressionElement)
        } else {
            let entryValueElement = document.createElement("div")
            entryValueElement.id = "port." + name + ".simulationValue"
            entryValueElement.className = "portSimulationValue"
            let controller = f.valueController = new InputController({
                type: camelCase(spec.returnType),
                modifiers: ["CONSTANT"]})
            entryValueElement.appendChild(controller.element)
            controller.addListener((value, source) => {
                sendJson("portSimulation?name=" + name, value)
            })
            showValue = !document.getElementById("simulationMode").checked
            entryValueElement.style.display = showValue ? "none" : "block"
            entryElement.appendChild(entryValueElement)
        }

        let entryValueElement = document.createElement("div")
        entryValueElement.id = "port." + name + ".value"
        entryValueElement.className = "portValue"
        entryValueElement.style.display = showValue ? "block" : "none"
        entryElement.appendChild(entryValueElement)
    }

    // console.log("received function spec", f)
    ports[f.name] = f
}



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

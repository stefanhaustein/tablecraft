import {getSelectedCellRangeKey, portValues, showDependencies, simulationValues} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {InputController} from "./forms/input_controller.js";
import {camelCase, insertById, postJson, updateSpec} from "./lib/util.js";
import {getFactory, getPortInstance, registerPortInstance} from "./shared_model.js";


let inputPortSpecListElement = document.getElementById("inputPortSpecList")
let outputPortSpecListElement = document.getElementById("outputPortSpecList")


let rangeNameSelectElement = document.getElementById("rangeNameSelect")


export function processPortSpec(spec) {
    let container = spec.kind == "OUTPUT_PORT" ? outputPortSpecListElement : inputPortSpecListElement
    if (spec.name == "NamedCells") {
        document.getElementById("addNamedCellsButton").addEventListener("click", () => { showPortDialog(spec) })
        rangeNameSelectElement.addEventListener("change", async () => {
            rangeNameSelectElement.selectedIndex = 0
            let port = getPortInstance(rangeNameSelectElement.value)
            showPortDialog(spec, port)
        })
    } else {
        updateSpec(container, "portspec.", spec, () => {
            showPortDialog(spec)
        })
    }
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
    let port = getPortInstance(key)
    if (port != null) {
        let controller = port.valueController
        if (controller != null) {
            controller.setValue(value)
        }
    }
}

export function processPortUpdate(name, f) {
    if (!registerPortInstance(name, f)) {
        let entryElement = document.getElementById("port." + name)
        if (entryElement != null) {
            entryElement.parentElement.removeChild(entryElement)
        }
    } else {
        let spec = getFactory(f.type)
        let entryElement = document.createElement("div")
        entryElement.id = "port." + f.name
        entryElement.className = "port"
        insertById(document.getElementById(spec.kind == "OUTPUT_PORT" ? f.type == "NamedCells" ? "namedCellListContainer" :  "outputPortList" : "inputPortList"), entryElement)

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


        entryTitleElement.append(nameElement, ": " + (f.type == "NamedCells" ? f.source : f.type))

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
            controller.inputElement.addEventListener("change", () => {
                postJson("portSimulation?name=" + name, controller.getValue())
            })
            showValue = !document.getElementById("simulationMode").checked
            entryValueElement.style.display = showValue ? "none" : "inline"
            entryContentElement.appendChild(entryValueElement)
        } else if (f.type != "NamedCells") {
            let sourceElement = document.createElement("div")
            sourceElement.style.float = "right"
            sourceElement.style.paddingRight = "5px"
            sourceElement.textContent = "(" + f.source + ")"

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
            showDependencies(f.name)
        }
    }
}


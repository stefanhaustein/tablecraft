import {portValues, showDependencies, simulationValues} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {InputController} from "./forms/input_controller.js";
import {insertById} from "./lib/dom.js";
import {getFactory, getPortInstance, registerPortInstance} from "./shared_model.js";
import {camelCase, post, updateSpec} from "./lib/utils.js";
import {FormController} from "./forms/form_builder.js";


let inputPortSpecListElement = document.getElementById("inputPortSpecList")
let outputPortSpecListElement = document.getElementById("outputPortSpecList")


let rangeNameElement = document.getElementById("rangeName")


export function processPortSpec(spec) {
    let container = spec.kind == "OUTPUT_PORT" ? outputPortSpecListElement : inputPortSpecListElement
    if (spec.name == "NamedCells") {
        document.getElementById("addNamedCellsButton").addEventListener("click", () => { showPortDialog(spec) })
        rangeNameElement.addEventListener("click", async () => {
            let port = getPortInstance(rangeNameElement.textContent)
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
        target.textContent = JSON.stringify(value)
    }
}

export function processSimulationValue(key, map) {
    let value = map[key]
    simulationValues[key] = value
    let port = getPortInstance(key)
    if (port != null) {
        let controller = port.valueController
        if (controller instanceof InputController) {
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
        let spec = getFactory(f.kind)
        let isStruct = typeof f.type != "string"
        let entryElement = document.createElement(isStruct ? "details" : "div")
        entryElement.id = "port." + f.name
        entryElement.className = "port"
        insertById(document.getElementById(spec.kind == "OUTPUT_PORT" ? f.kind == "NamedCells" ? "namedCellListContainer" :  "outputPortList" : "inputPortList"), entryElement)

        let entryConfigElement = document.createElement("img")
        entryConfigElement.src = "/img/settings.svg"
        entryConfigElement.className = "portConfig"
        entryConfigElement.onclick = () => {
            showPortDialog(spec, f)
        }

        let entryTitleElement = document.createElement(isStruct ? "summary" : "div")
        entryTitleElement.className = "portTitle"
        let nameElement = document.createElement("b")
        nameElement.textContent = name

        entryTitleElement.append(nameElement, ": " + (f.kind == "NamedCells" ? f.source : f.kind))
        entryTitleElement.append(entryConfigElement)
        entryElement.append(entryTitleElement)

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
            if (!isStruct) {
                let controller = f.valueController = InputController.create(
                    {type: camelCase(f.type)})
                entryValueElement.appendChild(controller.inputElement)
                controller.inputElement.addEventListener("change", () => {
                    post("portSimulation?name=" + name, controller.getValue())
                })
            } else {
                console.log(spec)
                let controller = f.valueController = FormController.create(entryValueElement, f.type)
                controller.addListener(() => {
                    post("portSimulation?name=" + name, controller.getValue())
                })
            }
            showValue = !document.getElementById("simulationMode").checked
            entryValueElement.style.display = showValue ? "none" : "inline"
            entryContentElement.appendChild(entryValueElement)
        } else if (f.kind != "NamedCells") {
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

        entryElement.onclick = (event) => {
            if (event.target.localName.toUpperCase() == "DIV") {
               showDependencies(f.name)
            }
        }
    }
}


import {renderComputedValue} from "./cell_renderer.js";
import {
    currentSheet,
    functions,
    integrations,
    model,
    ports,
    portValues,
    selectSheet,
    simulationValues
} from "./shared_state.js";

import {addOption, camelCase, sendJson} from "./lib/util.js";
import {InputController} from "./forms/input_controller.js";
import {updateOperation} from "./operation_panel_controller.js";
import {processIntegrationUpdate, updateIntegrationSpec} from "./integration_panel_controller.js";

let sheetSelectElement = document.getElementById("sheetSelect")

var currentTag = -1
fetch()


function fetch() {
    var xmlhttp = new XMLHttpRequest();
    var url = "data?tag=" + currentTag;

    xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            proccessUpdateResponseText(this.responseText)
        }
    };
    xmlhttp.open("GET", url, true);
    xmlhttp.send();
    xmlhttp.onloadend = function () {
        setTimeout( fetch, 100)
    }
}

function proccessUpdateResponseText(responseText) {
    let lines = responseText.split("\n")
    let sectionMap = {}
    let sectionTitle = ""
    for (const line of lines) {
        let trimmed = line.trim()
        if (trimmed.length == 0) {
            // skip
        } else if (trimmed.startsWith("[")) {
            processSection(sectionTitle, sectionMap)
            sectionTitle = trimmed.substring(1, trimmed.length - 1)
            sectionMap = {}
        } else {
            let eq = trimmed.indexOf("=")
            let col = trimmed.indexOf(":")
            let cut = eq == -1 ? col : (col == -1 ? eq : Math.min(col, eq))
            if (cut != -1) {
                let key = trimmed.substring(0, cut).trim()
                let rawValue = trimmed.substring(cut + 1).trim()
                let value = rawValue == "" ? null : JSON.parse(rawValue)
                sectionMap[key] = value
            }
        }
    }
    processSection(sectionTitle, sectionMap)

    if (currentSheet == null) {
       selectSheet()
    }
}

function processSection(sectionName, map) {
    if (sectionName.startsWith("sheets."))  {
        if (sectionName.endsWith(".cells") && sectionName != "sheets.cells") {
            processSheetCellsUpdate(sectionName.substring("sheets.".length, sectionName.length - ".cells".length), map)
        } else {
            processSheetUpdate(sectionName.substring("sheets.".length), map)
        }
    } else switch (sectionName) {
        case "":
            currentTag = map["tag"]
            let simulationMode = map["simulationMode"]
            if (simulationMode != null) {
                document.getElementById("simulationMode").checked = simulationMode
            }
            break
        case "functions":
            processFunctionsUpdate(map)
            break
        case "ports":
            processPortsUpdate(map)
            break
        case "portValues":
            processPortValues(map)
            break
        case "simulationValues":
            processSimulationValues(map)
            break
        case "integrations":
            for (let name in map) {
                processIntegrationUpdate(name, map[name])
            }
            break
        default:
            console.log("Unrecognizes section: ", sectionName, map)
    }
}

function processPortValues(map) {
    for (let key in map) {
        let value = map[key]
        portValues[key] = value
        let target = document.getElementById("port." + key + ".value")
        if (target != null) {
            target.textContent = value
        }
    }
}

function processSimulationValues(map) {
    for (let key in map) {
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
}

function updateSheetSelectElement() {
    sheetSelectElement.textContent = ""
    for (let key in model.sheets) {
        let option = document.createElement("option")
        option.textContent = key
        sheetSelectElement.appendChild(option)
    }
    addOption(sheetSelectElement, "Edit Sheet Metadata")
    addOption(sheetSelectElement, "Add New Sheet")
}

function processSheetUpdate(name, map) {
    if (map["deleted"]) {
        let current =  model.sheets[name] == currentSheet
        delete model.sheets[name]
        updateSheetSelectElement()
        if (current) {
            selectSheet()
        }
    }
}

function processSheetCellsUpdate(name, map) {

    if (model.sheets[name] == null || sheetSelectElement.firstElementChild == null) {
        if (model.sheets[name] == null ) {
            model.sheets[name] = {
                name: name,
            cells: {}
            }
        }

        updateSheetSelectElement()
    }

    let cells = model.sheets[name].cells
    for (let key in map) {
        let newValue = map[key]
        if (key.endsWith(".c")) {
            key = key.substring(0, key.length - 2)
            let cell = cells[key]
            if (cell == null) {
                cell = cells[key] = {}
            }
            cell.c = newValue
        } else if (key.indexOf(".") == -1) {
            cells[key] = newValue
        } else {
            console.log("Unrecognized suffix for key ", key, "value", newValue)
        }
        let element = document.getElementById(key)
        if (element != null) {
            renderComputedValue(element, cells[key])
        } else {
            console.log("Sync issue: Element '" + key + "' not found for line '" + key + "=" + newValue + "'")
        }
    }
}

function processFunctionsUpdate(map) {
    for (let name in map) {
        processFunctionUpdate(name, map[name])
    }
}

function processFunctionUpdate(name, f) {
    let functionSelectElement = document.getElementById("functions")
    let optionElement = document.getElementById("op." + name)
    if (f.kind == "TOMBSTONE") {
        if (optionElement != null) {
            optionElement.parentElement.removeChild(optionElement)
        }
        let entryElement = document.getElementById("port." + name)
        if (entryElement != null) {
            entryElement.parentElement.removeChild(entryElement)
        }
    } else {
        let newAddition = optionElement == null
        if (newAddition) {
            optionElement = document.createElement("option")
            optionElement.id = "op." + name
            switch (f.kind) {
                case "INTEGRATION":
                    updateIntegrationSpec(f)
                    break
                case "FUNCTION":
                    functionSelectElement.appendChild(optionElement)
                    updateOperation(f)
                    break;
            }
        }

        if (f.kind == "FUNCTION") {
            optionElement.text = "=" + f.name + "("
        } else {
            optionElement.text = f.name
        }

    }

    // console.log("received function spec", f)
    functions[f.name] = f
}

function processPortsUpdate(map) {
    for (let name in map) {
        processPortUpdate(name, map[name])
    }
}

function processPortUpdate(name, f) {
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


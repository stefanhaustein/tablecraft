import {renderComputedValue} from "./cell_renderer.js";
import {
    currentSheet,
    model,
    portValues,
    selectSheet,
    simulationValues
} from "./shared_state.js";
import { registerFactory } from "./shared_model.js"


import {addOption} from "./lib/util.js";
import {processFunction} from "./operation_panel_controller.js";
import {processIntegrationUpdate, updateIntegrationSpec} from "./integration_panel_controller.js";
import {
    processPortSpec,
    processPortUpdate,
    processPortValue,
    processSimulationValue
} from "./port_panel_controller.js";

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
        case "factories":
            for (let name in map) {
                processFactoryUpdate(name, map[name])
            }
            break
        case "functions":
            for (let name in map) {
                processFunction(name, map[name])
            }
            break
        case "ports":
            for (let name in map) {
                processPortUpdate(name, map[name])
            }
            break
        case "portValues":
            for (let key in map) {
                processPortValue(key, map)
            }
            break
        case "simulationValues":
            for (let key in map) {
                processSimulationValue(key, map)
            }
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


function processFactoryUpdate(name, f) {
    registerFactory(name, f)

    switch (f.kind) {
        case "INTEGRATION":
            updateIntegrationSpec(f)
            break
        case "INPUT_PORT":
        case "OUTPUT_PORT":
            processPortSpec(f)
            break
        default:
            console.log("Unrecognized Factory: ", name, f)
    }
}


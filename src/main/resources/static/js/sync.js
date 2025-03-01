import {renderComputedValue} from "./cell_renderer.js";
import {currentSheet, ports, functions, currentCellId, currentCellData} from "./shared_state.js";
import {FormController} from "./forms/form_builder.js";
import {sendJson, camelCase} from "./lib/util.js";

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
}

function processSection(name, map) {
    if (name == "") {
        currentTag = map["tag"]
    } else if (name == "ports") {
        processPortsUpdate(map)
    } else if (name == "functions") {
        processFunctionsUpdate(map)
    } else if (name.startsWith("sheets")) {
        processSheetUpdate(map)
    } else {
        console.log("Unrecognizes section: ", name, map)
    }
}

function processSheetUpdate(map) {
    let cells = currentSheet.cells
    for (const rawKey in map) {
        let value = map[rawKey]
        if (rawKey.endsWith(".c")) {
            let key = rawKey.substring(0, rawKey.length - 2)
            let cell = cells[key]
            if (cell == null) {
                cell = cells[key] = {}
            }
            cell.c = value
            let element = document.getElementById(key)
            if (element != null) {
                renderComputedValue(element, cell)
            } else {
                console.log("Sync issue: Element '" + key + "' not found for line '" + key + "=" + value + "'")
            }
        } else if (rawKey.indexOf(".") == -1) {
            cells[rawKey] = value
        } else {
            console.log("Unrecognized suffix for key ", rawKey, "value", value)
        }
    }
}

function processFunctionsUpdate(map) {
    let functionSelectElement = document.getElementById("functions")
    let portSelectElement = document.getElementById("portSelect")
    let simulationListElement = document.getElementById("simulationList")
    for (let name in map) {
        let f = map[name]
        let optionElement = document.getElementById("op." + name)
        let simulationElement = document.getElementById("sim." + name)
        if (f.kind == "TOMBSTONE") {
            if (optionElement != null) {
                optionElement.parentElement.removeChild(optionElement)
            }
            if (simulationElement != null) {
                simulationListElement.removeChild(simulationElement)
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
                let target = f.kind == "PORT_CONSTRUCTOR" ? portSelectElement : functionSelectElement
                target.appendChild(optionElement)

                if (f.kind == "INPUT_PORT") {
                    simulationElement = document.createElement("div")
                    simulationElement.id = "sim." + name

                    let schema = {
                        type: camelCase(f.returnType),
                        name: name
                    }
                    let controller = FormController.create(simulationElement, [schema])
                    controller.addListener((name, value) => {
                        sendJson("portSimulation?name=" + name, value)
                    })

                    simulationListElement.appendChild(simulationElement)
                }

            }

            if (f.kind == "PORT_CONSTRUCTOR") {
                optionElement.text = f.name
            } else {
                optionElement.text = "=" + f.name + "("
            }

        }

        // console.log("received function spec", f)
        functions[f.name] = f
    }
}

function processPortsUpdate(map) {
    let portListElement = document.getElementById("portList")
    for (let name in map) {
        let f = map[name]
        let entryElement = document.getElementById("port." + name)
        if (f.kind == "TOMBSTONE") {
            if (entryElement != null) {
                entryElement.parentElement.removeChild(entryElement)
            }
        } else {
            if (entryElement == null) {
                entryElement = document.createElement("div")
                entryElement.id = "port." + f.name
                portListElement.appendChild(entryElement)
            } else {
                entryElement.textContent = ""
            }
            let title = name + ": " + f.type

            let entryTitleElement = document.createElement("div")
            entryTitleElement.className = "portTitle"
            entryTitleElement.textContent = title
            entryElement.appendChild(entryTitleElement)

            let entryBodyElement = document.createElement("div")
            entryBodyElement.className = "portDescription"
            entryBodyElement.textContent = Object.entries(f.configuration).map(([key, value]) => key + "=" + value).join(", ")
            entryElement.appendChild(entryBodyElement)
        }

        // console.log("received function spec", f)
        ports[f.name] = f
    }
}
import {renderComputedValue} from "./lib/util.js";
import {currentSheet, currentCellId, currentCellElement} from "./shared_state.js";

var currentTag = -1
fetch()


function fetch() {
    var xmlhttp = new XMLHttpRequest();
    var url = "sheet/" + currentSheet.name + "?tag=" + currentTag;

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
                let value = JSON.parse(trimmed.substring(cut + 1))
                sectionMap[key] = value
            }
        }
    }
    processSection(sectionTitle, sectionMap)
}

function processSection(name, map) {
    if (name == "") {
        currentTag = map["tag"]
    } else if (name.startsWith("sheets")) {
        processSheetUpdate(map)
    } else {
        console.log("Unrecognizes section: ", name, map)
    }
}

function processSheetUpdate(map) {
    let cells = currentSheet.cells
    for (const rawKey in map) {
        let cut = rawKey.indexOf(".")
        if (cut == -1) {
            console.log("unrecognized key", rawKey, value)
        } else {
            let value = map[rawKey]
            let key = rawKey.substring(0, cut).trim()
            let suffix = rawKey.substring(cut + 1).trim()
            let cell = cells[key]
            if (cell == null) {
                cell = cells[key] = {}
            }
            switch (suffix) {
                case "f":
                    cell.f = value
                    break
                case "c":
                    cell.c = value
                    if (key != currentCellId || !currentCellElement.classList.contains("editing")) {
                        let element = document.getElementById(key)
                        if (element != null) {
                            renderComputedValue(element, cell)
                        } else {
                            console.log("Sync issue: Element '" + key + "' not found for line '" + line + "'")
                        }
                    }
                    break
                default:
                    console.log("Unrecognized suffix: ", suffix, key, value)
            }
        }
    }
}
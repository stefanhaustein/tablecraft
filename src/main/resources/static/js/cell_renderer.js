import {currentSheet, setCurrentCellFormula} from "./shared_state.js";
import {getColumn, getRow, toCellId} from "./lib/util.js";

export function renderCell(key) {
    let targetElement = document.getElementById(key)
    if (targetElement == null) {
        console.log("Element for cell key '" + key + "' not found.'")
    }

    let cellData = currentSheet.cells[key]
    if (cellData == null) {
        cellData = {}
    }
    let imgSrc = cellData["i"]
    let value = cellData["c"]
    if (imgSrc) {
        if (imgSrc.endsWith("=")) {
            imgSrc += value
            value = ""
        }
        targetElement.style.backgroundImage = "url(" + imgSrc + ")"
        targetElement.style.backgroundSize = "cover"
    } else {
        targetElement.style.backgroundImage = null
        targetElement.style.backgroundSize = null
    }
    let validation = cellData["v"]
    if (validation != null && validation["values"] != null) {
        renderSelect(targetElement, cellData)
        return
    }

    let classes = targetElement.classList
    classes.remove("c", "e", "i", "r", "l")
    targetElement.removeAttribute("title")

    switch(typeof value) {
        case "bigint":
        case "number": classes.add("r"); break;
        case "boolean": classes.add("c"); break;
        case "object":
            if (value == null || value == "") {
                let col = getColumn(key)
                let row = getRow(key)
                let nextKey = toCellId(col + 1, row)
                let nextCell = currentSheet.cells[nextKey]
                if (nextCell != null && nextCell.f != null && nextCell.f.startsWith("=") && !imgSrc) {
                    targetElement.textContent = nextCell.f.substring(1).trim();
                    classes.add("i");
                } else {
                    targetElement.textContent = "";
                }
            } else {
                classes.add("l");
                switch (value["type"]) {
                    case "err":
                        let abbr = document.createElement("span")
                        targetElement.setAttribute("title", value["msg"])
                        abbr.textContent = "#REF"
                        targetElement.textContent = ""
                        targetElement.appendChild(abbr)
                        classes.add("e")
                        break;
                    default:
                        targetElement.textContent = JSON.stringify(value)
                }
            }
            return;
    }

    targetElement.textContent = value
}

function renderSelect(targetElement, cellData) {
    targetElement.textContent = ""
    let selectElement = document.createElement("select")
    selectElement.style.width = "100%"
    selectElement.style.height = "100%"
    let options = cellData["v"]["values"]
    let type = cellData["v"]["type"]
    let content = cellData["c"]
    let stringValue = content == null ? "" : content.toString()

    let found = false
    for (let option of options) {
        let optionElement = document.createElement("option")
        optionElement.textContent = option["label"]
        let optionStringValue = option["value"].toString()
        optionElement.value = optionStringValue
        if (optionStringValue == stringValue) {
            optionElement.selected = true
            found = true
        }
        selectElement.appendChild(optionElement)
    }
    if (!found) {
        let optionElement = document.createElement("option")
        optionElement.textContent = "(" + stringValue + ")"
        optionElement.value = stringValue
        optionElement.selected = true
        selectElement.style.backgroundColor = "#FDD"
        selectElement.appendChild(optionElement)
    }
    targetElement.appendChild(selectElement)
    selectElement.addEventListener("change", () => {
        setCurrentCellFormula(selectElement.value)
    })
}


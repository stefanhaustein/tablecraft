import {currentSheet, setCurrentCellFormula} from "./shared_state.js";

import {getColumn, getRow, toCellId} from "./lib/utils.js";

export function renderCell(key) {
    let targetElement = document.getElementById(key)
    if (targetElement == null) {
        console.log("Element for cell key '" + key + "' not found.'")
        return;
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
    if (validation != null && (validation["options"] != null || validation["type"] == "Boolean")) {
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
        case "string":
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
    let validation = cellData.v
    let selectElement = document.createElement("select")
    selectElement.style.width = "100%"
    selectElement.style.height = "100%"
    let type = validation.type
    let content = cellData["c"]

    if (type == "Boolean") {
        let optionElement = document.createElement("option")
        optionElement.textContent = validation["true"] || "True"
        selectElement.appendChild(optionElement)
        optionElement = document.createElement("option")
        optionElement.textContent = validation["false"] || "False"
        selectElement.appendChild(optionElement)

        selectElement.selectedIndex = content ? 0 : 1

        selectElement.addEventListener("change", () => {
            setCurrentCellFormula(selectElement.selectedIndex == 0)
        })
    } else {
        let stringValue = content == null ? "" : content.toString()
        let found = false
        for (let option of validation.options) {
            let optionElement = document.createElement("option")
            optionElement.textContent = option
            if (option == stringValue) {
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
        selectElement.addEventListener("change", () => {
            setCurrentCellFormula(selectElement.value)
        })
    }
    targetElement.appendChild(selectElement)

}


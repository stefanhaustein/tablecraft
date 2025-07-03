import {currentSheet, selectCell, setCurrentCellFormula} from "./shared_state.js";

import {getColumn, getRow, toCellId} from "./lib/utils.js";
import {InputController} from "./forms/input_controller.js";

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

    let classes = targetElement.classList
    classes.remove("c", "e", "i", "r", "l", "u")
    targetElement.removeAttribute("title")

    let validation = cellData["v"]
    if (validation?.type != null && validation?.type != "No User Input") {
        renderInput(targetElement, cellData)
        return
    }

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

function renderInput(targetElement, cellData) {
    targetElement.textContent = ""
    targetElement.classList.add("u")

    let inputController = InputController.create(cellData.v, document.getElementById("globalErrorDiv"))
    let inputElement = inputController.inputElement
    inputElement.style.width = "100%"
    inputElement.style.height = "100%"
    targetElement.appendChild(inputElement)
    inputController.setValue(cellData["c"])
    inputElement.addEventListener("change", () => {
        setCurrentCellFormula(inputElement.value, "renderer")
    })
    inputElement.addEventListener("click", () => {
        selectCell(targetElement.id)
    })

}


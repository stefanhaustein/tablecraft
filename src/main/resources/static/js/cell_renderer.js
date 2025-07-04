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
    if (value == null) {
        value = ""
    }
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
    classes.remove("c", "e", "i", "r", "l", "u", "I")
    targetElement.removeAttribute("title")

    let validation = cellData["v"]
    if (validation?.type != null && validation?.type != "No User Input") {
        renderInput(targetElement, cellData)
        return
    }

    let renderedValue = value
    switch(typeof value) {
        case "bigint":
        case "number":
            classes.add("r")
            break

        case "boolean":
            classes.add("c")
            renderedValue = value ? "True" : "False"
            break

        case "string":
            if (value == "" && !imgSrc) {
                let col = getColumn(key)
                let row = getRow(key)
                let nextKey = toCellId(col + 1, row)
                let nextCell = currentSheet.cells[nextKey]
                if (nextCell?.f != null && nextCell.f.startsWith("=")) {
                    renderedValue = nextCell.f.substring(1).trim()
                    classes.add("i")
                    // This is necessary because table cells don't respect (max-)height properly.
                    if (renderedValue.length > 8) {
                        targetElement.textContent = ""
                        let div = document.createElement("div")
                        targetElement.appendChild(div)
                        div.textContent = renderedValue
                        return
                    }
                    break
                }
            }
            classes.add("l")
            break

        default:
            switch (value["type"]) {
                case "err":
                    renderedValue = "#REF"
                    classes.add("e")
                    break
                default:
                    classes.add("l")
                    renderedValue = JSON.stringify(value)
            }
    }

    targetElement.textContent = renderedValue
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


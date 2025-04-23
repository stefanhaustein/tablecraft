import {
    currentCellElement,
    currentCellId,
    selectCell,
    setEditMode,
    EditMode,
    selectionRangeY,
    selectionRangeX,
} from "./shared_state.js"
import {getColumn, getRow, toCellId} from "./lib/util.js";

document.addEventListener("keydown", tableKeyPress)

let spreadsheetTBodyElement = document.getElementById("spreadsheetTBody")

spreadsheetTBodyElement.addEventListener(
    "click", (event) => selectCell(event.target.id || event.target.parentNode.id))
spreadsheetTBodyElement.addEventListener(
    "dblclick", (event) => {
        selectCell(event.target.id|| event.target.parentNode.id)
        setEditMode(EditMode.INPUT)
    })


function selectAndScrollCurrentIntoView(cellId) {
    selectCell(cellId)

}

function tableKeyPress(event) {

    if (event.target != document.body) {
        return
    }

    let matched = true
    switch (event.key) {
        case "ArrowDown":
            moveCursor(0, 1, event)
            break

        case "ArrowUp":
            moveCursor(0, -1, event)
            break

        case "ArrowLeft":
            moveCursor(-1, 0, event)
            break

        case "ArrowRight":
            moveCursor(1, 0, event)
            break

        case "Enter":
            setEditMode(EditMode.INPUT)
            break

        default:
            matched = false
    }
    if (matched) {
        event.preventDefault()
    }
}

function moveCursor(dx, dy, event) {
    let cellId = toCellId(getColumn(currentCellId) + dx, getRow(currentCellId) + dy)

    if (event.shiftKey) {
        selectCell(cellId, selectionRangeX - dx, selectionRangeY - dy)
    } else {
        selectCell(cellId)
    }

    currentCellElement.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })

}



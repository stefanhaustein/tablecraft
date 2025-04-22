import {
    currentCellElement,
    currentCellId,
    selectCell,
    setEditMode,
    EditMode,
    selectionRangeY,
    selectionRangeX, setSelectionRange
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
    currentCellElement.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })
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

function setRangeHighlight(setReset) {
    let x0 = getColumn(currentCellId)
    let y0 = getRow(currentCellId)
    let y = y0
    let dx = Math.sign(selectionRangeX)
    let dy = Math.sign(selectionRangeY)
    while(true) {
        let x = x0
        while (true) {
            if (x != x0 || y != y0) {
                let cellId = toCellId(x, y)
                let cellElement = document.getElementById(cellId)
                if (cellElement) {
                    if (setReset) {
                        cellElement.classList.add("focus2")
                    } else {
                        cellElement.classList.remove("focus2")
                    }
                }
            }

            if (x == x0 + selectionRangeX) {
                break
            }
            x += dx
        }
        if (y == y0 + selectionRangeY) {
            break
        }
        y += dy
    }
}


function moveCursor(dx, dy, event) {
    let column = getColumn(currentCellId)
    let row = getRow(currentCellId)

    setRangeHighlight(false)

    let shift = event.shiftKey
    if (!shift) {
        setSelectionRange(0, 0)
    }

    selectAndScrollCurrentIntoView(toCellId(column + dx, row + dy))

    if (shift) {
        setSelectionRange(selectionRangeX - dx, selectionRangeY - dy)
        setRangeHighlight(true)
    }
}



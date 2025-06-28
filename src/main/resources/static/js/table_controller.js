import {
    getCurrentCellElement,
    currentCell,
    selectCell,
    selectionRangeY,
    selectionRangeX,
} from "./shared_state.js"
import {getColumn, getRow, toCellId} from "./lib/dom.js";

let spreadsheetTBodyElement = document.getElementById("spreadsheetTBody")
let formulaInputElement = document.getElementById("formulaInput")



spreadsheetTBodyElement.addEventListener("keydown", tableKeyPress)


var dragOrigin = null
var dragRangeSelection = false

spreadsheetTBodyElement.addEventListener(
    "click", (event) => {
        if (dragRangeSelection) {
            dragRangeSelection = false
        } else {
            selectCell(event.target.id || event.target.parentNode.id)
        }
    })

spreadsheetTBodyElement.addEventListener(
    "dblclick", (event) => {
        selectCell(event.target.id|| event.target.parentNode.id)
        formulaInputElement.focus()
    })



spreadsheetTBodyElement.addEventListener("mousedown", (event) => {
    dragOrigin = event.target.id  || event.target.parentNode.id
    dragRangeSelection = false
});

spreadsheetTBodyElement.addEventListener("mouseup", (event) => {
    dragOrigin = null
    if (dragRangeSelection) {
        spreadsheetTBodyElement.style.userSelect = ""
    }
});


spreadsheetTBodyElement.addEventListener("mousemove", (event) => {
    // reset the transparency

    if (dragOrigin) {
        if (event.target.id == dragOrigin) {
            if (dragRangeSelection) {
                selectCell(dragOrigin)
            }
        } else {
            if (!dragRangeSelection) {
                dragRangeSelection = true
                spreadsheetTBodyElement.style.userSelect = "none"
            }
            let targetId = event.target.id || event.target.parentNode.id

            let targetColumn = getColumn(targetId)
            let targetRow = getRow(targetId)

            let originColum = getColumn(dragOrigin)
            let originRow = getRow(dragOrigin)

            console.log("dragOrigin", dragOrigin, [originColum, originRow], "target", targetId, [targetColumn, targetRow])

            selectCell(dragOrigin,  targetColumn - originColum, targetRow - originRow)
        }
    }

});


function selectAndScrollCurrentIntoView(cellId) {
    selectCell(cellId)

}

function tableKeyPress(event) {
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
            formulaInputElement.focus()
            break

        default:
            matched = false
    }
    if (matched) {
        event.preventDefault()
    }
}

function moveCursor(dx, dy, event) {
    let cellId = toCellId(getColumn(currentCell.key) + dx, getRow(currentCell.key) + dy)

    if (event.shiftKey) {
        selectCell(cellId, selectionRangeX - dx, selectionRangeY - dy)
    } else {
        selectCell(cellId)
    }

    getCurrentCellElement().scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })

}



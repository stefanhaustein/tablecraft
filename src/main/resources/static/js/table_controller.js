import {currentCellElement, currentCellId, selectCell} from "./model.js";


document.addEventListener("keydown", tableKeyPress)

let spreadsheetTBodyElement = document.getElementById("spreadsheetTBody")

spreadsheetTBodyElement.addEventListener(
    "click", (event) => selectCell(event.target.id, false))
spreadsheetTBodyElement.addEventListener(
    "dblclick", (event) => selectCell(event.target.id, true))


function selectAndScrollCurrentIntoView(cellId) {
    selectCell(cellId, false)
    currentCellElement.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })
}

function tableKeyPress(event) {

    if (event.target != document.body) {
        return
    }

    let letter = currentCellId.substring(0,1)
    let number = parseInt(currentCellId.substring(1))

    let matched = true
    switch (event.key) {
        case "ArrowDown":
            selectAndScrollCurrentIntoView(letter + (number + 1))
            break

        case "ArrowUp":
            selectAndScrollCurrentIntoView(letter + (number - 1))
            break

        case "ArrowLeft":
            selectAndScrollCurrentIntoView(String.fromCodePoint(letter.codePointAt(0) - 1) + number)
            break

        case "ArrowRight":
            selectAndScrollCurrentIntoView(String.fromCodePoint(letter.codePointAt(0) + 1) + number)
            break

        case "Enter":
            selectCell(currentCellId, true)
            break

        default:
            matched = false
    }
    if (matched) {
        event.preventDefault()
    }
}



import {currentCellElement, currentCellId, selectCell, setEditMode, EditMode} from "./shared_state.js"

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
            setEditMode(EditMode.INPUT)
            break

        default:
            matched = false
    }
    if (matched) {
        event.preventDefault()
    }
}



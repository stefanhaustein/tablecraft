import {currentCellElement, currentCellId} from "./model.js";



document.addEventListener("keydown", tableKeyPress)



function tableKeyPress(event) {
    if (document.activeElement == inputElement) {
        return
    }

    let letter = currentCellId.substring(0,1)
    let number = parseInt(currentCellId.substring(1))

    console.log(event)

    if (event.key == "ArrowDown") {
        event.preventDefault()
        selectCell(letter + (number + 1))
        currentCellElement.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })
    } else if (event.key == "ArrowUp" && number > 1) {
        event.preventDefault()
        selectCell(letter + (number - 1))
        currentCellElement.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })
    } else if (event.key == "Enter") {
        event.preventDefault()
        selectCell(currentCellId)
    }

}



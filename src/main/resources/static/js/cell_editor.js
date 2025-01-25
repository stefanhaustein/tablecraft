import {currentSheet} from "./model.js";

export let currentCellId = null
export let currentCellElement = null
let currentCellData = {}

document.getElementById("tbody").addEventListener(
    "click", (event) => selectCell(event.target.id))
//document.getElementById("tableViewport").addEventListener("keydown", tableKeyPress)
//document.getElementById("table").addEventListener("keydown", tableKeyPress)
document.addEventListener("keydown", tableKeyPress)

let inputElement = document.getElementById("current")

inputElement.addEventListener("change", sendInput)
inputElement.addEventListener("input", processInput)
inputElement.addEventListener("keydown", (event) => {
    if (event.key == "Enter") {
        event.preventDefault()
        event.stopPropagation()
        inputElement.blur()
        sendInput()
    } else if (event.key == "Escape") {
        inputElement.value = nullToEmtpy(currentCellSavedFormula)
        inputElement.blur()
        sendInput()
    } else {
        console.log(event.key)
        processInput()
    }
})
inputElement.addEventListener("focus", () => {
  currentCellElement.classList.add("editing")
  currentCellElement.innerText = nullToEmtpy(currentCellData["f"])
})
inputElement.addEventListener("blur", () => {
    currentCellElement.classList.remove("editing")
    currentCellElement.innerText = nullToEmtpy(currentCellData["c"])
})

selectCell("A1")

function sendInput() {
    processInput()
    console.log("sendInput")
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "update/" + currentSheet.name + "!" + currentCellId, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(currentSheet.cells[currentCellId]["f"]);
    console.log("xhr", xhr)
}

function processInput() {
    console.log("processInput")
    let value = inputElement.value
    currentCellData["f"] = value
    currentCellData["c"] = null
    currentCellElement.textContent = value
}


function selectCell(id) {
    let newElement = document.getElementById(id)
    if (!newElement) {
        return
    }
    let newData = currentSheet.cells[id]
    if (newData == null) {
        newData = currentSheet.cells[id] = {}
    }
    let newlySelected = id != currentCellId
    if (newlySelected) {
        if (currentCellId != null) {
            currentCellElement.classList.remove("focus")
            currentCellElement.classList.remove("editing")
            currentCellElement.innerText = nullToEmtpy(currentCellData["c"])
        }
    } else {
        inputElement.focus()
    }

    currentCellId = id
    currentCellElement = newElement
    currentCellData = newData
    let currentCellSavedFormula = currentCellData["f"]
    inputElement.value = nullToEmtpy(currentCellSavedFormula)

    if (newlySelected) {
        currentCellElement.classList.add("focus")
    }
}

function nullToEmtpy(s) {
    return s == null ? "" : s
}

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


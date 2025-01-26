import {currentSheet, currentCellId, currentCellData, currentCellElement, currentCellSavedFormula, selectCell} from "./model.js";
import {nullToEmtpy} from "./lib/util.js";

//document.getElementById("tableViewport").addEventListener("keydown", tableKeyPress)
//document.getElementById("table").addEventListener("keydown", tableKeyPress)

let formulaInputElement = document.getElementById("formulaInput")

formulaInputElement.addEventListener("change", sendInput)
formulaInputElement.addEventListener("input", processInput)
formulaInputElement.addEventListener("keydown", (event) => {
    if (event.key == "Enter") {
        event.preventDefault()
        event.stopPropagation()
        formulaInputElement.blur()
        sendInput()
        selectCell(currentCellId, false)
    } else if (event.key == "Escape") {
        formulaInputElement.value = nullToEmtpy(currentCellSavedFormula)
        formulaInputElement.blur()
        sendInput()
        selectCell(currentCellId, false)
    } else {
        console.log(event.key)
        processInput()
    }
})
formulaInputElement.addEventListener("focus", () => {
  selectCell(currentCellId, true)
})
/*
inputElement.addEventListener("blur", () => {
    currentCellElement.classList.remove("editing")
    currentCellElement.innerText = nullToEmtpy(currentCellData["c"])
})*/

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
    let value = formulaInputElement.value
    currentCellData["f"] = value
    currentCellData["c"] = null
    currentCellElement.textContent = value
}




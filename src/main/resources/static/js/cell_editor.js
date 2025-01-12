let currentCell = "A1"

document.getElementById("tbody").addEventListener("click", tableClick)

let inputElement = document.getElementById("current")

inputElement.addEventListener("change", sendInput)
inputElement.addEventListener("keyup", processInput)

selectCell(currentCell)

function sendInput() {
    processInput()
    console.log("sendInput")
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "update/" + currentSheet.name + "!" + currentCell, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(currentSheet.cells[currentCell][0]);
    console.log("xhr", xhr)
}

function processInput() {
    console.log("processInput")
    let data = currentSheet.cells[currentCell]
    if (data == null) {
        data = currentSheet.cells[currentCell] = []
    }
    let f = inputElement.value
    data[0] = f
    data[1] = null
    selectedElement().textContent = f
}

function selectedElement() {
    return document.getElementById(currentCell)
}

function selectCell(id) {
    if (id == currentCell) {
        inputElement.focus()
        processInput()
        return
    }

    selectedElement().style.background = ""
    currentCell = id
    selectedElement().style.background = "#ddf"

    let data = currentSheet.cells[id]
    if (data) {
        inputElement.value = data[0]
    } else {
        inputElement.value = ""
    }
}


function tableClick(event) {
    selectCell(event.target.id)
}

import {getColumn, getRow, makeEnum, postJson, toCellId} from "./lib/util.js";
import {nullToEmtpy} from "./lib/values.js";
import {renderComputedValue} from "./cell_renderer.js";
import {getPortInstance} from "./shared_model.js";

export var model = {
    sheets: {}
}

export var portValues = {}
export var simulationValues = {}
export var currentSheet = null
export let currentCellId = null
export let currentCellElement = null
export let currentCellData = {}

export let selectionRangeX = 0
export let selectionRangeY = 0

export let EditMode = makeEnum(["NONE", "INPUT", "PANEL"])

let currentEditMode = EditMode.NONE

let cellContentChangeListeners = {}  // new content, source
let cellSelectionListeners = [] // new id, edit mode

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

let originElement = document.getElementById("origin")


export function setSelectionRange(x, y) {
    selectionRangeX = x
    selectionRangeY = y
}

document.getElementById("simulationMode").addEventListener("change", (event) =>{
    let checked = event.target.checked
    for (let port of getAllPorts()) {
        let name = port.name
        let simulationValueElement = document.getElementById("port." + name + ".simulationValue")
        if (simulationValueElement != null) {
            let valueElement =  document.getElementById("port." + name + ".value")
            valueElement.style.display = checked ? "none" : "inline"
            simulationValueElement.style.display = checked ? "inline" : "none"
        }
    }

    postJson("/simulationMode", checked)
})

export function addCellSelectionListener(listener) {
    cellSelectionListeners.push(listener)
}

export function addCellContentChangeListener(name, listener) {
    cellContentChangeListeners[name] = listener
}

export function commitCurrentCell() {
    committedFormula = currentCellData.f
    postJson("update/" + currentSheet.name + "!" + currentCellId, currentCellData)
}


export function setCurrentCellFormula(value, source) {
    currentCellData["f"] = value
    currentCellData["c"] = null
    /*if (currentEditMode != EditMode.NONE) {
        currentCellElement.textContent = value
    }*/
    if (source != "input") {
        formulaInputElement.value = value
    }
    for (let key in cellContentChangeListeners) {
        if (key != source) {
            cellContentChangeListeners[key](value, source)
        }
    }
}

export function setEditMode(editMode) {
    if (editMode == null) {
        editMode = EditMode.NONE
    }
    currentEditMode = editMode
    if (editMode == EditMode.NONE) {
        currentCellElement.classList.remove("editing")
        currentCellElement.classList.add("focus")
     //   renderComputedValue(currentCellElement, currentCellData)
    } else {
        currentCellElement.classList.remove(/*"c", "e", "i", "r",*/ "focus")
        currentCellElement.classList.add("editing")
   //     currentCellElement.textContent = nullToEmtpy(currentCellData["f"])
        if (editMode == EditMode.INPUT) {
            formulaInputElement.focus()
        }
    }
    notifySelectionListeners()
}

function renderDependencies(
    key, propertyName, add, seen, depth, className0, className1, className2) {
    if (seen[key] != null && seen[key] < depth) {
        return
    }
    seen[key] = depth
    let cut = key.indexOf("!")
    let id = cut == -1 ? "port." + key : key.substring(cut + 1)

    let element = document.getElementById(id)
    if (element != null) {
        element.classList.remove(className0)
        element.classList.remove(className1)
        element.classList.remove(className2)
        if (add) {
            element.classList.add(depth == 0 ? className0 : (depth == 1 ? className1 : className2))
        }
    }

    let entity = cut == -1 ? getPortInstance(key) : currentSheet.cells[key.substring(cut + 1)]
    if (entity != null) {
        let depList = entity[propertyName]
        if (depList != null) {
            for (let childKey of depList) {
                renderDependencies(childKey, propertyName, add, seen, depth + 1, className0, className1, className2)
            }
        }
    }
}

export function selectSheet(name) {
    let cellId = currentCellId || "A1"

    if (name == null) {
        for (name in model.sheets) {
            break
        }
    }

    currentSheet = model.sheets[name]
    let cells = currentSheet.cells

    document.getElementById("sheetSelect").value = name

    for (let tdElement of document.getElementById("spreadsheetTBody").getElementsByTagName("td")) {
        renderComputedValue(tdElement, cells[tdElement.id])
    }

    selectCell(cellId)
}


export function setRangeHighlight(setReset) {
    if (currentCellId == null) {
        return
    }
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

let dependenciesShown = null

export function showDependencies(targetKey) {
    if (dependenciesShown != null) {
        //renderDependencies(dependenciesShown, "equivalent", false, {}, 0, "equivalent", "equivalent2")
        renderDependencies(dependenciesShown, "inputs", false, {},0, "self","input", "input2")
        renderDependencies(dependenciesShown, "dependencies", false,{},0, "self","dependency", "dependency2")
    }
    dependenciesShown = targetKey
    if (dependenciesShown != null) {
        //renderDependencies(dependenciesShown, "equivalent", true, {},0, "equivalent", "equivalent")
        renderDependencies(dependenciesShown, "inputs", true, {},0, "self","input", "input2")
        renderDependencies(dependenciesShown, "dependencies", true,  {},0, "self","dependency", "dependency2", true)
    }
}



export function selectCell(id, rangeX = 0, rangeY = 0) {

    setRangeHighlight(false)

    let newElement = document.getElementById(id)
    if (!newElement || newElement.localName != "td") {
        return
    }

    originElement.textContent = id

    let newData = currentSheet.cells[id]
    if (newData == null) {
        newData = currentSheet.cells[id] = {}
    }
    let newlySelected = id != currentCellId
    if (newlySelected) {
        if (currentCellId != null) {
            if (committedFormula != currentCellData["f"]) {
                commitCurrentCell()
            }

            currentCellElement.classList.remove("focus", "editing")
            renderComputedValue(currentCellElement, currentCellData)

        }
        committedFormula = newData["f"]
    }

    currentCellId = id
    currentCellElement = newElement
    currentCellData = newData
    formulaInputElement.value = nullToEmtpy(committedFormula)

    selectionRangeX = rangeX
    selectionRangeY = rangeY
    setRangeHighlight(true)

    if (newlySelected) {
        currentCellElement.classList.add("focus")
        showDependencies("!" + currentCellId)

        notifySelectionListeners()
    }
}

function notifySelectionListeners() {
    for (let listener of cellSelectionListeners) {
        listener(currentCellId)
    }
}


let panelSelectElement = document.getElementById("panelSelect")
let currentPanelName = ""
let currentPanelElement = null

selectPanel(panelSelectElement.value)

panelSelectElement.addEventListener("change", (ev) => {
    console.log("Select panel: " + name, ev)
    selectPanel(panelSelectElement.value)
})

export function selectPanel(name) {
    if (name == currentPanelName) {
        return
    }

    if (currentPanelElement != null) {
        currentPanelElement.style.display = "none"
    }
    currentPanelName = name
    panelSelectElement.value = name

    let sidePanelElement = document.getElementById("sidePanel")
    currentPanelElement = document.getElementById(name + "Panel")
    if (name == "Hide") {
        sidePanelElement.style.display = "none"
    } else {
        sidePanelElement.style.display = ""
        currentPanelElement.style.display = "block"
    }
}
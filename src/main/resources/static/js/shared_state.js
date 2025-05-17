import {getColumn, getRow, makeEnum, postJson, toCellId} from "./lib/util.js";
import {nullToEmtpy} from "./lib/values.js";
import {renderComputedValue} from "./cell_renderer.js";

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

function renderDependencies(ids, className, add) {
    if (ids == null) {
        return
    }
    for (let qualifiedId of ids) {
        let cut = qualifiedId.indexOf("!")
        let id = cut == -1 ? "port." + qualifiedId : qualifiedId.substring(cut + 1)

        let element = document.getElementById(id)

        if (element != null) {
            if (add) {
                element.classList.add(className)
            } else {
                element.classList.remove(className)
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

export function showDependencies(target) {
    if (dependenciesShown != null) {
        renderDependencies(dependenciesShown.equivalent, "equivalent", false)
        renderDependencies(dependenciesShown.inputs, "input", false)
        renderDependencies(dependenciesShown.dependencies, "dependency", false)
    }
    dependenciesShown = target
    if (dependenciesShown != null) {
        renderDependencies(dependenciesShown.equivalent, "equivalent", true)
        renderDependencies(dependenciesShown.inputs, "input", true)
        renderDependencies(dependenciesShown.dependencies, "dependency", true)
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
        showDependencies(currentCellData)

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
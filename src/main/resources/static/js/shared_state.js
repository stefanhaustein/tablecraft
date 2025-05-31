import {postJson} from "./lib/util.js";
import {nullToEmtpy} from "./lib/values.js";
import {renderComputedValue} from "./cell_renderer.js";
import {getAllPorts, model} from "./shared_model.js";
import {removeClasses, renderDependencies, renderRangeHighlight} from "./shared_state_internal_renderer.js";

export var portValues = {}
export var simulationValues = {}
export var currentSheet = null
export let currentCellElement = null
export let currentCellData = {}

export let selectionRangeX = 0
export let selectionRangeY = 0

let currentCellId = null

let cellContentChangeListeners = {}  // new content, source
let cellSelectionListeners = [] // new id, edit mode

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

let originElement = document.getElementById("origin")



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
    postJson("update/" + currentSheet.name + "!" + currentCellData.key, currentCellData)
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
    if (editMode == EditMode.NONE) {
     //   currentCellElement.classList.remove("editing")
     //   currentCellElement.classList.add("selected")
     //   renderComputedValue(currentCellElement, currentCellData)
        currentCellElement.focus()
    } else {
    //    currentCellElement.classList.remove(/*"c", "e", "i", "r",*/ "focus")
    //    currentCellElement.classList.add("editing")
   //     currentCellElement.textContent = nullToEmtpy(currentCellData["f"])
        if (editMode == EditMode.INPUT) {
            formulaInputElement.focus()
        } else {
            currentCellElement.focus()
        }
    }
    notifySelectionListeners()
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


let dependenciesShown = []

export function showDependencies(targetKey) {
    removeClasses(dependenciesShown, ["self","input", "input2", "dependency", "dependency2"])
    if (targetKey == null) {
        dependenciesShown = []
    } else {
        let seenIn = {}
        renderDependencies(targetKey, "inputs",  seenIn,0, ["self","input", "input2"])
        let seenOut = {}
        renderDependencies(targetKey, "dependencies", seenOut,0, ["self","dependency", "dependency2"])

        dependenciesShown = Object.getOwnPropertyNames(seenIn).concat(Object.getOwnPropertyNames(seenOut));
    }
}



export function selectCell(id, rangeX = 0, rangeY = 0) {

    renderRangeHighlight(currentCellData.key, selectionRangeX, selectionRangeY, false)

    let newElement = document.getElementById(id)
    if (!newElement || newElement.localName != "td") {
        return
    }

    originElement.textContent = id

    let newData = currentSheet.cells[id]
    if (newData == null) {
        newData = currentSheet.cells[id] = {key:id}
    }
    let newlySelected = id != currentCellId
    if (newlySelected) {
        if (currentCellId != null) {
            if (committedFormula != currentCellData["f"]) {
                commitCurrentCell()
            }

            currentCellElement.classList.remove("selected", "editing")
            renderComputedValue(currentCellElement, currentCellData)

        }
        committedFormula = newData["f"]
    }

    currentCellId = id
    currentCellElement = newElement
    currentCellData = newData
    formulaInputElement.value = nullToEmtpy(committedFormula)

    currentCellElement.focus()

    selectionRangeX = rangeX
    selectionRangeY = rangeY

    renderRangeHighlight(currentCellData.key, selectionRangeX, selectionRangeY, true)


    if (newlySelected) {
        currentCellElement.classList.add("selected")
        showDependencies(currentSheet.name + "!" + currentCellData.key)

        notifySelectionListeners()
    }
}

function notifySelectionListeners() {
    for (let listener of cellSelectionListeners) {
        listener(currentCellData.key)
    }
}


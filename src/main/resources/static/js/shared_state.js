import {makeEnum, sendJson} from "./lib/util.js";
import {nullToEmtpy} from "./lib/values.js";
import {renderComputedValue} from "./cell_renderer.js";

export var model = {
    sheets: {
        "Sheet1": {
            "name": "Sheet1",
            "cells": {
            }
        }
    }
}

export var functions = {}
export var ports = {}
export var portValues = {}
export var simulationValues = {}
export var currentSheet = model.sheets["Sheet1"]
export let currentCellId = null
export let currentCellElement = null
export let currentCellData = {}

export let EditMode = makeEnum(["NONE", "INPUT", "PANEL"])

let currentEditMode = EditMode.NONE

let cellContentChangeListeners = {}  // new content, source
let cellSelectionListeners = [] // new id, edit mode

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

selectCell("A1")

document.getElementById("simulationMode").addEventListener("change", (event) =>{
    let checked = event.target.checked
    for (let name in ports) {
        let simulationValueElement = document.getElementById("port." + name + ".simulationValue")
        if (simulationValueElement != null) {
            let valueElement =  document.getElementById("port." + name + ".value")
            valueElement.style.display = checked ? "none" : "block"
            simulationValueElement.style.display = checked ? "block" : "none"
        }
    }

    sendJson("/simulationMode", checked)
})

export function addCellSelectionListener(listener) {
    cellSelectionListeners.push(listener)
}

export function addCellContentChangeListener(name, listener) {
    cellContentChangeListeners[name] = listener
}

export function commitCurrentCell() {
    committedFormula = currentCellData.f
    sendJson("update/" + currentSheet.name + "!" + currentCellId, currentCellData)
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



export function selectCell(id) {
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

    if (newlySelected) {
        currentCellElement.classList.add("focus")
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

    currentPanelElement = document.getElementById(name + "Panel")
    currentPanelElement.style.display = "block"
}
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
export var integrations = {}
export var ports = {}
export var portValues = {}
export var simulationValues = {}
export var currentSheetName = "Sheet1"
export var currentSheet = model.sheets[currentSheetName]
export let currentCellId = null
export let currentCellElement = null
export let currentCellData = {}

export let EditMode = makeEnum(["NONE", "INPUT", "PANEL"])

let currentEditMode = EditMode.NONE

let cellContentChangeListeners = {}  // new content, source
let cellSelectionListeners = [] // new id, edit mode

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

let originElement = document.getElementById("origin")

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

function showDependencies(ids, className, add) {
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
    let cellId = currentCellId

    currentSheetName = name
    currentSheet = model.sheets[name]
    let cells = currentSheet.cells

    for (let tdElement of document.getElementById("spreadsheetTBody").getElementsByTagName("td")) {
        renderComputedValue(tdElement, cells[tdElement.id])
    }


    selectCell(cellId)
}

export function selectCell(id) {
    let newElement = document.getElementById(id)
    if (!newElement) {
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

            showDependencies(currentCellData.equivalent, "equivalent", false)
            showDependencies(currentCellData.inputs, "input", false)
            showDependencies(currentCellData.dependencies, "dependency", false)

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

        showDependencies(currentCellData.equivalent, "equivalent", true)
        showDependencies(currentCellData.inputs, "input", true)
        showDependencies(currentCellData.dependencies, "dependency", true)


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
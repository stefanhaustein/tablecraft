import {makeEnum, nullToEmtpy, renderComputedValue} from "./lib/util.js";

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
export var currentSheet = model.sheets["Sheet1"]
export let currentCellId = null
export let currentCellElement = null
export let currentCellData = {}

export let EditMode = makeEnum(["NONE", "INPUT", "PANEL"])

let currentEditMode = EditMode.NONE

let cellSelectionListeners = {} // new id, edit mode, source
let cellContentChangeListeners = {}  // new content, source

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

selectCell("A1")

export function commitCurrentCell() {
    let commitValue = currentCellData["f"]
    committedFormula = commitValue
    let xhr = new XMLHttpRequest();
    xhr.open("POST", "update/" + currentSheet.name + "!" + currentCellId, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(commitValue);
}


export function setCurrentCellFormula(value, source) {
    currentCellData["f"] = value
    currentCellData["c"] = null
    if (currentEditMode != EditMode.NONE) {
        currentCellElement.textContent = value
    }
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
        renderComputedValue(currentCellElement, currentCellData)
    } else {
        currentCellElement.classList.remove("c", "e", "i", "r", "focus")
        currentCellElement.classList.add("editing")
        currentCellElement.textContent = nullToEmtpy(currentCellData["f"])
        if (editMode == EditMode.INPUT) {
            formulaInputElement.focus()
        }
    }
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
    currentPanelElement.style.display = ""
}
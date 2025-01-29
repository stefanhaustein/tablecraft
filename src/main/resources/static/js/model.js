import {nullToEmtpy} from "./lib/util.js";

export var model = {
    sheets: {
        "Sheet1": {
            "name": "Sheet1",
            "cells": {
            }
        }
    }
}

export var currentSheet = model.sheets["Sheet1"]

export let currentCellId = null
export let currentCellElement = null
export let currentCellData = {}
export let currentCellSavedFormula = null
export var functions = {}

let formulaInputElement = document.getElementById("formulaInput")


let req = new XMLHttpRequest()
req.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
        let rawFunctions = JSON.parse(this.responseText)
        let datalist = document.getElementById("functions")
        for (let f of rawFunctions) {
            let optionElement = document.createElement("option")
            optionElement.text = "=" + f["name"] + "("
            datalist.appendChild(optionElement)
            functions[f["name"]] = transformFunction(f)
        }
    }};
req.open('GET', "functions", true);
req.send()


function transformFunction(f) {
    return f
}

export function setCurrentCellFormula(value) {
    formulaInputElement.value = value
}

export function renderComputedValue(targetElement, cellData) {
    let value = cellData["c"]
    if (value == null || value == "") {
        targetElement.textContent = ""
        targetElement.style.textAlign = ""
    } else if (value.startsWith("l:")) {
        targetElement.textContent = value.substring(2)
        targetElement.style.textAlign = ""
    } else if (value.startsWith("r:")) {
        targetElement.textContent = value.substring(2)
        targetElement.style.textAlign = "right"
    } else if (value.startsWith("c:")) {
        targetElement.textContent = value.substring(2)
        targetElement.style.textAlign = "center"
    } else {
        let abbr = document.createElement("span")
        abbr.setAttribute("title", value.startsWith("e:") ? value.substring(2) : value)
        abbr.textContent = "#REF"
        abbr.style.color = "red"
        targetElement.textContent = ""
        targetElement.appendChild(abbr)
        targetElement.style.textAlign = "center"
    }
}

export function selectCell(id, editMode) {
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
            renderComputedValue(currentCellElement, currentCellData)
        }
        currentCellSavedFormula = newData["f"]
    }

    currentCellId = id
    currentCellElement = newElement
    currentCellData = newData
    formulaInputElement.value = nullToEmtpy(currentCellSavedFormula)

    if (newlySelected) {
        currentCellElement.classList.add("focus")
    }

    if (editMode) {
        formulaInputElement.focus()
        currentCellElement.classList.remove("focus")
        currentCellElement.classList.add("editing")
        currentCellElement.textContent = nullToEmtpy(currentCellData["f"])
    } else {
        currentCellElement.classList.remove("editing")
        currentCellElement.classList.add("focus")
        renderComputedValue(currentCellElement, currentCellData)
    }
}


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

let inputElement = document.getElementById("current")


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
    inputElement.value = value
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
            currentCellElement.innerText = nullToEmtpy(currentCellData["c"])
        }
        currentCellSavedFormula = newData["f"]
    }

    currentCellId = id
    currentCellElement = newElement
    currentCellData = newData
    inputElement.value = nullToEmtpy(currentCellSavedFormula)

    if (newlySelected) {
        currentCellElement.classList.add("focus")
    }

    if (editMode) {
        inputElement.focus()
        currentCellElement.classList.remove("focus")
        currentCellElement.classList.add("editing")
        currentCellElement.innerText = nullToEmtpy(currentCellData["f"])
    } else {
        currentCellElement.classList.remove("editing")
        currentCellElement.classList.add("focus")
        currentCellElement.innerText = nullToEmtpy(currentCellData["c"])
    }
}


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

export var functions = {}


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

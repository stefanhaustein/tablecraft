import {showIntegrationDialog} from "../integration_editor.js";

export function makeEnum(arr){
    let obj = Object.create(null);
    for (let val of arr){
        obj[val] = Symbol(val);
    }
    return Object.freeze(obj);
}

export function addOption(selectElement, name) {
    let option = document.createElement("option")
    option.textContent = name
    selectElement.appendChild(option)
}

export function camelCase(s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()
}

export function insertById(parent, element) {
    let id = element.id
    let existing = document.getElementById(id)
    if (existing) {
        existing.replaceWith(element)
    } else {
        let child = parent.firstElementChild
        while (child != null && child.id < id) {
            child = child.nextElementSibling
        }
        if (child == null) {
            parent.appendChild(element)
        } else {
            child.insertAdjacentElement("beforebegin", element)
        }
    }
}

export function updateSpec(parent, idPrefix, spec, createAction) {

    let details = document.createElement("details")
    details.style.clear = "both"
    details.id = idPrefix + spec.name
    let summary = document.createElement("summary")
    summary.textContent = spec.name

    let separator = document.createElement("div")
    separator.style.clear = "both"

    details.append(summary, spec.description, separator)

    if (createAction) {
        let createButton = document.createElement("button")
        createButton.onclick = createAction
        createButton.style.float = "right"
        createButton.style.clear = "both"
        createButton.textContent = "Create"
        details.appendChild(createButton)
    }


    insertById(parent, details)

    return details
}


export function sendJson(path, data) {
    let xhr = new XMLHttpRequest();
    xhr.open("POST", path, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(JSON.stringify(data))
}

export function sendText(path, data) {
    let xhr = new XMLHttpRequest();
    xhr.open("POST", path, true);
    xhr.setRequestHeader('Content-Type', 'text/plain');
    xhr.send(data)
}

export function getColumn(cellId) {
    return cellId.codePointAt(0) - 64
}

export function getRow(cellId) {
    return parseInt(cellId.substring(1))
}

export function toCellId(column, row) {
    return String.fromCodePoint(column + 64) + row
}
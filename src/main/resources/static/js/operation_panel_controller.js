import {insertById, updateSpec} from "./lib/util.js";

let operationListContainerElement = document.getElementById("operationListContainer")

export function deleteOperation(name) {
    let detailsElement = document.getElementById("op.details." + name)
    if (detailsElement) {
        detailsElement.parentElement.removeChild(detailsElement)
    }
}

export function updateOperation(op) {
    updateSpec(operationListContainerElement, "op.details.", op)
}
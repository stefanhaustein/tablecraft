import {insertById} from "./lib/util.js";

let operationListContainerElement = document.getElementById("operationListContainer")

export function updateOperation(op) {

    let detailsElement = document.createElement("details")
    detailsElement.id = "op.details." + op.name

    let summaryElement = document.createElement("summary")
    summaryElement.textContent = op.name

    detailsElement.appendChild(summaryElement)

    detailsElement.append(op.description)

    insertById(operationListContainerElement, detailsElement)

}
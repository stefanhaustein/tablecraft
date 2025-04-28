import {insertById, updateSpec} from "./lib/util.js";

let operationListContainerElement = document.getElementById("operationListContainer")


export function updateOperation(op) {
    updateSpec(operationListContainerElement, "op.details.", op)
}
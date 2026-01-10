import {confirmDialog} from "./lib/dialogs.js"
import {updateSpec} from "./artifacts.js";
import {currentCell, setCurrentCellFormula} from "./shared_state.js";
import {registerFunction} from "./shared_model.js";


let operationListContainerElement = document.getElementById("operationListContainer")


export function processFunction(name, spec) {
    registerFunction(name, spec)
    updateSpec(operationListContainerElement, "op.details.", spec)
}
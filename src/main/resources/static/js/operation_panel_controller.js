import {updateSpec} from "./lib/dom.js";
import {confirmDialog} from "./lib/dialogs.js"
import {currentCell, setCurrentCellFormula} from "./shared_state.js";
import {registerFunction} from "./shared_model.js";

let operationListContainerElement = document.getElementById("operationListContainer")


export function processFunction(name, spec) {
    registerFunction(name, spec)
    updateSpec(operationListContainerElement, "op.details.", spec, async () => {
        let value = "=" + spec.name + "("
        if (currentCell.f == null || currentCell.f == "" || await confirmDialog("Overwrite Current Formula?", currentCell.key + ": '" + currentCell.f + "'")) {
            setCurrentCellFormula("=" + spec.name + "(")
        }
    })
}
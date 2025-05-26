import {updateSpec} from "./lib/util.js";
import {confirmDialog} from "./lib/dialogs.js"
import {currentCellData, setCurrentCellFormula} from "./shared_state.js";
import {registerFunction} from "./shared_model.js";

let operationListContainerElement = document.getElementById("operationListContainer")


export function processFunction(name, spec) {
    registerFunction(name, spec)
    updateSpec(operationListContainerElement, "op.details.", spec, async () => {
        let value = "=" + spec.name + "("
        if (currentCellData.f == null || currentCellData.f == "" || await confirmDialog("Overwrite Current Formula?", currentCellData.key + ": '" + currentCellData.f + "'")) {
            setCurrentCellFormula("=" + spec.name + "(")
        }
    })
}
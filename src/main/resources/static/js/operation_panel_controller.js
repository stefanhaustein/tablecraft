import {insertById, updateSpec} from "./lib/util.js";
import {confirmDialog} from "./lib/dialogs.js"
import {currentCellData, currentCellId, factories, functions, setCurrentCellFormula} from "./shared_state.js";

let operationListContainerElement = document.getElementById("operationListContainer")


export function processFunction(name, spec) {
    spec.name = name
    functions[name] = spec
    updateSpec(operationListContainerElement, "op.details.", spec, async () => {
        let value = "=" + spec.name + "("
        if (currentCellData.f == null || currentCellData.f == "" || await confirmDialog("Overwrite Current Formula?", currentCellId + ": '" + currentCellData.f + "'")) {
            setCurrentCellFormula("=" + spec.name + "(")
        }
    })
}
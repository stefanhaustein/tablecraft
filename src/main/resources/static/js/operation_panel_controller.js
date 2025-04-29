import {confirmDialog, insertById, updateSpec} from "./lib/util.js";
import {currentCellData, currentCellId, setCurrentCellFormula} from "./shared_state.js";

let operationListContainerElement = document.getElementById("operationListContainer")


export function updateOperation(op) {
    updateSpec(operationListContainerElement, "op.details.", op, async () => {
        let value = "=" + op.name + "("
        if (currentCellData.f == null || currentCellData.f == "" || await confirmDialog("Overwrite Current Formula?", currentCellId + ": '" + currentCellData.f + "'")) {
            setCurrentCellFormula("=" + op.name + "(")
        }
    })
}
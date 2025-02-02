import {nullToEmtpy} from "./lib/util.js";
import {EditMode, setCurrentCellFormula, setEditMode, commitCurrentCell} from "./shared_state.js";


let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

formulaInputElement.addEventListener("change", event => { setCurrentCellFormula(formulaInputElement.value, "input") } )
formulaInputElement.addEventListener("input", event => { setCurrentCellFormula(formulaInputElement.value, "input") } )
formulaInputElement.addEventListener("keydown", event => {
    if (event.key == "Enter") {
        event.preventDefault()
        event.stopPropagation()
        formulaInputElement.blur()
        setCurrentCellFormula(formulaInputElement.value)
        setEditMode(EditMode.NONE)
        commitCurrentCell()
    } else if (event.key == "Escape") {
        formulaInputElement.value = committedFormula
        setCurrentCellFormula(nullToEmtpy(committedFormula), "input")
        formulaInputElement.blur()
        setEditMode(EditMode.NONE)
    } else {
        console.log(event.key)
        setCurrentCellFormula(formulaInputElement.value)
    }
})
formulaInputElement.addEventListener("focus", () => {
    setEditMode(EditMode.INPUT)
})

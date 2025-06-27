import {setCurrentCellFormula, getCurrentCellElement, getSelectedCellRangeKey} from "./shared_state.js";
import {nullToEmtpy} from "./lib/values.js";
import {promptDialog} from "./lib/dialogs.js";
import {getAllPorts} from "./shared_model.js";
import {post} from "./lib/util.js";

// Sets up event handlers etc. for shared state. Depends on shared state

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

formulaInputElement.addEventListener("change", event => { setCurrentCellFormula(formulaInputElement.value, "input") } )
formulaInputElement.addEventListener("input", event => { setCurrentCellFormula(formulaInputElement.value, "input") } )
formulaInputElement.addEventListener("keydown", event => {
    if (event.key == "Enter") {
        event.preventDefault()
        event.stopPropagation()
        setCurrentCellFormula(formulaInputElement.value)
        getCurrentCellElement().focus()
    } else if (event.key == "Escape") {
        formulaInputElement.value = committedFormula
        setCurrentCellFormula(nullToEmtpy(committedFormula), "input")
        getCurrentCellElement().focus()
    } else {
        console.log(event.key)
        setCurrentCellFormula(formulaInputElement.value)
    }
})





document.getElementById("simulationMode").addEventListener("change", (event) =>{
    let checked = event.target.checked
    for (let port of getAllPorts()) {
        let name = port.name
        let simulationValueElement = document.getElementById("port." + name + ".simulationValue")
        if (simulationValueElement != null) {
            let valueElement =  document.getElementById("port." + name + ".value")
            valueElement.style.display = checked ? "none" : "inline"
            simulationValueElement.style.display = checked ? "inline" : "none"
        }
    }

    post("/simulationMode", checked)
})
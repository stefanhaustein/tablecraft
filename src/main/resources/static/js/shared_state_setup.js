import {setCurrentCellFormula, commitCurrentCell, currentCellElement} from "./shared_state.js";
import {nullToEmtpy} from "./lib/values.js";

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
        currentCellElement.focus()
        commitCurrentCell()
    } else if (event.key == "Escape") {
        formulaInputElement.value = committedFormula
        setCurrentCellFormula(nullToEmtpy(committedFormula), "input")
        currentCellElement.focus()
    } else {
        console.log(event.key)
        setCurrentCellFormula(formulaInputElement.value)
    }
})


let panelSelectElement = document.getElementById("panelSelect")
let currentPanelName = ""
let currentPanelElement = null

selectPanel(panelSelectElement.value)

panelSelectElement.addEventListener("change", (ev) => {
    console.log("Select panel: " + name, ev)
    selectPanel(panelSelectElement.value)
})

export function selectPanel(name) {
    if (name == currentPanelName) {
        return
    }

    if (currentPanelElement != null) {
        currentPanelElement.style.display = "none"
    }
    currentPanelName = name
    panelSelectElement.value = name

    let sidePanelElement = document.getElementById("sidePanel")
    currentPanelElement = document.getElementById(name + "Panel")
    if (name == "Hide") {
        sidePanelElement.style.display = "none"
    } else {
        sidePanelElement.style.display = ""
        currentPanelElement.style.display = "block"
    }
}
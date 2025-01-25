import {functions} from "./model.js";
import {addInputElements} from "./lib/form_builder.js";

let currentElement = document.getElementById("current")

currentElement.addEventListener("change", considerUpdatingFunctionTab)
currentElement.addEventListener("input", considerUpdatingFunctionTab)

let functionPanel = document.getElementById("functionPanel")
let currentFunction = null

function considerUpdatingFunctionTab() {
    let currentInput = currentElement.value
    console.log("currentInput:", currentInput)
    if (!currentInput.startsWith("=")) {
        return
    }
    let cut = currentInput.indexOf("(")
    let name = currentInput.substring(1, cut === -1 ? currentInput.length : cut)
    let found = functions[name]

    console.log("found:", found)

    if (found == null) {
        functionPanel.textContent = ""
        return;
    }

    if (currentFunction !== found) {
        functionPanel.style.display = ""
        functionPanel.textContent = ""
        addInputElements(functionPanel, found["params"], {})
        currentFunction = found
    }



}
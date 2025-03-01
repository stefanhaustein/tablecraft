import {
    functions,
    setCurrentCellFormula,
    selectPanel,
    addCellContentChangeListener,
    addCellSelectionListener, currentCellData, setEditMode, EditMode, currentCellElement, commitCurrentCell
} from "./shared_state.js";
import {FormController} from "./forms/form_builder.js";
import {extractParameters} from "./lib/expressions.js";

let functionPanelElement = document.getElementById("FunctionPanel")
let currentFunction = null
let currentController = null
let currentParameters = {}

addCellContentChangeListener("panel", (newValue, source) => {
    updateTabAndConsiderShowing()
})

addCellSelectionListener(() => {
    updateTabAndConsiderShowing()
})


functionPanelElement.addEventListener("focusin", event => {
    setEditMode(EditMode.PANEL)
})

function updateTabAndConsiderShowing() {
    let supported = updateParameterTab()
    if (supported) {
        if (currentCellData.f.startsWith("=image")) {
            selectPanel("Graphics")
        } else {
            selectPanel("Function")
        }
    }
}

function updateParameterTab() {
    let currentInput = currentCellData["f"]
    console.log("currentInput:", currentInput)
    let name = ""
    let found = null
    let cut = -1

    if (currentInput != null && currentInput.startsWith("=")) {
        cut = currentInput.indexOf("(")
        name = currentInput.substring(1, cut === -1 ? currentInput.length : cut)
        found = functions[name]
    }

    if (found == null) {
        functionPanelElement.textContent = "N/A"
        currentFunction = null
        return false;
    }

    if (currentFunction !== found) {
        functionPanelElement.textContent = ""

        let titleElement = document.createElement("div")
        titleElement.className = "subtitle"
        titleElement.textContent = found.name
        functionPanelElement.appendChild(titleElement)

        currentController = FormController.create(functionPanelElement, found["params"])
        currentFunction = found

        if (found.description != "") {
            let descriptionElement = document.createElement("p")
            descriptionElement.textContent = found.description
            functionPanelElement.appendChild(descriptionElement)
        }


        currentController.addListener((key, value) => {
            currentParameters[key] = value
            let s = "=" + currentFunction["name"] + "("
            for (let param of currentFunction.params) {
                let key = param["name"]
                let value = currentParameters[key]
                if (value != null) {
                    if (!s.endsWith("(")) {
                        s += ", "
                    }
                    s += key + "=" + value
                }
            }
            s += ")"
            setCurrentCellFormula(s)
            commitCurrentCell()
        })
    }

    currentParameters = cut === -1 ? {} : extractParameters(currentInput.substring(cut + 1), currentFunction.params)

    currentController.setValues(currentParameters)

    return true
}

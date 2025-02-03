import {
    functions,
    setCurrentCellFormula,
    selectPanel,
    addCellContentChangeListener,
    addCellSelectionListener, currentCellData, setEditMode, EditMode
} from "./shared_state.js";
import {InputController} from "./lib/form_builder.js";
import {tokenize} from "./lib/expression_tokenizer.js";

let parameterPanelElement = document.getElementById("ParametersPanel")
let currentFunction = null
let currentController = null
let currentParameters = {}

addCellContentChangeListener("panel", (newValue, source) => {
    updateTabAndConsiderShowing()
})

addCellSelectionListener(() => {
    updateTabAndConsiderShowing()
})


parameterPanelElement.addEventListener("focusin", event => {
    setEditMode(EditMode.PANEL)
})

function updateTabAndConsiderShowing() {
    if (updateParameterTab()) {
        selectPanel("Parameters")
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
        parameterPanelElement.textContent = "N/A"
        currentFunction = null
        return false;
    }

    if (currentFunction !== found) {
        parameterPanelElement.textContent = found["description"] || ""
        currentController = InputController.create(parameterPanelElement, found["params"])
        currentFunction = found

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
        })
    }

    currentParameters = cut === -1 ? {} : extractParameters(currentInput.substring(cut + 1), currentFunction.params)

    currentController.setValues(currentParameters)

    return true
}

function extractParameters(expr, expectedParams) {
    let result = {}
    let tokens = tokenize(expr) || []
    let expectedParameterIndex = 0

    console.log(currentFunction)
    let parameterName = ""
    let collecting = false
    let collected = ""
    let depth = 0

    for (let i = 0; i < tokens.length; i++) { // in loop uses strings
        let token = tokens[i]
        if (collecting) {
            if (depth == 0 && (token == "," || token == ")")) {
                collecting = false
                result[parameterName] = collected
            } else {
                collected += token
            }
        } else if (depth == 0) {
            if (/[a-zA-Z]+/.test(token) && tokens[i + 1] == "=") {
                parameterName = token
                i++
                collected = ""
            } else {
                let param = expectedParams[expectedParameterIndex]
                parameterName = param != null ? param.name : (""+expectedParameterIndex)
                collected = token
                collecting = true
                expectedParameterIndex++
            }
            collecting = true
        }

        switch (token) {
            case "(":
            case "{":
            case "[":
                depth++;
                break;
            case ")":
            case "}":
            case "]":
                depth--;
                break;
        }
    }

    if (collecting) {
        result[parameterName] = collected
    }

    return result
}

import {functions, setCurrentCellFormula} from "./model.js";
import {InputController} from "./lib/form_builder.js";
import {tokenize} from "./lib/expression_tokenizer.js";
import {selectPanel} from "./panel_controller.js";

let formulaInputElement = document.getElementById("formulaInput")

formulaInputElement.addEventListener("change", considerUpdatingFunctionTab)
formulaInputElement.addEventListener("input", considerUpdatingFunctionTab)

let parameterPanelElement = document.getElementById("ParametersPanel")
let currentFunction = null
let currentController = null
let currentParameters = {}

function considerUpdatingFunctionTab() {
    let currentInput = formulaInputElement.value
    console.log("currentInput:", currentInput)
    let name = ""
    let found = null
    let cut = -1

    if (currentInput.startsWith("=")) {
        cut = currentInput.indexOf("(")
        name = currentInput.substring(1, cut === -1 ? currentInput.length : cut)
        found = functions[name]
    }

    if (found == null) {
        parameterPanelElement.textContent = "(Parameter editor not available for the current cell content)"
        currentFunction = null
        return;
    }

    if (currentFunction !== found) {
        selectPanel("Parameters")
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
                s += ")"
                setCurrentCellFormula(s)
            }
        })
    }

    currentParameters = cut === -1 ? {} : extractParameters(currentInput.substring(cut + 1))

    currentController.setValues(currentParameters)
}

function extractParameters(expr) {
    let result = {}
    let tokens = tokenize(expr) || []

    console.log(tokens)
    let lastIdentifier = ""
    let collecting = false
    let collected = ""
    let depth = 0

    for (let token of tokens) {
        if (collecting) {
            if (depth == 0 && token == "," || token == ")") {
                collecting = false
                result[lastIdentifier] = collected
            }
            collected += token
        } else if (depth == 0) {
            if (/[a-zA-Z]+/.test(token)) {
                lastIdentifier = token
            } else if (token == "=") {
                collected = ""
                collecting = true
            }
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
        result[lastIdentifier] = collected
    }

    return result
}

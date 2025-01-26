import {functions, setCurrentCellFormula} from "./model.js";
import {InputController} from "./lib/form_builder.js";
import {tokenize} from "./lib/expression_tokenizer.js";

let currentElement = document.getElementById("current")

currentElement.addEventListener("change", considerUpdatingFunctionTab)
currentElement.addEventListener("input", considerUpdatingFunctionTab)

let functionPanel = document.getElementById("functionPanel")
let currentFunction = null
let currentController = null
let currentParameters = {}

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
        currentFunction = null
        return;
    }

    if (currentFunction !== found) {
        functionPanel.style.display = ""
        functionPanel.textContent = found["description"] || ""
        currentController = InputController.create(functionPanel, found["params"])
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

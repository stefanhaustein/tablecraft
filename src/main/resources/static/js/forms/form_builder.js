import {InputController} from "./input_controller.js";

export class FormController {
    constructor(elementControllers, listeners) {
        this.elementControllers = elementControllers
        this.listeners = listeners
    }

    addListener(listener) {
        this.listeners.push(listener)
    }

    setValues(map) {
        for (let key in map) {
            let elementController = this.elementControllers[key]
            if (elementController) {
                elementController.setValue(map[key])
            }
        }
    }

    getValues() {
        let result = {}
        for (let name in this.elementControllers) {
            result[name] = this.elementControllers[name].getValue()
        }
        return result
    }



    static create(rootElement, schema) {
        let result = {}
        let listeners = []
        for (const entry of schema) {
            let inputController = InputController.create(entry)
            let inputContainer = document.createElement("div")
            inputContainer.className = "inputContainer"

            inputContainer.append(inputController.inputElement, inputController.messageElement)
            rootElement.append(inputController.labelElement, inputContainer)

            result[entry.name] = inputController

            inputController.inputElement.addEventListener("change", () => {
                console.log("change event")
                for (let listener of listeners) {
                    listener(name, inputController.getValue())
                }
            })
        }
        return new FormController(result, listeners)
    }
}

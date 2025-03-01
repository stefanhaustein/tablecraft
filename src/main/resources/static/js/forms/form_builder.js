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
            let label = document.createElement("label")
            label.style.display = "block"
            let name = entry["name"]
            label.textContent = name
            rootElement.appendChild(label)

            let inputController = new InputController(entry)
            rootElement.appendChild(inputController.element)
            result[name] = inputController

            inputController.element.addEventListener("change", () => {
                console.log("change event")
                for (let listener of listeners) {
                    listener(name, inputController.getValue())
                }
            })
        }
        return new FormController(result, listeners)
    }
}

import { addInputElements } from "./lib/form_builder.js";


let createElement = document.getElementById("create")
createElement.addEventListener("change", createCellContent)


function createCellContent() {
    let type = createElement.value
    createElement.selectedIndex = 0


    let dialog = document.getElementById("dialog")
    dialog.innerText = schema[type].label

    let contentDiv = document.createElement("div")
    contentDiv.style.display = "grid"
    contentDiv.style.gridTemplateColumns = "max-content max-content"
    contentDiv.style.gap = "5px"
    contentDiv.style.alignItems = "center"
    contentDiv.style.paddingTop = "15px"
    contentDiv.style.paddingBottom = "15px"


    let result = {}
    addInputElements(contentDiv, schema[type].fields, result)
    dialog.appendChild(contentDiv)


    let buttonsDiv = document.createElement("div")
    buttonsDiv.style.display = "flex"
    buttonsDiv.style.justifyContent = "flex-end"
    buttonsDiv.style.gap = "5px"

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "Cancel"
    cancelButton.addEventListener("click", () => dialog.close() )
    buttonsDiv.appendChild(cancelButton)

    let okButton = document.createElement("button")
    okButton.innerText = "Ok"
    okButton.addEventListener("click", () => {
        dialog.close()
        let currentInputElement = document.getElementById("current")
        let value = "=" + type + "("
        for (key in result) {
            value += key + "=" + result[key]
        }
        value += ")"
        currentInputElement.value = value
        sendInput()
        // console.log("result: ", result)
    } )
    buttonsDiv.appendChild(okButton)

    dialog.appendChild(buttonsDiv)
    dialog.showModal()
}
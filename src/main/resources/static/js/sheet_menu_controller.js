import {currentSheetName, model} from "./shared_state.js";

let sheetDialogElement = document.getElementById("sheetDialog")
let sheetSelectElement = document.getElementById("sheetSelect")
let sheetDialogTitleElement = document.getElementById("sheetDialogTitle")
let sheetDialogOkButtonElement = document.getElementById("sheetOkButton")
let sheetNameInputElement = document.getElementById("sheetNameInput")

document.getElementById("sheetCancelButton").addEventListener("click", () => sheetDialogElement.close())

sheetSelectElement.addEventListener("change", () => {
    sheetDialogTitleElement.textContent = sheetSelectElement.value
    if (sheetSelectElement.value == "Edit Sheet Metadata") {
        sheetSelectElement.value = currentSheetName
        sheetDialogOkButtonElement.textContent = "Ok"
        editSheetMetadata(currentSheetName)
    } else if (sheetSelectElement.value == "Add New Sheet") {
        sheetSelectElement.value = currentSheetName
        sheetDialogOkButtonElement.textContent = "Add"
        editSheetMetadata()
    }
})


function editSheetMetadata(sheetName) {
    let previousName = sheetName
    if (sheetName == null) {
        let n = 0
        for (let key in model.sheets) {
            n++
        }
        do {
            n++
            sheetName = "Sheet" + n
        } while (model.sheets[sheetName] != null)
    }
    sheetNameInputElement.value = sheetName

    sheetDialogElement.showModal()

    sheetDialogOkButtonElement.onclick = () => {
        let body = {
            previousName: previousName,
            name: sheetNameInputElement.value }
        fetch(new Request("updateSheet", {method: "POST", body: JSON.stringify(body)}))
        sheetDialogElement.close()
    }

}



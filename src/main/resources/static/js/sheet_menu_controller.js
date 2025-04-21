import {model, currentSheet, selectSheet} from "./shared_state.js";

let sheetDialogElement = document.getElementById("sheetDialog")
let sheetSelectElement = document.getElementById("sheetSelect")
let sheetDialogTitleElement = document.getElementById("sheetDialogTitle")
let sheetDialogOkButtonElement = document.getElementById("sheetOkButton")
let sheetNameInputElement = document.getElementById("sheetNameInput")

document.getElementById("sheetCancelButton").addEventListener("click", () => sheetDialogElement.close())

sheetSelectElement.addEventListener("change", () => {
    let selectedValue = sheetSelectElement.value
    sheetDialogTitleElement.textContent = selectedValue
    if (selectedValue == "Edit Sheet Metadata") {
        sheetSelectElement.value = currentSheet.name
        sheetDialogOkButtonElement.textContent = "Ok"
        editSheetMetadata(currentSheet.name)
    } else if (selectedValue == "Add New Sheet") {
        sheetSelectElement.value = currentSheet.name
        sheetDialogOkButtonElement.textContent = "Add"
        editSheetMetadata()
    } else {
        selectSheet(selectedValue)
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
        fetch(new Request("sheet", {method: "POST", body: JSON.stringify(body)}))
        sheetDialogElement.close()
    }

}



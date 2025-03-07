import {commitCurrentCell, setCurrentCellFormula} from "./shared_state.js";

export function renderComputedValue(targetElement, cellData) {
    let validation = cellData["v"]
    if (validation != null && validation["values"] != null) {
        renderSelect(targetElement, cellData)
        return
    }
    let value = cellData["c"]

    let classes = targetElement.classList
    classes.remove("c", "e", "i", "r", "l")
    targetElement.removeAttribute("title")

    switch(typeof value) {
        case "bigint":
        case "number": classes.add("r"); break;
        case "boolean": classes.add("c"); break;
        case "object":
            if (value == null) {
                targetElement.textContent = ""
            } else {
                classes.add("l");
                switch (value["type"]) {
                    case "img":
                        targetElement.textContent = ""
                        let img = document.createElement("img")
                        img.src = value["src"]
                        targetElement.appendChild(img)
                        classes.add("i")
                        break
                    case "err":
                        let abbr = document.createElement("span")
                        targetElement.setAttribute("title", value["msg"])
                        abbr.textContent = "#REF"
                        targetElement.textContent = ""
                        targetElement.appendChild(abbr)
                        classes.add("e")
                        break;
                    default:
                        targetElement.textContent = JSON.stringify(value)
                }
            }
            return;
    }

    targetElement.textContent = value


    /*    } else if (value.startsWith("i:")) {
        targetElement.textContent = ""
        let img = document.createElement("img")
        img.src = value.substring(2)
        targetElement.appendChild(img)
        classes.add("i")
    } else {
        let abbr = document.createElement("span")
        targetElement.setAttribute("title", value.startsWith("e:") ? value.substring(2) : value)
        abbr.textContent = "#REF"
        targetElement.textContent = ""
        targetElement.appendChild(abbr)
        classes.add("e")
    }*/
}

function renderSelect(targetElement, cellData) {
    targetElement.textContent = ""
    let selectElement = document.createElement("select")
    selectElement.style.width = "100%"
    selectElement.style.height = "100%"
    let options = cellData["v"]["values"]
    let type = cellData["v"]["type"]
    let stringValue = cellData["c"].toString()
    let found = false
    for (let option of options) {
        let optionElement = document.createElement("option")
        optionElement.textContent = option["label"]
        let optionStringValue = option["value"].toString()
        optionElement.value = optionStringValue
        if (optionStringValue == stringValue) {
            optionElement.selected = true
            found = true
        }
        selectElement.appendChild(optionElement)
    }
    if (!found) {
        let optionElement = document.createElement("option")
        optionElement.textContent = "(" + stringValue + ")"
        optionElement.value = stringValue
        optionElement.selected = true
        selectElement.style.backgroundColor = "#FDD"
        selectElement.appendChild(optionElement)
    }
    targetElement.appendChild(selectElement)
    selectElement.addEventListener("change", () => {
        setCurrentCellFormula(selectElement.value)
        commitCurrentCell()
    })
}


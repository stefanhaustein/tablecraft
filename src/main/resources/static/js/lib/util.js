export function nullToEmtpy(s) {
    return s == null ? "" : s
}


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

    if (value == null || value == "") {
        targetElement.textContent = ""
    } else if (value.startsWith("l:")) {
        targetElement.textContent = value.substring(2)
        if (value.length > 2) {
            classes.add("l")
        }
    } else if (value.startsWith("r:")) {
        targetElement.textContent = value.substring(2)
        classes.add("r")
    } else if (value.startsWith("c:")) {
        targetElement.textContent = value.substring(2)
        classes.add("c")
    } else if (value.startsWith("i:")) {
        targetElement.textContent = ""
        let img = document.createElement("img")
        img.src = value.substring(2)
        targetElement.appendChild(img)
        classes.add("i")
    } else {
        let abbr = document.createElement("span")
        /*abbr.*/ targetElement.setAttribute("title", value.startsWith("e:") ? value.substring(2) : value)
        abbr.textContent = "#REF"
        targetElement.textContent = ""
        targetElement.appendChild(abbr)
        classes.add("e")
    }
}

function renderSelect(targetElement, cellData) {
    targetElement.textContent = ""
    let selectElement = document.createElement("select")
    selectElement.style.width = "100%"
    selectElement.style.height = "100%"
    let options = cellData["v"]["values"]
    let value = cellData["c"]
    let found = false
    for (let option of options) {
        let optionElement = document.createElement("option")
        optionElement.textContent = option["label"]
        let optionValue = option["value"]
        optionElement.value = option["value"]
        if (optionValue == value) {
            optionElement.setAttribute("selected")
            found = true
        }
        selectElement.appendChild(optionElement)
    }
    if (!found) {
        let optionElement = document.createElement("option")
        optionElement.textContent = "REF: " + value
        selectElement.appendChild(optionElement)
    }
    targetElement.appendChild(selectElement)
}


export function makeEnum(arr){
    let obj = Object.create(null);
    for (let val of arr){
        obj[val] = Symbol(val);
    }
    return Object.freeze(obj);
}

export function sendJson(path, data) {
    let xhr = new XMLHttpRequest();
    xhr.open("POST", path, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(JSON.stringify(data))
}

export function sendText(path, data) {
    let xhr = new XMLHttpRequest();
    xhr.open("POST", path, true);
    xhr.setRequestHeader('Content-Type', 'text/plain');
    xhr.send(data)
}
import {functions} from "./shared_state.js";

// Handles all dynamic / async setup, depends on core.

// Generate the spreadsheet content


let thead = document.getElementById("spreadsheetTHead")
for (let col = 0; col < 27; col++) {
    let th = document.createElement("th")
    th.style.top = 0
    th.style.position = "sticky"
    if (col == 0) {
        th.style.left = 0
        th.style.zIndex = 1
    } else {
        th.textContent = String.fromCharCode(col + 64)
    }
    thead.appendChild(th)
}
let tbody = document.getElementById("spreadsheetTBody")
for (let row = 1; row < 100; row++) {
    let tr = document.createElement("tr")
    tbody.appendChild(tr)
    let th = document.createElement("th")
    th.style.left = "0"
    th.style.position = "sticky"
    th.textContent = row
    tr.appendChild(th)
    for(let col = 1; col < 27; col++) {
        let td = document.createElement("td")
        let id = String.fromCharCode(col + 64) + row
        td.id = id
        tr.appendChild(td)
    }
}

// Request the function specifications available

let req = new XMLHttpRequest()
req.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
        let rawFunctions = JSON.parse(this.responseText)
        let datalist = document.getElementById("functions")
        for (let f of rawFunctions) {
            let optionElement = document.createElement("option")
            optionElement.text = "=" + f["name"] + "("
            datalist.appendChild(optionElement)
            functions[f["name"]] = transformFunctionSpec(f)
        }
    }};
req.open('GET', "functions", true);
req.send()


function transformFunctionSpec(f) {
    return f
}
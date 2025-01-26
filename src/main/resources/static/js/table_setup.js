import {currentSheet} from "./model.js";

let thead = document.getElementById("spreadsheetTHead")
for (let col = 0; col < 27; col++) {
    let th = document.createElement("th")
    th.style.top = 0
    th.style.position = "sticky"
    if (col == 0) {
        th.style.left = 0
        th.style.zIndex = 1
        th.style.width = "33px"
        th.style.minWidth = "33px"
    } else {
        th.style.width = "100px"
        th.style.minWidth = "100px"
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
        let data = currentSheet.cells[id]
        if (data) {
            td.textContent = data[1] || data[0]
        }
        tr.appendChild(td)
    }

}
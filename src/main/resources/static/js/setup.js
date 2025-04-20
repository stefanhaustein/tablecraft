// Generate the spreadsheet content
// Needs to be loaded first, so shared_state can select the first cell

let thead = document.getElementById("spreadsheetTHead")
for (let col = 0; col < 27; col++) {
    let th = document.createElement("th")
    th.style.top = 0
    if (col == 0) {
        th.style.left = 0
        th.style.zIndex = 3
        th.id = "origin"
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
    th.textContent = row
    tr.appendChild(th)
    for(let col = 1; col < 27; col++) {
        let td = document.createElement("td")
        let id = String.fromCharCode(col + 64) + row
        td.id = id
        tr.appendChild(td)
    }
}

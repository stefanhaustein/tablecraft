import {currentSheet, currentCellId, currentCellElement} from "./model.js";


var currentTag = -1
fetch()


function fetch() {
    var xmlhttp = new XMLHttpRequest();
    var url = "sheet/" + currentSheet.name + "?tag=" + currentTag;

    xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            updateCurrentSheet(this.responseText)
        }
    };
    xmlhttp.open("GET", url, true);
    xmlhttp.send();
    xmlhttp.onloadend = function () {
        setTimeout( fetch, 100)
    }

}

function updateCurrentSheet(responseText) {
    let lines = responseText.split("\n")
    let cells = currentSheet.cells
    for (const line of lines) {
        let cut = line.indexOf("=")
        let rawKey = line.substring(0, cut).trim()
        let value = line.substring(cut + 1).trim()
        if (value.startsWith('"')) {
            value = JSON.parse(value)
        }
        cut = rawKey.indexOf(".")
        if (cut == -1) {
            if (rawKey == "tag") {
                currentTag = value
            } else if (rawKey != "") {
                console.log("unrecognized key", rawKey, value)
            }
        } else {
            let key = rawKey.substring(0, cut).trim()
            let suffix = rawKey.substring(cut + 1).trim()
            let cell = cells[key]
            if (cell == null) {
                cell = cells[key] = {}
            }
            switch (suffix) {
                case "f":
                    cell.f = value
                    break
                case "c":
                    cell.c = value
                    if (key != currentCellId || !currentCellElement.classList.contains("editing")) {
                        document.getElementById(key).innerText = value
                    }
                    break
                default:
                    console.log("Unrecognized suffix: ", suffix, key, value)
            }
        }
    }
}
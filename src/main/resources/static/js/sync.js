fetch(false)


function fetch(computed) {
    var xmlhttp = new XMLHttpRequest();
    var url = "sheet/" + currentSheet.name + (computed ?  "/computed" : "/formulas");

    xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            // console.log(this.responseText)
            let json = JSON.parse(this.responseText);
            updateCurrentSheet(computed, json)
        }
    };
    xmlhttp.open("GET", url, true);
    xmlhttp.send();
    // xmlhttp.onloadend = function () { setTimeout(fetch, 10000) }

}

function updateCurrentSheet(computed, json) {
    let cells = currentSheet.cells
    for (const key in json) {
        let cell = cells[key]
        if (cell == null) {
            cell = cells[key] = []
        }
        document.getElementById(key).innerText = cell[computed? 1 : 0] = json[key]
    }
    if (!computed) {
        fetch(true)
    }

}
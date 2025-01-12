fetch()

function fetch() {
    var xmlhttp = new XMLHttpRequest();
    var url = "sheet/" + currentSheet.name + "/computed";

    xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            // console.log(this.responseText)
            let json = JSON.parse(this.responseText);
            updateCurrentSheet(json)
        }
    };
    xmlhttp.open("GET", url, true);
    xmlhttp.send();
    // xmlhttp.onloadend = function () { setTimeout(fetch, 10000) }

}

function updateCurrentSheet(json) {
    let cells = currentSheet.cells
    for (const key in json) {
        let cell = cells[key]
        if (cell == null) {
            cell = cells[key] = []
        }
        document.getElementById(key).innerText = cell[1] = json[key]
    }


}
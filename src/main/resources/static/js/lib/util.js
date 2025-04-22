export function makeEnum(arr){
    let obj = Object.create(null);
    for (let val of arr){
        obj[val] = Symbol(val);
    }
    return Object.freeze(obj);
}

export function addOption(selectElement, name) {
    let option = document.createElement("option")
    option.textContent = name
    selectElement.appendChild(option)
}

export function camelCase(s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()
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

export function getColumn(cellId) {
    return cellId.codePointAt(0) - 64
}

export function getRow(cellId) {
    return parseInt(cellId.substring(1))
}

export function toCellId(column, row) {
    return String.fromCodePoint(column + 64) + row
}
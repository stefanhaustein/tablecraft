import {alertDialog} from "./dialogs.js";

export function camelCase(s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()
}

export function getColumn(cellId) {
    return cellId.codePointAt(0) - 64
}

export function getRow(cellId) {
    return parseInt(cellId.substring(1))
}


export function iterateKeys(cellRange, callback) {
    let cut = cellRange.indexOf(":")
    if (cut == -1) {
        callback(cellRange)
        return
    }
    let key0 = cellRange.substring(0, cut).trim()
    let key1 = cellRange.substring(cut + 1).trim()

    let col0 = getColumn(key0)
    let col1 = getColumn(key1)

    let row0 = getRow(key0)
    let row1 = getRow(key1)

    let startCol = Math.min(col0, col1)
    let startRow = Math.min(row0, row1)

    let endCol = Math.max(col0, col1)
    let endRow = Math.max(row0, row1)

    for (let row = startRow; row <= endRow; row++) {
        for (let col = startCol; col <= endCol; col++) {
            callback(toCellId(col, row))
        }
    }
}

export function nullToEmtpy(s) {
    return s == null ? "" : s
}


export function post(path, data) {
    let init = {method: "POST"}

    if (data == null) {
    } else if (typeof data === 'string' || data instanceof String) {
        // init.headers = { "Content-Type": "application/json" }
        init.body = data
    } else {
        init.headers = {"Content-Type": "application/json"}
        init.body = JSON.stringify(data)
    }
    fetch(path, init).then((response) => {
        if (!response.ok) {
            alertDialog("Request Error", response.statusText)
        }
    }).catch((error) => {
        alertDialog("Request Exception", error.toString())
    })
}

export function toCellId(column, row) {
    return String.fromCodePoint(column + 64) + row
}

export function toRangeKey(column, row, colSpan, rowSpan, forceRange) {
    return toCellId(column, row) + (forceRange || rowSpan != 0 || colSpan != 0 ? ":" + toCellId(column + colSpan, row + rowSpan) : "")
}

export function transformSchema(schema, forOperation) {
    if (Array.isArray(schema)) {
        return schema.map(element => transformSchema(element))
    }

    let transformed = {...schema}
    let modifiers = schema.modifiers || []

    // delete transformed.options

    transformed.isExpression = forOperation && modifiers.indexOf("CONSTANT") == -1
    transformed.isReference = modifiers.indexOf("REFERENCE") != -1

    return transformed
}

export function ensureCategory(parent, name) {
    if (name == null || name == "") {
        return parent
    }
    let cut = name.indexOf('.')
    if (cut != -1) {
        let outer = ensureCategory(parent, name.substring(0, cut))
        return ensureCategory(outer, name.substring(cut + 1))
    }

    let target = null
    let before = null
    for (let child of parent.children) {
        if (child.localName == "details") {
            let category = child.firstElementChild.textContent
            if (category == name) {
                target = child
                break
            }
            if (category > name) {
                before = child
                break
            }
        }
    }

    if (target == null) {
        target = document.createElement("details")
        let summary = document.createElement("summary")
        summary.textContent = name
        target.appendChild(summary)
        parent.insertBefore(target, before)
    }

    return target
}


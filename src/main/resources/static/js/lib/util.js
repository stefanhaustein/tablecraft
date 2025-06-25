export function addOption(selectElement, name) {
    let option = document.createElement("option")
    option.textContent = name
    selectElement.appendChild(option)
}

export function camelCase(s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()
}

export function insertById(parent, element) {
    let id = element.id
    let existing = document.getElementById(id)
    if (existing) {
        existing.replaceWith(element)
    } else {
        let child = parent.firstElementChild
        while (child != null && child.id < id) {
            child = child.nextElementSibling
        }
        if (child == null) {
            parent.appendChild(element)
        } else {
            child.insertAdjacentElement("beforebegin", element)
        }
    }
}

export function updateSpec(parent, idPrefix, spec, createAction) {
    let id = idPrefix + spec.name

    if (spec.modifiers && spec.modifiers.indexOf("DELETED") != -1) {
        let existing = document.getElementById(id)
        if (existing) {
            existing.parentElement.removeChild(existing)
        }
        return existing  // should be null?
    }

    let element = document.createElement("div")
    element.id = idPrefix + spec.name

    if (createAction) {
        let addButtonElement = document.createElement("img")
        addButtonElement.src = "/img/add.svg"
        addButtonElement.style.float = "right"
        addButtonElement.onclick = createAction
        element.appendChild(addButtonElement)
    }

    let titleElement = document.createElement("div")
    let nameSpan = document.createElement("b")
    nameSpan.append(spec.name)
    titleElement.appendChild(nameSpan)
    if (spec.params && spec.params.length > 0) {
        titleElement.append("(" + spec.params.map((e) => e.name).join(", ") + ")")
    }
    if (spec.returnType.toLowerCase() != "void") {
        titleElement.append(": " + camelCase(spec.returnType))
    }
    titleElement.style.marginBottom = "5px"
    titleElement.style.marginTop = "10px"
    titleElement.style.textIndent = "-10px"
    titleElement.style.paddingLeft = "10px"
    element.appendChild(titleElement)

    let descriptionElement = document.createElement("div")
    descriptionElement.style.paddingLeft = "10px"

    let description = spec.description
    let cut = description.indexOf(".")

    descriptionElement.textContent = (cut == -1 ? description : description.substring(0, cut + 1))
    element.appendChild(descriptionElement)

    insertById(parent, element)

    return element
}


export function postJson(path, data) {
    let request = new Request(path, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    })
    fetch(request)
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

export function toRangeKey(column, row, colSpan, rowSpan, forceRange) {
    return toCellId(column, row) + (forceRange || rowSpan != 0 || colSpan != 0 ? ":" + toCellId(column + colSpan, row + rowSpan) : "")
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
export function addOption(selectElement, name) {
    let option = document.createElement("option")
    option.textContent = name
    selectElement.appendChild(option)
}

export function blink(element) {
    if (element) {
        element.classList.add("changed")
        setTimeout(() => {
            element.classList.remove("changed")
        }, 1000)
    }

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



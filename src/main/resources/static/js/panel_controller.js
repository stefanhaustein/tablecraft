
let panelSelectElement = document.getElementById("panelSelect")
let currentPanelName = ""
let currentPanelElement = null

selectPanel(panelSelectElement.value)

panelSelectElement.addEventListener("change", (ev) => {
    console.log("Select panel: " + name, ev)
    selectPanel(panelSelectElement.value)
})

export function selectPanel(name) {
    if (name == currentPanelName) {
        return
    }
    if (currentPanelElement != null) {
        currentPanelElement.style.display = "none"
    }
    currentPanelName = name
    panelSelectElement.value = name

    currentPanelElement = document.getElementById(name + "Panel")
    currentPanelElement.style.display = ""
}
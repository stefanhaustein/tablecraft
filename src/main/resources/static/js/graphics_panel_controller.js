import {commitCurrentCell, currentCellData} from "./shared_state.js";

let graphicsPanelElement = document.getElementById("GraphicsPanel")

graphicsPanelElement.addEventListener("click", event => {
    let src = event.target.getAttribute("src")

    if (src != null)  {
        currentCellData["i"] = src

        commitCurrentCell()
    }

})
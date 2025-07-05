import {setCurrentCellImage} from "./shared_state.js";

let graphicsPanelElement = document.getElementById("GraphicsPanel")


graphicsPanelElement.addEventListener("click", event => {
    let src = event.target.getAttribute("src")

    if (src != null || event.target.localName.toLowerCase() == "button")  {
        setCurrentCellImage(src)
    }

})

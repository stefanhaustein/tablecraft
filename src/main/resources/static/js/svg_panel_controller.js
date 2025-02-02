import {setCurrentCellFormula} from "./shared_state.js";

let graphicsPanelElement = document.getElementById("GraphicsPanel")

graphicsPanelElement.addEventListener("click", event => {
    let src = event.target.src

    if (src) {
        let cut = src.indexOf("//")
        cut = src.indexOf("/", cut + 2)

        setCurrentCellFormula("image(\"" + src.substring(cut) + "\")")
    }

})
import {setCurrentCellFormula} from "./shared_state.js";

let graphicsPanelElement = document.getElementById("GraphicsPanel")

graphicsPanelElement.addEventListener("click", event => {
    let src = event.target.src

    if (src) {
        let cut = src.indexOf("//")
        cut = src.indexOf("/", cut + 2)
        let path = src.substring(cut)

        if (path.indexOf("/line/") != -1) {
            setCurrentCellFormula("=image(\"" + path + "\")")
        } else {
            let cut2 = path.lastIndexOf(".")
            setCurrentCellFormula("=" + path.substring(1, cut2).replaceAll("/", ".") + "()")
        }
    }

})
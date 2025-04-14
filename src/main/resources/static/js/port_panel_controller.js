import {functions, ports} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";

let portListElement = document.getElementById("portList")

document.getElementById("addInputPort").addEventListener("click", () => showPortDialog("INPUT_PORT"))
document.getElementById("addOutputPort").addEventListener("click", () => showPortDialog("OUTPUT_PORT"))

portListElement.addEventListener("click", event => editPort(event))

function editPort(event) {
    if (event.target.className != "portConfig") {
        return;
    }
    let entryElement = event.target.parentNode
    let id = entryElement.id
    if (!id.startsWith("port.")) {
        // Clicked on input; not handled here.
        // console.log("Target element id not recognized: ", entryElement)
        return
    }
    let name = id.substring("port.".length)
    let portSpec = ports[name]

    showPortDialog(functions[portSpec.type].kind, portSpec)
}

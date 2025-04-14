import {functions, ports} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";

let portSelectElement = document.getElementById("portSelect")
let portListElement = document.getElementById("portList")

portSelectElement.addEventListener("change", addPort)
portListElement.addEventListener("click", event => editPort(event))

function addPort() {
    console.log("add port")

    let type = portSelectElement.value
    portSelectElement.selectedIndex = 0

    let typeSpec = functions[type]

    showPortDialog(typeSpec)
}


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
    let constructorSpec = functions[portSpec.type]

    showPortDialog(constructorSpec, portSpec)
}

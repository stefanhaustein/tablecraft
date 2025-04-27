import {insertById, updateSpec} from "./lib/util.js";
import {functions, integrations} from "./shared_state.js";
import {showIntegrationDialog} from "./integration_editor.js"

let integrationListElement = document.getElementById("integrationList")
let integrationSpecListElement = document.getElementById("integrationSpecList")

export function processIntegrationUpdate(name, integration) {
    let id = "integration." + name
    let element = document.getElementById(id)

    if (integration.type == "TOMBSTONE") {
        if (element != null) {
            integrationListElement.removeChild(element)
        }
        delete integrations[name]
    } else {
        integration.name = name
        integrations[name] = integration

        if (element == null) {
            element = document.createElement("div")
            element.id = id

            let nameSpan = document.createElement("span")
            nameSpan.textContent = name
            nameSpan.style.fontWeight = "bold"

            element.append(nameSpan, " (" + integration.type + ")")

            integrationListElement.appendChild(element)

            element.onclick = () => {
                let spec = functions[integration.type]
                showIntegrationDialog(spec, integration)
            }
        }
    }
}

export function updateIntegrationSpec(spec) {
    updateSpec(
        integrationSpecListElement,
        "integration.spec.",
        spec,
        () => { showIntegrationDialog(spec) })
}
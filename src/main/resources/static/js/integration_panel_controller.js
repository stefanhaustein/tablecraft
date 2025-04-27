import {insertById} from "./lib/util.js";
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

    let details = document.createElement("details")
    details.id = "integration.spec." + spec.name
    let summary = document.createElement("summary")
    summary.textContent = spec.name

    let separator = document.createElement("div")
    separator.style.clear = "both"

    let createButton = document.createElement("button")
    createButton.onclick = () => {
        showIntegrationDialog(spec)
    }
    createButton.style.float = "right"
    createButton.style.clear = "both"
    createButton.textContent = "Create"

    details.append(summary, spec.description, separator, createButton)

    insertById(integrationSpecListElement, details)
}
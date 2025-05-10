import {insertById, promptDialog, updateSpec} from "./lib/util.js";
import {functions, integrations} from "./shared_state.js";
import {showIntegrationInstanceConfigurationDialog} from "./integration_editor.js"

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
                showIntegrationInstanceConfigurationDialog(spec, integration)
            }
        }
    }
}

export function updateIntegrationSpec(spec) {
    updateSpec(
        integrationSpecListElement,
        "integration.spec.",
        spec,
        () => { showIntegrationCreationDialog(spec) })
}

async function showIntegrationCreationDialog(spec) {
    // Skip for singletons
    // TODO: Name validation
    let name = await promptDialog("Add " + spec.name, "Name")
    showIntegrationInstanceConfigurationDialog(spec, {name: name, type: spec.name, kind: spec.name})
}
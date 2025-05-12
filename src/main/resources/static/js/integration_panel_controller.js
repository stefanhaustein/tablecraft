import {updateSpec} from "./lib/util.js"
import {promptDialog} from "./lib/dialogs.js"
import {factories, integrations, ports} from "./shared_state.js";
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
                let spec = factories[integration.type]
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
    let name = spec.name

    if (spec.modifiers == null || spec.modifiers.indexOf("SINGLETON") == -1) {
        name = await promptDialog("Add " + name, name, {
            label: "Name",
            modifiers: ["CONSTANT"],
            validation: {
                "Integration name conflict": (name) => integrations[name] == null && (factories[name] == null || factories[name] == spec),
                "Port name conflict": (name) => ports[name] == null,
                "Valid: letters, non-leading '_' or digits": /^[a-zA-Z][a-zA-Z_0-9]*$/
            }
        })
        if (name == null) {
            return
        }
    }
    showIntegrationInstanceConfigurationDialog(spec, {name: name, type: spec.name, kind: spec.name})
}
import {updateSpec} from "./lib/dom.js"
import {promptDialog} from "./lib/dialogs.js"
import {showIntegrationInstanceConfigurationDialog} from "./integration_editor.js"
import {getFactory, getIntegrationInstance, registerIntegrationInstance} from "./shared_model.js";

let integrationListElement = document.getElementById("integrationList")
let integrationSpecListElement = document.getElementById("integrationSpecList")

export function processIntegrationUpdate(name, integration) {
    let key = name.toLowerCase()
    let id = "integration." + key
    let element = document.getElementById(id)

    if (!registerIntegrationInstance(name, integration)) {
        if (element != null) {
            integrationListElement.removeChild(element)
        }
    } else {
        if (element == null) {
            element = document.createElement("div")
            element.id = id

            let nameSpan = document.createElement("span")
            nameSpan.textContent = name
            nameSpan.style.fontWeight = "bold"

            element.append(nameSpan, " (" + integration.type + ")")

            integrationListElement.appendChild(element)

            element.onclick = () => {
                let spec = getFactory(integration.type)
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
                "Integration name conflict": (name) => getIntegrationInstance(name) == null && (getFactory(name) == null || getFactory(name) == spec),
                "Port name conflict": (name) => getPortInstance(name) == null,
                "Valid: letters, non-leading '_' or digits": /^[a-zA-Z][a-zA-Z_0-9]*$/
            }
        })
        if (name == null) {
            return
        }
    }
    showIntegrationInstanceConfigurationDialog(spec, {name: name, type: spec.name, kind: spec.name})
}
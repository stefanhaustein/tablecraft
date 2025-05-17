let factories = {}
let functions = {}
let integrations = {}
let ports = {}

export function getAllPorts() {
    return ports.values
}

export function getAllFactories() {
    return factories.values
}

export function getFactory(name) {
    return factories[name.toLowerCase()]
}

export function getFunction(name) {
    return functions[name.toLowerCase()]
}

export function getIntegrationInstance(name) {
    return integrations[name.toLowerCase()]
}

export function getIntegrationFactory(name) {
    return getFactory(name)
}

export function getPortFactory(name) {
    return getFactory(name)
}

export function getPortInstance(name) {
    return ports[name.toLowerCase()]
}

export function registerFactory(name, factory) {
    factory.name = name
    let key = factory.key = name.toLowerCase()
    if (factory.modifiers != null && factory.modifiers.indexOf("DELETED") != -1) {
        delete factories[key]
        return false
    }
    factories[key] = factory
    return true
}

export function registerFunction(name, f) {
    f.name = name
    let key = f.key = f.name.toLowerCase()
    functions[key] = f
}

export function registerIntegrationInstance(name, instance) {
    instance.name = name
    let key = name.toLowerCase()
    instance.key = key
    if (instance.type == "TOMBSTONE") {
        delete integrations[key]
        return false
    }
    integrations[key] = instance
    return true
}

export function registerPortInstance(name, instance) {
    instance.name = name
    let key = instance.name.toLowerCase()
    instance.key = key
    if (instance.type == "TOMBSTONE") {
        delete ports[key]
        return false
    }
    ports[key] = instance
    return true
}



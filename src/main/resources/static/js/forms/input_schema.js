export function getType(schema) {
    if (schema.type) {
        return Array.isArray(schema.type) ? "Enum" : schema.type.toString();
    }
    if (schema.options) {
        return "Enum"
    }
    return "String"
}

export function getOptions(schema) {
    if (schema.options) {
        return schema.options
    }
    if (Array.isArray(schema.type)) {
        return schema.type
    }
    if (getType(schema).toLowerCase() == "Bool") {
        return ["True", "False"]
    }
}

export function containsModifier(schema, modifier) {
    if (schema.modifiers == null) {
        return false
    }
    return schema.modifiers.indexOf(modifier) != -1
}

export function getType(schema) {
    if (schema.type) {
        return Array.isArray(schema.type) ? "Enum" : schema.type.toString();
    }
    if (schema.options) {
        return "Enum"
    }
    return "Unspecified"
}

export function getTypeLabel(schema) {
    let t = getType(schema)

    if (t == "Unspecified") {
        if (isReference(schema)) {
            return "Ref."
        }
        if (!isConstant(schema)) {
            return "Expr."
        }
        return ""
    }
    if (isReference(schema)) {
        return t + " ref."
    }
    if (!isConstant(schema)) {
        return t + " expr."
    }
    return t
}


export function isLiteral(schema) {
    return isConstant(schema) && !isReference(schema)
}

export function isReference(schema) {
    return containsModifier(schema, "REFERENCE")
}

export function isConstant(schema) {
    return containsModifier(schema, "CONSTANT")
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

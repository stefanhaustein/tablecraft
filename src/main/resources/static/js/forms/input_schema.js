export function getType(schema) {
    if (schema.type) {
        if (Array.isArray(schema.type)) {
            return "Enum"
        }
        if (typeof schema.type == "object") {
            return schema.type
        }
        let name = schema.type.toString()
        if (name.toLowerCase().startsWith("bool")) {
            return "Bool"
        }
        return name
    }
    if (schema.options) {
        return "Enum"
    }
    return "Unspecified"
}

export function getTypeLabel(schema) {
    let t = getType(schema)

    if (t == "Unspecified") {
        if (schema.isReference) {
            return "Ref."
        }
        if (schema.isExpression) {
            return "Expr."
        }
        return ""
    }
    if (schema.isReference) {
        return t + " ref."
    }
    if (schema.isExpression) {
        return t + " expr."
    }
    return t
}


export function isLiteral(schema) {
    return !schema.isExpression && !schema.isReference
}



export function getOptions(schema) {
    if (schema.options) {
        return schema.options
    }
    if (Array.isArray(schema.type)) {
        return schema.type
    }
    if (getType(schema) == "Bool") {
        return [{label:"True", value: true}, {label:"False", value:false}]
    }
}


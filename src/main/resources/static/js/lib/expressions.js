let regex = /[a-zA-Z0-9_.]+|\+|:|-|<|>|=|\*|\(|\)|,/g


function tokenize(s) {
    let tokenized = s.match(regex)
    console.log("tokenized", tokenized)
    return tokenized
}


export function extractParameters(expr, expectedParams) {
    let result = {}
    let tokens = tokenize(expr) || []
    let expectedParameterIndex = 0

    let parameterName = ""
    let collecting = false
    let collected = ""
    let depth = 0

    for (let i = 0; i < tokens.length; i++) { // in loop uses strings
        let token = tokens[i]
        if (collecting) {
            if (depth == 0 && (token == "," || token == ")")) {
                collecting = false
                result[parameterName] = collected
            } else {
                collected += token
            }
        } else if (depth == 0) {
            if (/[a-zA-Z]+/.test(token) && tokens[i + 1] == "=") {
                parameterName = token
                i++
                collected = ""
            } else {
                let param = expectedParams[expectedParameterIndex]
                parameterName = param != null ? param.name : (""+expectedParameterIndex)
                collected = token
                collecting = true
                expectedParameterIndex++
            }
            collecting = true
        }

        switch (token) {
            case "(":
            case "{":
            case "[":
                depth++;
                break;
            case ")":
            case "}":
            case "]":
                depth--;
                break;
        }
    }

    if (collecting) {
        result[parameterName] = collected
    }

    return result
}

let regex = /[a-zA-Z0-9.]+|\+|-|<|>|=|\*|\(|\)|,/g


export function tokenize(s) {
    return s.match(regex)
}
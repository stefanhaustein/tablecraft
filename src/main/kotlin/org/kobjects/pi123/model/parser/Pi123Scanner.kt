package org.kobjects.pi123.model.parser

import org.kobjects.parsek.tokenizer.Scanner
import org.kobjects.parsek.tokenizer.RegularExpressions

class Pi123Scanner(
    input: String
) : Scanner<Pi123TokenType>(
    input,
    Pi123TokenType.EOF,
    RegularExpressions.WHITESPACE to null,
) {
}
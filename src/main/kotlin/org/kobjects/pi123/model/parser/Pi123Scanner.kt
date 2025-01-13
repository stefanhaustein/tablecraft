package org.kobjects.pi123.model.parser

import org.kobjects.parsek.tokenizer.Scanner
import org.kobjects.parsek.tokenizer.RegularExpressions

class Pi123Scanner(
    input: String
) : Scanner<Pi123TokenType>(
    input,
    Pi123TokenType.EOF,
    RegularExpressions.WHITESPACE to null,
    RegularExpressions.IDENTIFIER to Pi123TokenType.IDENTIFIER,
    RegularExpressions.NUMBER to Pi123TokenType.NUMBER,
    RegularExpressions.DOUBLE_QUOTED_STRING to Pi123TokenType.STRING,
    RegularExpressions.SYMBOL to Pi123TokenType.SYMBOL
) {
}
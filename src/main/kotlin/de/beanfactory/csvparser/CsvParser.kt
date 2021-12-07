package de.beanfactory.csvparser

import de.beanfactory.csvparser.CsvParser.ParserState.*

/**
 * contains a field, read from the csv rows
 */
data class Field(val name: String, val value: String)

/**
 * contains a data row of fields
 */
data class Row(var fields: List<Field> = ArrayList()) {
    fun add(field: Field) {
        fields += field
    }
}

/**
 * CsvParser implementation.
 *
 * (C) 2021 by Thomas Strau&szlig; All rights reserved.
 *
 *
 */
class CsvParser {

    private var fieldNames: List<String>? = null
    private var currentField: Int = 0

    /**
     * parse the given string as csv.
     *
     * <p>
     * Supported features:
     * <ul><li>quoted multiline fields
     * <li>escaping characters inside fields (using backslash)
     * <li>escaped multiline fields (using backslash)
     * <li>basic error handling
     * </ul>
     */
    fun parse(csvString: String): List<Row> {
        val rows = ArrayList<Row>()
        var currentRow = Row()

        var pos = 0
        var state: ParserState = START

        while (pos < csvString.length) {
            when (state) {
                START -> {
                    state = START_OF_ROW
                }
                START_OF_ROW -> {
                    when (csvString[pos]) {
                        '\n' -> pos++ // skip empty lines
                        else -> state = READ_FIELD_VALUE
                    }
                }
                READ_FIELD_VALUE -> {
                    val fieldName = nextFieldName()
                    val result: ParseResult<Field> = readField(
                        fieldName, csvString.substring(pos)
                    )
                    pos += result.newPos
                    currentRow.add(result.value)
                    state = NEXT_FIELD
                }
                END_OF_ROW -> { // end of Row reached, process and start
                    pos++
                    rows += currentRow
                    currentRow = Row()
                    currentField = 0
                    state = START_OF_ROW
                }
                NEXT_FIELD -> {
                    when (csvString[pos]) {
                        ';' -> { // start new field value
                            pos++
                            state = READ_FIELD_VALUE
                        }
                        ' ' -> pos++  // skips whitespace after field (sketchy csv!)
                        '\n' -> state = END_OF_ROW
                        else -> {
                            state = ERROR_EXPECTED_FIELD_SEPARATOR
                        }
                    }
                }
                ERROR_EXPECTED_FIELD_SEPARATOR -> {
                    println(
                        "Unexpected end of row, expected field separator " + csvString.substring(
                            0, Integer.min(pos + 1, csvString.length)
                        ) + " <-- here"
                    )
                    throw RuntimeException()
                }
                else -> throw RuntimeException(
                    "Unknown state $state @ ${pos}, " + "consumed so far: " + csvString.substring(
                        0, pos
                    )
                )
            }
        }
        csvString.chars()

        if (currentRow.fields.isNotEmpty()) {
            rows += currentRow
        }

        return rows
    }

    private fun readField(name: String, inputData: String): ParseResult<Field> {
        var pos = 0
        var value = ""
        var state: ParserState = START

        while (pos < inputData.length) {
            when (state) {
                START -> {
                    state = when (inputData[pos]) {
                        '"' -> {
                            pos++
                            READ_QUOTED_CHARS
                        }
                        else -> READ_UNQUOTED_CHARS
                    }
                }
                READ_UNQUOTED_CHARS -> {
                    val result = readFieldChar(";\n", inputData.substring(pos), false)
                    state = result.state ?: state
                    pos += result.newPos
                    value += result.value
                    // Field terminates at end of data
                    if (pos >= inputData.length) state = END_OF_FIELD
                }
                READ_QUOTED_CHARS -> {
                    val result = readFieldChar("\"", inputData.substring(pos), true)
                    state = result.state ?: state
                    pos += result.newPos
                    value += result.value
                    // Field cannot be terminated by end of data
                    if (pos >= inputData.length && state != END_OF_FIELD) state =
                        ERROR_UNTERMINATED_QUOTED_FIELD
                }
                END_OF_FIELD -> {
                    return ParseResult(pos, Field(name, value))
                }
                ERROR_INCOMPLETE_ESCAPE -> {
                    println(
                        "Unexpected end of data during escape character processing\n" + inputData.substring(
                            0, Integer.min(pos + 1, inputData.length)
                        ) + " <-- here"
                    )
                    throw RuntimeException()
                }
                else -> throw RuntimeException("unknown state $state")
            }
        }

        if (state != END_OF_FIELD) {
            println(
                "Unexpected end of data in state $state " + inputData.substring(
                    0, Integer.min(pos + 1, inputData.length)
                ) + " <-- here"
            )
            throw RuntimeException()
        }

        // end of field at end of file
        return ParseResult(pos, Field("", value))
    }

    private fun nextFieldName(): String {
        currentField++
        return if (fieldNames != null && fieldNames!!.size > currentField) {
            fieldNames!![currentField]
        } else {
            currentField.toString()
        }
    }

    private fun readFieldChar(
        terminator: String, inputData: String, readBehindTerminator: Boolean
    ): ParseResult<String> {
        val result = readNextChar(inputData)

        return if (terminator.contains(result.value) && result.state != ESCAPED_CHARACTER) {
            if (readBehindTerminator) {
                ParseResult(result.newPos, "", END_OF_FIELD)
            } else {
                ParseResult(result.newPos - 1, "", END_OF_FIELD)
            }
        } else {
            val state = if (result.state?.isError == true) result.state else null
            ParseResult(result.newPos, "${result.value}", state)
        }
    }

    private fun readNextChar(inputData: String): ParseResult<Char> {
        var pos = 0
        var state: ParserState? = null
        val value: Char

        if (inputData[pos] == '\\') {   // read escaped
            if (inputData.length <= 1) { // catch escape without data
                state = ERROR_INCOMPLETE_ESCAPE
                value = inputData[pos]
            } else {
                value = inputData[1]
                pos += 2
                state = ESCAPED_CHARACTER
            }
        } else {
            value = inputData[pos]
            pos += 1
        }

        return ParseResult(pos, value, state)
    }

    private data class ParseResult<T>(
        val newPos: Int, val value: T, val state: ParserState? = null
    )

    private enum class ParserState {
        START, READ_FIELD_VALUE, NEXT_FIELD, END_OF_ROW, START_OF_ROW, READ_QUOTED_CHARS, READ_UNQUOTED_CHARS, END_OF_FIELD, ESCAPED_CHARACTER, ERROR_EXPECTED_FIELD_SEPARATOR, ERROR_INCOMPLETE_ESCAPE, ERROR_UNTERMINATED_QUOTED_FIELD, ;

        val isError: Boolean = name.startsWith("ERROR")
    }
}
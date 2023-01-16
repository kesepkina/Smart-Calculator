package calculator

import java.lang.Exception
import java.math.BigInteger
import java.util.Stack
import kotlin.math.pow

const val EXIT_COMMAND = "/exit"
const val HELP_COMMAND = "/help"

const val PLUS = '+'
const val MINUS = '-'
const val MULT = '*'
const val DIV = '/'
const val POW = '^'
const val EQUAL_SIGN = '='

val IDENTIFIER_REGEX = "[A-Za-z]+".toRegex()
const val OPERATORS = "*/+()^-"
val OPERATORS_REGEX = "[*/+^-]".toRegex()

fun main() {
    val vars = mutableMapOf<String, BigInteger>()
    while (true) {
        val input = readln().trim()
        if (input.startsWith('/')) {
            when {
                input == EXIT_COMMAND -> break
                input == HELP_COMMAND -> println("The program calculates the sum and subtraction of numbers, " +
                        "that can be assigned to the variables.")
                else -> {
                    println("Unknown command")
                }
            }
        } else {
            if (input.isBlank()) continue
            if (input.contains(EQUAL_SIGN)) executeAssignment(input, vars)
            else if (validateExpression(input)) println(countPostfixExpression(convertInfixToPostfix(input), vars))
            else println("Invalid expression")
        }
    }
    println("Bye!")
}

fun executeAssignment(input: String, vars: MutableMap<String, BigInteger>): Boolean {
    val (variable, value) = input.split(EQUAL_SIGN).map { it.trim() }
    if (!variable.matches(IDENTIFIER_REGEX)) {
        println("Invalid identifier")
        return false
    }
    val convertedValue: BigInteger?
    if (value.matches(IDENTIFIER_REGEX)) {
        if (value in vars) {
            convertedValue = vars[value]!!
        } else {
            println("Unknown variable")
            return false
        }
    } else {
        convertedValue = value.toBigIntegerOrNull()
        if (convertedValue == null) {
            println("Invalid assignment")
            return false
        }
    }
    vars[variable] = convertedValue
    return true
}

fun convertInfixToPostfix(input: String): String {
    val operatorsPrecedence = mapOf(
        '+' to 0,
        '-' to 0,
        '*' to 1,
        '/' to 1,
        '^' to 2
    )
    var result = ""
    val stack = Stack<Char>()
    var lastSymbol: Char? = null
    val infix = if (input.trim().startsWith('-') || input.trim().startsWith('+')) {
        "0 $input"
    } else input
    for (symbol in infix) {
        if (symbol in OPERATORS) {
            if (!stack.empty() && symbol == lastSymbol && symbol in "+-") {
                if (symbol == '-') {
                    if (stack.peek() == MINUS) {
                        stack.pop()
                        stack.push(PLUS)
                    } else if (stack.peek() == PLUS) {
                        stack.pop()
                        stack.push(MINUS)
                    }
                }
            } else if (stack.empty() || (stack.peek() == '(')) {
                stack.push(symbol)
            } else if (symbol == '(') {
                stack.push(symbol)
            } else if (symbol == ')') {
                var topElem = stack.pop()
                while (topElem != '(') {
                    result += "$topElem "
                    topElem = stack.pop()
                }
            } else if (operatorsPrecedence[symbol]!! > operatorsPrecedence[stack.peek()]!!) {
                stack.push(symbol)
            } else {
                while (!stack.empty() && stack.peek() != '(' && operatorsPrecedence[symbol]!! <= operatorsPrecedence[stack.peek()]!!) {
                    result += "${stack.pop()} "
                }
                stack.push(symbol)
            }
        } else if (symbol != ' ') {
            if (result.isNotEmpty() && (lastSymbol!! !in OPERATORS)) {
                result = result.trim() + "$symbol "
            } else result += "$symbol "
        }
        if (symbol != ' ') lastSymbol = symbol
    }
    while (!stack.empty()) {
        result += "${stack.pop()} "
    }
    return result.trim()
}

fun validateExpression(expression: String): Boolean {
    val expressionRegexp = "\\(*[+-]?\\s?([0-9]|[a-zA-Z])+(\\s?([+-]+|[*/^])\\s?[()]*[+-]?([0-9]|[a-zA-Z])+[()]*)*".toRegex()
    val numOfParenthesesEquals = expression.count {it == '('} == expression.count {it == ')'}
    return expression.matches(expressionRegexp) && numOfParenthesesEquals
}

fun countPostfixExpression(expression: String, vars: MutableMap<String, BigInteger>): BigInteger? {
    val symbols = expression.split("\\s+".toRegex())
    val stack = Stack<BigInteger>()
    stack.push(BigInteger.ZERO)
    for (symbol in symbols) {
        if (symbol.matches(IDENTIFIER_REGEX)) {
            if (symbol in vars) {
                stack.push(vars[symbol])
            } else {
                print("Unknown variable")
                return null
            }
        } else if (symbol.matches(OPERATORS_REGEX)) {
            val numSecond = stack.pop()
            val numFirst = stack.pop()
            stack.push( when (symbol) {
                PLUS.toString() -> numFirst + numSecond
                MINUS.toString() -> numFirst - numSecond
                MULT.toString() -> numFirst * numSecond
                DIV.toString() -> numFirst / numSecond
                POW.toString() -> numFirst.toDouble().pow(numSecond.toDouble()).toInt().toBigInteger()
                else -> throw Exception("Operator not recognized")
            })
        } else {
            stack.push(symbol.toBigInteger())
        }
    }
    return stack.pop()
}

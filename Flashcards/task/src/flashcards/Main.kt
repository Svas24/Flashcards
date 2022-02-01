package flashcards

import java.io.File
import kotlin.random.Random

data class Card(val term: String, val definition: String, var mistakes: Int = 0)
val cards = mutableListOf<Card>()
val log = mutableListOf<String>()

fun printMsg (massage: String) = println(massage).also { log.add( massage )  }

fun responseTo (request: String): String {
    printMsg(request)
    return readLine()!!.also { log.add(it) }
}

fun importFrom(importFile: File?) {
    if (importFile != null && importFile.exists() && importFile.isFile) {
        var casrdLoaded = 0
        importFile.forEachLine { line ->
            val (term, definition, mistakes) = line.split(":")
            val doublesId = cards.indexOfFirst { it.term == term }
            if (doublesId >= 0) cards[doublesId] = Card(term, definition, cards[doublesId].mistakes + mistakes.toInt())
            else  cards.add(Card(term, definition, mistakes.toInt())).also { casrdLoaded++ }
        }.also { printMsg("$casrdLoaded cards have been loaded.") }
    } else printMsg("File not found.")
}

fun exportTo(exportFile: File?) {
    if (exportFile != null)
        cards.forEach { exportFile.appendText("${it.term}:${it.definition}:${it.mistakes}\n")
        }.also { printMsg("${cards.size} cards have been saved.") }
}

fun main(args: Array<String>) {

    val impArgPos = args.indexOfFirst { it == "-import" } + 1
    importFrom(if (impArgPos > 0 && impArgPos < args.size) File(args[impArgPos]) else null)
    val expArgId = args.indexOfFirst { it == "-export" } + 1
    val exportFile = if (expArgId > 0 && expArgId < args.size) File(args[expArgId]) else null

    while (true) when ( responseTo("\nInput the action (add, remove, import, export, ask, exit):") ) {

            "add" -> {
                val term = responseTo ("The card:")
                if (cards.none { it.term == term }) {
                    val definition = responseTo("The definition of the card:")
                    if (cards.none { it.definition == definition }) cards.add(Card(term, definition)).also {
                            printMsg("The pair (\"$term\":\"$definition\") has been added.") }
                    else printMsg("The definition \"$definition\" already exists.")
                } else printMsg("The card \"$term\" already exists.")
            }

            "remove" -> {
                val term = responseTo("Which card?")
                val position = cards.indexOfFirst { it.term == term }
                if (position >= 0) cards.removeAt(position).also{ printMsg ("The card has been removed.") }
                else printMsg("Can't remove \"$term\": there is no such card.")
            }

            "import" -> importFrom(File(responseTo("File name:")))

            "export" -> exportTo(exportFile?: File(responseTo("File name:")))

            "ask" -> {
                    val times = responseTo("How many times to ask?").toInt()
                    for (i in 0 until times) {
                        var randomCard = cards[Random.nextInt(cards.size)]
                        val answer = responseTo( "Print the definition of \"${randomCard.term}\":" )
                        if (answer == randomCard.definition) println ("Correct!")
                        else {
                            randomCard.mistakes += 1
                            val sameDefIdx = cards.indexOfFirst { it.definition == answer }
                            printMsg ("Wrong. The right answer is \"${randomCard.definition}\"" +
                                    if (sameDefIdx < 0) "."
                                    else ", but your definition is correct for \"${cards[sameDefIdx].term}\"." )
                        }
                    }
            }

            "log" -> {
                val file = File(responseTo("File name:"))
                log.forEach { file.appendText("$it\n") }.also { printMsg("The log has been saved.") }
            }

            "hardest card" -> {
                if (cards.isNotEmpty() && cards.any {it.mistakes > 0}) {
                    var maxMistakes = cards.maxOf { it.mistakes }
                    val hardestCards = cards.filter { it.mistakes == maxMistakes }.map { it.term }
                    if (hardestCards.size == 1) printMsg("The hardest card is \"${hardestCards.first()}\". " +
                                "You have ${maxMistakes} errors answering it.")
                    else printMsg("The hardest cards are \"${hardestCards.joinToString("\", \"")}\". " +
                                "You have ${maxMistakes} errors answering them.")
                } else printMsg("There are no cards with errors.")
            }

            "reset stats" -> cards.forEach { it.mistakes = 0 }.also { printMsg("Card statistics have been reset.") }

            "exit" -> {
                exportTo(exportFile)
                break
            }
        }

    println ("Bye bye!")
}
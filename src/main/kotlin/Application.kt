package ru.veryval.kotlinTGBot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.inlineQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import java.io.InputStream
import java.util.*





fun main() {

    val properties = Properties()
    val inputStream: InputStream = object {}.javaClass.getResourceAsStream("/config.properties")
        ?: throw IllegalStateException("Файл config.properties не найден")
    properties.load(inputStream)


    val botToken = properties.getProperty("BOT_TOKEN") ?: throw IllegalStateException("BOT_TOKEN не найден")

    val bot = bot {
        token = botToken

        dispatch {
            command("start") {

                val chatId = message.chat.id
                bot.sendMessage(ChatId.fromId(chatId), "Привет! Я ваш новый бот на Kotlin! Напиши \"привет\"")
            }

            command("hello") {
                val chatId = message.chat.id
                bot.sendMessage(ChatId.fromId(chatId), "Привет, ${message.from?.firstName}!")
            }

            command("userButtons") {
                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello, users buttons!",
                    replyMarkup = keyboardMarkup,
                )
            }

            text ("привет") {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Здарова!")
                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData(text = "Test Inline Button", callbackData = "testButton")),
                    listOf(InlineKeyboardButton.CallbackData(text = "Show alert", callbackData = "showAlert")),
                )
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello, inline buttons!",
                    replyMarkup = inlineKeyboardMarkup
                )
            }

            callbackQuery("testButton") {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery

                bot.sendMessage(ChatId.fromId(chatId), callbackQuery.data.plus(" something"))
            }

            callbackQuery(
                callbackData = "showAlert",
                callbackAnswerText = "HelloText",
                callbackAnswerShowAlert = true,
            ) {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(ChatId.fromId(chatId), callbackQuery.data)
            }


        }
    }

    bot.startPolling()
}


fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Request location (not supported on desktop)", requestLocation = true)),
        listOf(KeyboardButton("Request contact", requestContact = true)),
    )
}
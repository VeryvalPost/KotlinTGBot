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
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream
import java.util.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody



data class OpenAIRequest(val model: String, val messages: List<Message>)
data class Message(val role: String, val content: String)
data class OpenAIResponse(val choices: List<Choice>)
data class Choice(val message: Message)





fun main() {

    val properties = Properties()
    val inputStream: InputStream = object {}.javaClass.getResourceAsStream("/config.properties")
        ?: throw IllegalStateException("Файл config.properties не найден")
    properties.load(inputStream)


    val botToken = properties.getProperty("BOT_TOKEN") ?: throw IllegalStateException("BOT_TOKEN не найден")
    val openAiApiKey = properties.getProperty("OPENAI_API_KEY") ?: throw IllegalStateException("OPENAI_API_KEY не найден")


    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()


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


            command("askgpt") {
                val chatId = message.chat.id
                val userQuery = message.text?.removePrefix("/askgpt ") ?: ""

                if (userQuery.isNotBlank()) {
                    val response = askGpt(client, openAiApiKey, userQuery);
                    bot.sendMessage(ChatId.fromId(chatId), response)
                } else {
                    bot.sendMessage(ChatId.fromId(chatId), "Пожалуйста, введите запрос после команды /askgpt")
                }
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

fun askGpt(client: OkHttpClient, apiKey: String, userQuery: String): String {
    val url = "https://api.openai.com/v1/chat/completions"
    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    val requestBody = OpenAIRequest(
        model = "gpt-3.5-turbo",
        messages = listOf(Message(role = "user", content = userQuery))
    )

    val gson = Gson()
    val body: RequestBody = gson.toJson(requestBody).toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .post(body)
        .addHeader("Authorization", "Bearer $apiKey")
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()

    val openAIResponse = gson.fromJson(responseBody, OpenAIResponse::class.java)

    return openAIResponse.choices.firstOrNull()?.message?.content ?: "Извините, я не могу ответить на этот запрос."
}
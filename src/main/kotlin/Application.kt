package ru.veryval.kotlinTGBot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import java.io.InputStream
import java.util.*



data class Message(val role: String, val content: String)
data class Choice(val message: Message)






fun main() {

    val properties = Properties()
    val inputStream: InputStream = object {}.javaClass.getResourceAsStream("/config.properties")
        ?: throw IllegalStateException("Файл config.properties не найден")
    properties.load(inputStream)


    val botToken = properties.getProperty("BOT_TOKEN") ?: throw IllegalStateException("BOT_TOKEN не найден")
    val admphone = properties.getProperty("PHONE") ?: throw IllegalStateException("PHONE не найден")
    val admname = properties.getProperty("NAME") ?: throw IllegalStateException("NAME не найден")
    val admlastname = properties.getProperty("LASTNAME") ?: throw IllegalStateException("LASTNAME не найден")
    val adminId = properties.getProperty("ADMINCHATID") ?: throw IllegalStateException("ADMINCHATID не найден")
    val mytestId = properties.getProperty("VERYVALCHATID") ?: throw IllegalStateException("VERYVALCHATID не найден")




    val contactUsers: MutableSet<Contacts> = mutableSetOf()

    val bot = bot {
        token = botToken




        dispatch {
            command("start") {

                val chatId = message.chat.id

                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true, )
                val userName = message.chat.username

                val userContact = Contacts(chatId)
                contactUsers.add(userContact)



                println("User started a chat with the bot: chatID " + chatId.toString() +" Login: "+ userName )
                Logger.logger.info("User started a chat with the bot: chatID" + chatId+" Login: "+ userName)

                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Добрый день! Вас приветствует телеграмм бот образовательного центра \"Индиго\" 🌏\n"+
                "👇Нажмите кнопку внизу, чтобы подарить пробный урок другу и получить бонус от Индиго.\n \n"+
                            "_В случае работы не на мобильном устройстве выберите пункт меню /share_",
                    replyMarkup = keyboardMarkup,
                    parseMode = ParseMode.MARKDOWN,

                )


            }


            command("share") {
                val chatId = message.chat.id

                val id = bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Давайте познакомимся! Сначала введите свое имя.",

                )

                contactUsers.find { it.chatId == chatId}?.also { it.stage = "nameStage" }.also { it?.messID = id.first?.body()?.result?.messageId!! }
            }

            text() {
                val chatId = message.chat.id
                val areYouBot = message.from?.isBot
                val currentUser = contactUsers.find { it.chatId == chatId }


                if ((areYouBot == false)&& ((message.messageId - 1)==currentUser?.messID)){


                    val nickname = message.chat.username
                    val msg = message.text




                    if (currentUser?.stage.equals("nameStage")) {

                        contactUsers.find { it.chatId == chatId }?.also {
                            if (msg != null) {
                                it.firstName = msg
                            }
                        }


                        val id=bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Теперь, пожалуйста, введите телефон",

                            )
                        contactUsers.find { it.chatId == chatId }?.also { it.stage = "phoneStage" }.also { it?.messID = id.first?.body()?.result?.messageId!! }

                    }



                    if ((currentUser?.stage.equals("phoneStage"))&& ((message.messageId - 1)==currentUser?.messID)) {

                        contactUsers.find { it.chatId == chatId }?.also {
                            if (msg != null) {
                                it.phone = msg
                            }
                        }


                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Спасибо за уделенное время!",

                            )
                        contactUsers.find { it.chatId == chatId }?.also { it.stage = "FinishStage" }

                    }


                    if (currentUser?.stage.equals("FinishStage")) {

                        Logger.logger.info(
                            "Получен запрос на подарок от контакта: \n"
                                    + "Тел:" + currentUser?.phone + "\n"
                                    + "Имя:" + currentUser?.firstName + "\n"
                                    + "Фамилия:" + currentUser?.lastName + "\n"
                                    + "Логин: @" + nickname + "\n"
                        )

                        println(
                            "Получен запрос на подарок от контакта: \n"
                                    + "Тел:" + currentUser?.phone + "\n"
                                    + "Имя:" + currentUser?.firstName + "\n"
                                    + "Фамилия:" + currentUser?.lastName + "\n"
                                    + "Логин: @" + nickname + "\n"
                        )


                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Наш сотрудник уже получил уведомление и скоро свяжется с Вами. \n " +
                                    "Если вопрос срочный, то можете сами связаться с ним напрямую: \n"
                        )

                        bot.sendContact(
                            chatId = ChatId.fromId(chatId),
                            phoneNumber = admphone,
                            firstName = admname,
                            lastName = admlastname
                        )


                        bot.sendMessage(
                            chatId = ChatId.fromId(adminId.toLong()),
                            text = "Получен запрос на подарок от контакта: \n"
                                    + "Тел:" + currentUser?.phone + "\n"
                                    + "Имя:" + currentUser?.firstName + "\n"
                                    + "Фамилия:" + currentUser?.lastName + "\n"
                                    + "Логин: @" + nickname + "\n"
                        )


                        bot.sendMessage(
                            chatId = ChatId.fromId(mytestId.toLong()),
                            text = "Получен запрос на подарок от контакта: \n"
                                    + "Тел:" + currentUser?.phone + "\n"
                                    + "Имя:" + currentUser?.firstName + "\n"
                                    + "Фамилия:" + currentUser?.lastName + "\n"
                                    + "Логин: @" + nickname + "\n"
                        )
                    }
                }
            }


            contact {
                var chatIdClient = ChatId.fromId(message.chat.id)
                val userName = message.chat.username

                Logger.logger.info("Получен запрос на подарок от контакта: \n"
                        + "Тел:" + contact.phoneNumber + "\n"
                        + "Имя:" + contact.firstName + "\n"
                        + "Фамилия:" + contact.lastName + "\n"
                        + "Логин: @" + userName + "\n")

                println("Получен запрос на подарок от контакта: \n"
                        + "Тел:" + contact.phoneNumber + "\n"
                        + "Имя:" + contact.firstName + "\n"
                        + "Фамилия:" + contact.lastName + "\n"
                        + "Логин: @" + userName + "\n")


                bot.sendMessage(
                    chatId = chatIdClient,
                    text = "Наш сотрудник уже получил уведомление и скоро свяжется с Вами. Если вопрос срочный, то можете сами связаться с ним напрямую: \n")

                bot.sendContact(chatId = chatIdClient,phoneNumber = admphone, firstName = admname, lastName = admlastname)


                bot.sendMessage(chatId = ChatId.fromId(adminId.toLong()), text = "Получен запрос на подарок от контакта: \n"
                        + "Тел:" + contact.phoneNumber + "\n"
                        + "Имя:" + contact.firstName + "\n"
                        + "Фамилия:" + contact.lastName + "\n"
                        + "Логин: @" + userName + "\n"
                )


                bot.sendMessage(chatId = ChatId.fromId(mytestId.toLong()),text = "Получен запрос на подарок от контакта: \n"
                        + "Тел:" + contact.phoneNumber + "\n"
                        + "Имя:" + contact.firstName + "\n"
                        + "Фамилия:" + contact.lastName + "\n"
                        + "Логин: @" + userName + "\n"
                )


            }
        }
    }

    bot.startPolling()
}


fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Подарить бесплатное занятие", requestContact = true)),
    )
}



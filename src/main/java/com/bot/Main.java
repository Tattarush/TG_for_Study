package com.bot;

/*TODO : Идеи для реализации
 * Рейтинг пользователей
 * Напоминалка Настраиваемая
 * Возможность включить выключить
 * В перспективе переехать на постгрес
 *
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        //инициализация БД
        DatabaseManager.initDatabase();

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            //Регистрация бота
            TelegramBot myBot = new TelegramBot();
            telegramBotsApi.registerBot(myBot);
            logger.info("Бот запущен");

        } catch (TelegramApiException e) {
            logger.error("Ошибка подключения к боту ", e);
            e.printStackTrace();
        }
    }
}

package com.bot.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotHandler {
    /**
     * Основной метод для обработки входящего события.
     * @param update объект события от Telegram
     * @param userId ID пользователя, который совершил действие
     * @param chatId ID чата, куда слать ответ
     */
    void handle(Update update, long userId, long chatId);

}

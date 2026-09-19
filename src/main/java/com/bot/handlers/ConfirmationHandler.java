package com.bot.handlers;

import com.bot.*;
import com.bot.DTO.FinanceRecord;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ConfirmationHandler implements BotHandler {

    private final TelegramBot bot;

    public ConfirmationHandler(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Update update, long userId, long chatId) {
        //Обработчик реагирует на кнопки да\ нет
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            switch (callbackData) {
                //Ответ да
                case "confirm_yes_clicked":
                    //Забираем информацию о дате и сумме
                    FinanceRecord record = bot.getTemporaryData().get(userId);

                    if (record != null) {
                        //Сериализация в JSON строку
                        String jsonString = new com.google.gson.Gson().toJson(record);

                        //Запись JSON в БД
                        FinanceDAO.saveUserNote(userId, jsonString);
                        // Очистка временных записей и возврат статуса в начало
                        bot.getTemporaryData().remove(userId);
                        bot.getUserStates().put(userId, BotState.MAIN_MENU);

                        bot.sendMessageWithKeyboard(chatId, "Данные успешно внесены",
                                InlineKeyboardFactory.createMainMenuKeyboard());
                    } else {
                        //В случае отсутствия данных запрашиваем заново
                        bot.sendMessageWithKeyboard(chatId, "Произошла ошибка, данные не найдены, попробуй снова",
                                InlineKeyboardFactory.createMainMenuKeyboard());
                        bot.getUserStates().put(userId, BotState.MAIN_MENU);
                    }
                    break;
                //пользователь выбрал - нет , данные неверны
                case "confirm_no_clicked":
                    //Убираем неверную запись с кэша
                    bot.getUserStates().remove(userId);
                    //Возврат на шаг назад
                    bot.getUserStates().put(userId, BotState.AWAITING_INPUT);
                    bot.sendMessageWithKeyboard(chatId, "Ввод отменен\n" +
                                    "\nВведите данные в формате: год.месяц.число(пробел)сумма",
                            InlineKeyboardFactory.createBackButtonKeyboard());
                    break;
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            bot.sendMessageWithKeyboard(chatId, "Выбери да / нет\n или вернись в главное меню", InlineKeyboardFactory.createBackButtonKeyboard());
        }
    }
}

package com.bot.handlers;


import com.bot.BotState;
import com.bot.InlineKeyboardFactory;
import com.bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;



public class MainMenuHandler implements BotHandler {

    private final TelegramBot bot;

    public MainMenuHandler(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Update update, long userId, long chatId) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            if ("add_info_clicked".equals(callbackData)) {
                bot.getUserStates().put(userId, BotState.AWAITING_INPUT);// меняем состояние через геттер бота
                bot.sendMessageWithKeyboard(chatId,
                        "Введите данные в формате: год.месяц.число(пробел)сумма\nПример: 2026.09.18 5500",
                        InlineKeyboardFactory.createBackButtonKeyboard());
            } else if ("get_info_clicked".equals(callbackData)) {
                //Позже прикрутить логику запроса информации
                bot.sendMessageWithKeyboard(chatId,"Выбран запрос информации",null);
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            if ("/start".equals(messageText)) {
                bot.sendMessageWithKeyboard(chatId, "Доступ подтвержден!\nПриветствую, " +
                        update.getMessage().getFrom().getFirstName() + ".\nЧто ты хочешь сделать?", InlineKeyboardFactory.createMainMenuKeyboard());
            } else {
                bot.sendMessageWithKeyboard(chatId, "Команда не распознана, используй /start заново", null);
            }

        }
    }
}

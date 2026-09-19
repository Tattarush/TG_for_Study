package com.bot;

import com.bot.handlers.ConfirmationHandler;
import com.bot.handlers.InputInfoHandler;
import com.bot.handlers.MainMenuHandler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import com.bot.DTO.FinanceRecord;


public class TelegramBot extends TelegramLongPollingBot {


    private final Set<Long> users = new HashSet<>();
    private final Long admin_ID;

    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, FinanceRecord> temporaryData = new HashMap<>();

    //Обработчики событий
    private final MainMenuHandler mainMenuHandler = new MainMenuHandler(this);
    private final InputInfoHandler inputInfoHandler = new InputInfoHandler(this);
    private final ConfirmationHandler confirmationHandler = new ConfirmationHandler(this);


    public TelegramBot() {
        super();
        // Загрузка списка разрешенных ид юзеров и админа
        this.admin_ID = Long.parseLong(System.getenv("Admin_ID"));
        String whiteUsers = System.getenv("Users");
        if (whiteUsers != null && !whiteUsers.isEmpty()) {
            for (String idStr : whiteUsers.split(",")) {
                users.add(Long.parseLong(idStr.trim()));
            }
            System.out.println("Белый список успешно загружен");
        }
    }

    @Override
    public String getBotUsername() {
        String botName = System.getenv("botName");

        if (botName == null && botName.isEmpty()) {
            System.out.println("Имя бота не найдено!Проверь внимательно");
        }
        return botName;
    }

    @Override
    public String getBotToken() {
        String botToken = System.getenv("token");

        if (botToken == null && botToken.isEmpty()) {
            System.out.println("Токен неверен! или не найден");
        }
        return botToken;
    }

    public Map<Long, BotState> getUserStates() {
        return userStates;
    }

    public Map<Long, FinanceRecord> getTemporaryData() {
        return temporaryData;
    }

    @Override
    public void onUpdateReceived(Update update) {

        long user_id = 0;
        long chat_Id = 0;

        if (update.hasMessage()) {
            user_id = update.getMessage().getFrom().getId();
            chat_Id = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            user_id = update.getCallbackQuery().getFrom().getId();
            chat_Id = update.getCallbackQuery().getMessage().getChatId();
        }

        if (user_id == 0) return;

        if (!users.contains(user_id)) {
                sendMessage(chat_Id, "Брысь отсюда!");
                System.out.println("Заблокирован доступ - ид " + user_id);
                sendMessage(admin_ID, "Попытка доступа к боту, ИД юзера - "+ user_id);
                return;
            }

        BotState currentState = userStates.getOrDefault(user_id, BotState.MAIN_MENU);


            switch (currentState) {

                case MAIN_MENU:
                   mainMenuHandler.handle(update,user_id, chat_Id);
                    break;

                case AWAITING_INPUT:
                    inputInfoHandler.handle(update, user_id, chat_Id);
                    break;

                case AWAITING_CONFIRMATION:
                    confirmationHandler.handle(update, user_id, chat_Id);
                    break;
            }

        }


    public void sendMessageWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(long chatId, String textMessage) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);


        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка ответа");
            e.getMessage();
        }
    }
}

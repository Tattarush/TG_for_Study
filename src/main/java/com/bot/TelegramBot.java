package com.bot;

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

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long userId = update.getCallbackQuery().getFrom().getId();

            switch (callbackData) {

                case "add_info_clicked":
                    userStates.put(userId, BotState.AWAITING_INPUT);
                    sendMessage(chatId, "Ты выбрал - добавить информацию");
                    sendMessageWithKeyboard(chatId, "Введи данные в формате: год.месяц.число(пробел)сумма\nПример: 2026.09.18 5500", InlineKeyboardFactory.createBackButtonKeyboard());

                    break;
                case "get_info_clicked":
                    sendMessage(chatId, "Ты выбрал - запросить информацию");
                    List<String> savedJson = DatabaseManager.getUserNote(userId);
                    if (!savedJson.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Полный список записей\n\n");
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        for (String str : savedJson) {
                            try {
                                FinanceRecord record = gson.fromJson(str, FinanceRecord.class);
                                sb
                                        .append("Дата: ")
                                        .append(record.getDate())
                                        .append(" |  Сумма: ")
                                        .append(record.getAmount()+"\n");
                            } catch (com.google.gson.JsonSyntaxException e) {
                                System.err.println("Ошибка десериализации");
                            }
                        }

                        sendMessageWithKeyboard(chatId,sb.toString(),InlineKeyboardFactory.createBackButtonKeyboard());
                    } else {
                        sendMessage(chatId, "В бд нет записей ");
                    }
                    break;

                case "back_to_main_clicked":
                    userStates.put(userId, BotState.MAIN_MENU);
                    temporaryData.remove(userId);
                    sendMessageWithKeyboard(chatId, "Возврат в главное меню",InlineKeyboardFactory.createMainMenuKeyboard());
                    break;
                case "confirm_yes_clicked":
                    FinanceRecord record = temporaryData.get(userId);
                    if (record != null) {
                        String jsonString = new com.google.gson.Gson().toJson(record);
                        sendMessage(chatId, "Данные успешно перенесены в JSON");
                        DatabaseManager.saveUserNote(userId, jsonString);
                        temporaryData.remove(userId);
                        userStates.put(userId, BotState.MAIN_MENU);

                        sendMessageWithKeyboard(chatId, "Что дальше?",InlineKeyboardFactory.createMainMenuKeyboard());
                    } else {
                        sendMessage(chatId, "Произошла ошибка, данные не записаны");
                        userStates.put(userId, BotState.MAIN_MENU);
                    }
                    break;
                case "confirm_no_clicked":
                    temporaryData.remove(userId);
                    userStates.put(userId, BotState.AWAITING_INPUT);
                    sendMessageWithKeyboard(chatId, "Ввод данных отменен\n", InlineKeyboardFactory.createBackButtonKeyboard());
                    break;
            }
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
                    sendMessage(chat_Id, "Выбери да / нет");
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

    private void sendMessage(long chatId, String textMessage) {
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

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.Set;

public class TelegramBot extends TelegramLongPollingBot {

    private final Set<Long> users = new HashSet<>();
    private final Long admin_ID = Long.parseLong(System.getenv("Admin_ID"));

    public TelegramBot() {
        super();
        loadWhiteList();
    }


    private void loadWhiteList() {
        String whiteUsers = System.getenv("Users");
        if (whiteUsers != null && !whiteUsers.isEmpty()) {
            String[] splitId = whiteUsers.split(",");
            for (String ids : splitId) {
                try {
                    users.add(Long.parseLong(ids.trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка парсинга юзера - " + ids);
                }
            }
            System.out.println("Белый список успешно загружен");
        } else {
            System.err.println("Список юзеров пуст!");
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


        if (update.hasMessage() && update.getMessage().hasText()) {


            String message = update.getMessage().getText();

            switch (message) {
                case "/start":
                    sendMessage(chat_Id, "Доступ подтвержден!\n" +
                            "Приветствую, " + update.getMessage().getFrom().getFirstName() +
                            "\nЖду твоих команд!");
                    break;
                case "/help":
                    sendMessage(chat_Id, "Список доступных команд");
                    break;
                default:
                    sendMessage(chat_Id, "команда не распознана");
                    break;
            }

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

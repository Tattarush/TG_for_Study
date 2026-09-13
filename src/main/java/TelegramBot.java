import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TelegramBot extends TelegramLongPollingBot {

    private final Set<Long> users = new HashSet<>();
    private final Long admin_ID;

    public TelegramBot() {
        super();
        // Загрузка списка разрешенных ид юзеров
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

            switch (callbackData) {
                case "add_info_clicked":
                    sendMessage(chat_Id, "Ты выбрал - добавить информацию");
                    break;
                case "get_info_clicked":
                    sendMessage(chat_Id, "Ты выбрал - внести информацию");
                    break;
            }
        return;
        }



        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();

            if (message.equals("/start")) {
                sendButtons(chat_Id, "Доступ подтвержден!\nПриветствую, " +
                        update.getMessage().getFrom().getFirstName() + ".\nЧто ты хочешь сделать?");
            } else {
                sendMessage(chat_Id, "Команда не распознана, используй /start заново");
            }
        }

    }


    private void sendButtons(long chat_Id, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chat_Id));
        message.setText(text);

        //создать сетку для кнопок
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowInLine = new ArrayList<>();

        // Кнопка добавить информацию
        InlineKeyboardButton buttonAdd = new InlineKeyboardButton();
        buttonAdd.setText("Добавить инф");
        buttonAdd.setCallbackData("add_info_clicked");

        // кнопка запросить информацию
        InlineKeyboardButton buttonGet = new InlineKeyboardButton();
        buttonGet.setText("Запросить инф");
        buttonGet.setCallbackData("get_info_clicked");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonAdd);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonGet);

        rowInLine.add(row1);
        rowInLine.add(row2);
        markup.setKeyboard(rowInLine);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.getMessage();
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

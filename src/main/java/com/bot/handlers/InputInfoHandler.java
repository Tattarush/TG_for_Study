package com.bot.handlers;


import com.bot.BotState;
import com.bot.DTO.FinanceRecord;
import com.bot.FinanceDAO;
import com.bot.InlineKeyboardFactory;
import com.bot.TelegramBot;
import com.google.gson.Gson;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Обработчик состояния AWAITING_INPUT.
 * Отвечает за валидацию введенного текста и кнопку "Назад".
 * Класс для валидации и парсинга принятого сообщения
 *
 */

public class InputInfoHandler implements BotHandler {

    private final TelegramBot bot;
    private final Gson gson = new Gson();
    //Регулярное выражение для проверки
    private static final Pattern INPUT_PATTERN =
            Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}\\s\\d+(\\.\\d{1,2})?$");

    public InputInfoHandler(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Update update, long userId, long chatId) {
        BotState currentState = bot.getUserStates().getOrDefault(userId, BotState.ADD_INFO_MENU);
        //Обработка инлайн кнопок выбора режима
        if (currentState == BotState.ADD_INFO_MENU && update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            switch (callbackData) {
                case "add_new_info_clicked":
                    bot.getUserStates().put(userId, BotState.AWAITING_NEW_INPUT);
                    bot.sendMessageWithKeyboard(chatId, "Внесение новой записи\n\nВведи данные в формате: `ГГГГ.ММ.ДД СУММА` \nПример: 2026.09.20 5500",
                            InlineKeyboardFactory.createBackButtonKeyboard());
                    break;
                case "edit_info_clicked":
                    bot.getUserStates().put(userId, BotState.AWAITING_EDIT_INPUT);
                    bot.sendMessageWithKeyboard(chatId, "Редактирование записи\n\nВведи ДАТУ (которую хочешь изменить) и НОВУЮ СУММУ в формате: `ГГГГ.ММ.ДД СУММА` \nПример: 2026.09.20 7000",
                            InlineKeyboardFactory.createBackButtonKeyboard());
                    break;
                case "back_to_main_clicked":
                    bot.getUserStates().put(userId, BotState.MAIN_MENU);
                    bot.sendMessageWithKeyboard(chatId, "Возвращение в главное меню",
                            InlineKeyboardFactory.createMainMenuKeyboard());
                    break;
            }
            return;
        }

        if (update.hasCallbackQuery() && "back_to_main_clicked".equals(update.getCallbackQuery().getData())) {
            bot.getUserStates().put(userId, BotState.MAIN_MENU);
            bot.getTemporaryData().remove(userId);
            bot.sendMessageWithKeyboard(chatId,"Возвращение в главное меню",
                    InlineKeyboardFactory.createMainMenuKeyboard());
        }


        //Обработка ввода текста
        //если пользователь прислал обычный текст
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();

            if (currentState == BotState.AWAITING_NEW_INPUT) {
                FinanceRecord record = parseAndValidateInput(messageText);
                if (record == null) {
                    //если проверка не пройдена запрос на ввод заново
                    bot.sendMessageWithKeyboard(chatId, "Формат не соответствует, попробуй заново",
                            InlineKeyboardFactory.createBackButtonKeyboard());
                } else {
                    //Проверка пройдена и текст спарсился
                    bot.getTemporaryData().put(userId, record);
                    bot.getUserStates().put(userId, BotState.AWAITING_CONFIRMATION);
                }
            } else if (currentState == BotState.AWAITING_EDIT_INPUT) {
                FinanceRecord record = parseAndValidateInput(messageText);
                if (record == null) {
                    bot.sendMessageWithKeyboard(chatId, "Формат не соответствует, попробуй заново",
                            InlineKeyboardFactory.createBackButtonKeyboard());
                    return;
                }
                //Проверка есть ли запись
                boolean isDateExist = checkDateInDatabase(userId, record.getDate());

                if (!isDateExist) {
                    //если запись не найдена сохраняем и спрашиваем внести или нет
                    bot.getTemporaryData().put(userId, record);
                    bot.getUserStates().put(userId, BotState.AWAITING_EDIT_CONFIRM);
                    bot.sendMessageWithKeyboard(chatId, "Запись за дату " + record.getDate() + " не найдена в базе данных!\n\nХочешь внести её как новую запись?",
                            InlineKeyboardFactory.createEditConfirmKeyboard());
                } else {
                    //Если не найдена сначала удаляем старую запись
                    FinanceDAO.deleteUserNoteByDate(userId, record.getDate());
                    //теперь отправлем данные на стандартное подтверждение
                    goToConfirmation(userId, chatId, record, "Старая запись будет ЗАМЕНЕНА на следующую:");
                }
            }
            return;
        }

        if (currentState == BotState.AWAITING_EDIT_CONFIRM && update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            FinanceRecord cachedRecord = bot.getTemporaryData().get(userId);

            if (cachedRecord != null) {
                if ("edit_force_insert_yes".equals(callbackData)) {
                    //пользователь согласен на внесение записи
                    goToConfirmation(userId, chatId, cachedRecord, "Будет внесена новая запись (так как старая не нашлась):");
                } else if ("edit_force_insert_no".equals(callbackData)) {
                    //пользователь передумал
                    bot.getTemporaryData().remove(userId);
                    bot.getUserStates().put(userId, BotState.ADD_INFO_MENU);
                    bot.sendMessageWithKeyboard(chatId, "Редактирование отменено. Выбери действие:",
                            InlineKeyboardFactory.createAddInfoMenuKeyboard());
                }
            } else {
                bot.getUserStates().put(userId, BotState.MAIN_MENU);
                bot.sendMessageWithKeyboard(chatId, "Ошибка сессии, возврат в главное меню",
                        InlineKeyboardFactory.createMainMenuKeyboard());
            }
        }
    }

    private void goToConfirmation(long userId, long chatId, FinanceRecord record, String titlePrefix) {
        bot.getTemporaryData().put(userId, record);
        bot.getUserStates().put(userId, BotState.AWAITING_CONFIRMATION);
        bot.sendMessageWithKeyboard(chatId, titlePrefix + "\n\nДата: " + record.getDate() +
                        "\nСумма: " + record.getAmount() + " руб.\n\nИнформация верна?",
                InlineKeyboardFactory.createConfirmationKeyboard() );
    }


    private boolean checkDateInDatabase(long userId, String targetDate) {
        List<String> jsons = FinanceDAO.getAllUserNotes(userId);
        for (String json : jsons) {
            FinanceRecord r = gson.fromJson(json, FinanceRecord.class);
            if (targetDate.equals(r.getDate())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Метод проверяет строку пользователя и превращает её в объект FinanceRecord.
     *  * *@param text Строка от пользователя (например, "2026.09.16 5500")
     *  * @return Объект с данными, или null, если формат не подошел*/

    private FinanceRecord parseAndValidateInput(String text) {
        if( text == null) return null;

        String trimmedText = text.trim();

        if (!INPUT_PATTERN.matcher(trimmedText).matches()) {
            return null;
        }

        String[] parts = trimmedText.split(" ");
        String datePart = parts[0];
        String amountPart = parts[1];


        try {
            double amount = Double.parseDouble(amountPart);
            return new FinanceRecord(datePart, amount);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

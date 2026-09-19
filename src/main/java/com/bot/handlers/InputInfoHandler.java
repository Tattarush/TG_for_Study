package com.bot.handlers;


import com.bot.BotState;
import com.bot.DTO.FinanceRecord;
import com.bot.InlineKeyboardFactory;
import com.bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.regex.Pattern;

/**
 * Обработчик состояния AWAITING_INPUT.
 * Отвечает за валидацию введенного текста и кнопку "Назад".
 * Класс для валидации и парсинга принятого сообщения
 *
 */

public class InputInfoHandler implements BotHandler {

    private final TelegramBot bot;

    //Регулярное выражение для проверки
    private static final Pattern INPUT_PATTERN =
            Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}\\s\\d+(\\.\\d{1,2})?$");

    public InputInfoHandler(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Update update, long userId, long chatId) {
        //Сначала проверяем нажатие кнопки назад
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            if ("back_to_main_clicked".equals(callbackData)) {
                bot.getUserStates().put(userId, BotState.MAIN_MENU);
                bot.getTemporaryData().remove(userId);
                bot.sendMessageWithKeyboard(chatId,
                        "Возврат в главное меню",
                        InlineKeyboardFactory.createMainMenuKeyboard());
            }
            return;
        }
        //если пользователь прислал обычный текст
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            //Проверка и парсинг строки через метод ниже
            FinanceRecord record = parseAndValidateInput(messageText);

            if (record == null) {
                //если проверка не пройдена запрос на ввод заново
                bot.sendMessageWithKeyboard(chatId, "Формат не соответствует, попробуй заново",
                        InlineKeyboardFactory.createBackButtonKeyboard());
            } else {
                //Проверка пройдена и текст спарсился
                bot.getTemporaryData().put(userId, record);
                bot.getUserStates().put(userId, BotState.AWAITING_CONFIRMATION);

                //Подтверждение верна ли информация перед внесением записи
                bot.sendMessageWithKeyboard(chatId, "Будет внесена запись:\nДата: " + record.getDate() +
                        "\nСумма: " + record.getAmount() + "\n\nИнформация верна?",
                        InlineKeyboardFactory.createConfirmationKeyboard());

            }
        }
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

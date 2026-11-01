package com.bot.handlers;

import com.bot.BotState;
import com.bot.DTO.FinanceRecord;
import com.bot.FinanceDAO;
import com.bot.InlineKeyboardFactory;
import com.bot.TelegramBot;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class QueryInfoHandler implements BotHandler {

    private static final Logger log = LoggerFactory.getLogger(QueryInfoHandler.class);
    private final TelegramBot bot;
    private final Gson gson = new Gson();

    private static final Pattern SINGLE_DATE_PATTERN = Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}$");
    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}\\s*-\\s*\\d{4}\\.\\d{2}\\.\\d{2}$");


    public QueryInfoHandler(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Update update, long userId, long chatId) {
        BotState state = bot.getUserStates().getOrDefault(userId, BotState.QUERY_MENU);

        //Сначала обрабатываем все нажатия инлайн кнопок
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String queryId = update.getCallbackQuery().getId();

            try {
                org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery answer =
                        new org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery();
                answer.setCallbackQueryId(queryId);
                bot.execute(answer);
            } catch (Exception e) {
                log.error("Не удалось ответить на callback", e);
            }
            if ("back_to_main_clicked".equals(callbackData)) {
                bot.getUserStates().put(userId, BotState.MAIN_MENU);
                bot.sendMessageWithKeyboard(chatId, "Возврат в главное меню", InlineKeyboardFactory.createMainMenuKeyboard());
                return;
            }

            //Обработка кнопок меню выбора
            if (state == BotState.QUERY_MENU) {
                switch (callbackData) {
                    case "query_all_clicked":
                        showAllRecords(userId, chatId);
                        break;
                    case "query_month_clicked":
                        bot.getUserStates().put(userId, BotState.AWAITING_MONTH_INPUT);
                        bot.sendMessageWithKeyboard(chatId, "Выбери интересующий месяц", InlineKeyboardFactory.createMonthSelectKeyboard());
                        break;
                    case "query_period_clicked":
                        bot.getUserStates().put(userId, BotState.AWAITING_PERIOD_INPUT);
                        bot.sendMessageWithKeyboard(chatId, "Введи желаемый диапазон дат\n" +
                                        "В формате \nГГГГ.ММ.ДД - ГГГГ.ММ.ДД\nПример: 2026.01.01 - 2026.05.01 ",
                                InlineKeyboardFactory.createBackButtonKeyboard());
                        break;
                    case "query_single_clicked":
                        bot.getUserStates().put(userId, BotState.AWAITING_SINGLE_DATE);
                        bot.sendMessageWithKeyboard(chatId, "Введи конкретную дату в формате:\nГГГГ.ММ.ДД\nПример: 2026.09.19",
                                InlineKeyboardFactory.createBackButtonKeyboard());
                        break;
                }
                return;
            }

            //если пользователь уде выбрал пункт по месяцам и нажал конкретный месяц
            if (state == BotState.AWAITING_MONTH_INPUT) {
                if (callbackData.startsWith("month_")) {
                    //Извлекаем номер месяца
                    int monthNumber = Integer.parseInt(callbackData.replace("month_", ""));
                    //Вызываем метод обработки
                    handleMonthSelect(userId, chatId, monthNumber);
                    return;
                }
            }
        }

        // Обработка ввода текста
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText().trim();

            if (state == BotState.AWAITING_SINGLE_DATE) {
                handleSingleDateInput(userId, chatId, text);
            } else if (state == BotState.AWAITING_PERIOD_INPUT) {
                handlePeriodInput(userId, chatId, text);
            }
        }
    }

    //Метод вывода всех записей

    private void showAllRecords(long userId, long chatId) {
        List<String> joins = FinanceDAO.getAllUserNotes(userId);
        if (joins.isEmpty()) {
            bot.sendMessageWithKeyboard(chatId, "В базе нет записей",
                    InlineKeyboardFactory.createMainMenuKeyboard());
            bot.getUserStates().put(userId, BotState.MAIN_MENU);
            return;
        }

        // Tree map сортирует по дате
        Map<String, Double> sortedData = new TreeMap<>();
        for (String json : joins) {
            FinanceRecord r = gson.fromJson(json, FinanceRecord.class);
            sortedData.put(r.getDate(), r.getAmount());
        }
        renderBeautifulOutput(chatId, sortedData, "Полная история записей по порядку\n");
        bot.getUserStates().put(userId, BotState.MAIN_MENU);
    }

    //Метод - обработка конкретной даты
    private void handleSingleDateInput(long userId, long chatId, String text) {
        if (!SINGLE_DATE_PATTERN.matcher(text).matches()) {
            bot.sendMessageWithKeyboard(chatId, "Формат периода неверный!\nПопробуй еще раз",
                    InlineKeyboardFactory.createBackButtonKeyboard());
            return;
        }

        List<String> jsons = FinanceDAO.getAllUserNotes(userId);
        Map<String, Double> sortedData = new TreeMap<>();

        for (String json : jsons) {
            FinanceRecord r = gson.fromJson(json, FinanceRecord.class);
            if (text.equals(r.getDate())) {
                sortedData.put(r.getDate(), r.getAmount());
            }
        }
        if (sortedData.isEmpty()) {
            bot.sendMessageWithKeyboard(chatId, "За дату " + text + " записей не найдено\nПродолжим поиск информации?",
                    InlineKeyboardFactory.createQueryMenuKeyboard());
            bot.getUserStates().put(userId, BotState.QUERY_MENU);
            return;
        } else {

            renderBeautifulOutput(chatId, sortedData, "Информация по выбранной дате");
        }
        bot.getUserStates().put(userId, BotState.MAIN_MENU);
    }

    //Метод по диапазонам дат

    private void handlePeriodInput(long userId, long chatId, String text) {
        if (!PERIOD_PATTERN.matcher(text).matches()) {
            bot.sendMessageWithKeyboard(chatId, "Формат периода неверный!\nПопробуй еще раз",
                    InlineKeyboardFactory.createQueryMenuKeyboard());
            return;
        }

        String[] dates = text.split("-");
        String startDate = dates[0].trim();
        String endDate = dates[1].trim();

        List<String> jsons = FinanceDAO.getAllUserNotes(userId);
        Map<String, Double> sortedData = new TreeMap<>();

        for (String json : jsons) {
            FinanceRecord r = gson.fromJson(json, FinanceRecord.class);
            String recordDate = r.getDate();

            //Проверка вхождения в диапазон
            if (recordDate.compareTo(startDate) >= 0 && recordDate.compareTo(endDate) <= 0){
                sortedData.put(recordDate, sortedData.getOrDefault(recordDate, 0.0) + r.getAmount());
            }
        }
        if (sortedData.isEmpty()) {
            bot.sendMessageWithKeyboard(chatId, "За период с " + startDate + " по " + endDate + " записей не найдено.\nПродолжим поиск информации?",
                    InlineKeyboardFactory.createQueryMenuKeyboard());
            bot.getUserStates().put(userId, BotState.QUERY_MENU);
            return;
        } else {
            renderBeautifulOutput(chatId, sortedData, "Результат выборки за период \n(" + startDate + " — " + endDate + "):\n");
        }
        bot.getUserStates().put(userId, BotState.MAIN_MENU);
    }


    private void handleMonthSelect(long userId, long chatId, int monthNumber) {
        //получаем текущий год
        int currentYear = java.time.LocalDate.now().getYear();

        //Форматируем месяц
        String monthStr = String.format("%02d", monthNumber);
        //Формируется префикс для запроса
        String datePrefix = currentYear + "." + monthStr + ".";

        List<String> jsons = FinanceDAO.getAllUserNotes(userId);
        Map<String, Double> sortedData = new TreeMap<>();

        for (String json : jsons) {
            FinanceRecord r = gson.fromJson(json, FinanceRecord.class);
            if (r.getDate().startsWith(datePrefix)) {
                sortedData.put(r.getDate(), sortedData.getOrDefault(r.getDate(), 0.0) + r.getAmount());
            }
        }
        String[] monthNames = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        String monthName = monthNames[monthNumber - 1];
        if (sortedData.isEmpty()) {
            bot.sendMessageWithKeyboard(chatId, "За " + monthName + " " + currentYear + " записей не найдено",
                    InlineKeyboardFactory.createQueryMenuKeyboard());
            bot.getUserStates().put(userId, BotState.QUERY_MENU);
        } else {
            renderBeautifulOutput(chatId, sortedData, "Результат выборки за " + monthName + " " + " г.\n");
            bot.getUserStates().put(userId, BotState.MAIN_MENU);
        }
    }


    //Вспомогательный метод для красивой обертки текста
    private void renderBeautifulOutput(long chatId, Map<String, Double> sortedData, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        double totalSum = 0;

        for (Map.Entry<String, Double> entry : sortedData.entrySet()) {
            sb.append(entry.getKey()).append(" | ").append(entry.getValue()).append("\n");
            totalSum += entry.getValue();
        }
        sb.append("\n")
                .append("Итого полная сумма\nза выбранные даты - ")
                .append(totalSum);
        bot.sendMessageWithKeyboard(chatId,sb.toString(),
                InlineKeyboardFactory.createMainMenuKeyboard());
    }
}

package com.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboardFactory {
    //Класс для сборки блоков инлайн клавиш


    //Инлайн кнопки главного меню
    public static InlineKeyboardMarkup createMainMenuKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowInline = new ArrayList<>();

        //Кнопка добавить информацию
        InlineKeyboardButton buttonAdd = new InlineKeyboardButton();
        buttonAdd.setText("Добавить информацию");
        buttonAdd.setCallbackData("add_info_clicked");

        //кнопка запросить информацию
        InlineKeyboardButton buttonGet = new InlineKeyboardButton();
        buttonGet.setText("Запросить информацию");
        buttonGet.setCallbackData("get_info_clicked");

        //Расклад кнопок по рядам
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonAdd);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonGet);

        rowInline.add(row1);
        rowInline.add(row2);

        markupInline.setKeyboard(rowInline);
        return markupInline;
    }

    //Создание клавиатуры с одной кнопкой назад в главное меню
    public static InlineKeyboardMarkup createBackButtonKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowInline = new ArrayList<>();

        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад в главное меню");
        buttonBack.setCallbackData("back_to_main_clicked");
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonBack);
        rowInline.add(row1);

        markupInline.setKeyboard(rowInline);
        return markupInline;
    }
    //Добавляем две кнопки выбора да \ нет на горизонтальной линии
    public static InlineKeyboardMarkup createConfirmationKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowInline = new ArrayList<>();

        InlineKeyboardButton buttonYes = new InlineKeyboardButton();
        buttonYes.setText("Да");
        buttonYes.setCallbackData("confirm_yes_clicked");

        InlineKeyboardButton buttonNo = new InlineKeyboardButton();
        buttonNo.setText("Нет");
        buttonNo.setCallbackData("confirm_no_clicked");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonYes);
        row1.add(buttonNo);

        rowInline.add(row1);

        markupInline.setKeyboard(rowInline);
        return markupInline;
    }

    public static InlineKeyboardMarkup createQueryMenuKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowInline = new ArrayList<>();
//        Кнопка выбора всех записей
        InlineKeyboardButton buttonAll = new InlineKeyboardButton();
        buttonAll.setText("Все записи");
        buttonAll.setCallbackData("query_all_clicked");
//кнопка периода
        InlineKeyboardButton buttonPeriod = new InlineKeyboardButton();
        buttonPeriod.setText("За период");
        buttonPeriod.setCallbackData("query_period_clicked");
//Кнопка конкретной записи
        InlineKeyboardButton buttonSingle = new InlineKeyboardButton();
        buttonSingle.setText("За конкретную дату");
        buttonSingle.setCallbackData("query_single_clicked");
//Кнопка назад
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("back_to_main_clicked");

//       Разметка кнопок по 1 в ряд

        List<InlineKeyboardButton> r1 = new ArrayList<>();
        r1.add(buttonAll);
        List<InlineKeyboardButton> r2 = new ArrayList<>();
        r2.add(buttonPeriod);
        List<InlineKeyboardButton> r3 = new ArrayList<>();
        r3.add(buttonSingle);
        List<InlineKeyboardButton> r4 = new ArrayList<>();
        r4.add(buttonBack);

        rowInline.add(r1);rowInline.add(r2);
        rowInline.add(r3);rowInline.add(r4);

        markupInline.setKeyboard(rowInline);
        return markupInline;
    }

    public static InlineKeyboardMarkup createAddInfoMenuKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowLine = new ArrayList<>();

        InlineKeyboardButton buttonNew = new InlineKeyboardButton();
        buttonNew.setText("Внести новую информацию");
        buttonNew.setCallbackData("add_new_info_clicked");

        InlineKeyboardButton buttonEdit = new InlineKeyboardButton();
        buttonEdit.setText("Редактировать информацию");
        buttonEdit.setCallbackData("edit_info_clicked");

        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад в главное меню");
        buttonBack.setCallbackData("back_to_main_clicked");


        List<InlineKeyboardButton> r1 = new ArrayList<>();
        r1.add(buttonNew);
        List<InlineKeyboardButton> r2 = new ArrayList<>();
        r2.add(buttonEdit);
        List<InlineKeyboardButton> r3 = new ArrayList<>();
        r3.add(buttonBack);
        rowLine.add(r1);
        rowLine.add(r2);
        rowLine.add(r3);
        markupInline.setKeyboard(rowLine);
        return markupInline;
    }

}

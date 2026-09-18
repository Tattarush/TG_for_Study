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

}

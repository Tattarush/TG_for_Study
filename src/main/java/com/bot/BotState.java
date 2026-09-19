package com.bot;

public enum BotState {
    MAIN_MENU,  //главное меню
    AWAITING_INPUT, //ожидание ввода информации
    AWAITING_CONFIRMATION,// Ожидание подтверждения

    QUERY_MENU,  // ожидание выбора из вариантов запроса, все, по датам, конкретная дата
    AWAITING_PERIOD_INPUT, //Ожидание ввода периода дат
    AWAITING_SINGLE_DATE //жидание ввода конкретной даты
}

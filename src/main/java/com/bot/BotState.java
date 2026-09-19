package com.bot;

public enum BotState {
    MAIN_MENU,  //главное меню
    AWAITING_INPUT, //ожидание ввода информации
    AWAITING_CONFIRMATION,// Ожидание подтверждения

    QUERY_MENU,  // ожидание выбора из вариантов запроса, все, по датам, конкретная дата
    AWAITING_PERIOD_INPUT, //Ожидание ввода периода дат
    AWAITING_SINGLE_DATE, //ожидание ввода конкретной даты

    ADD_INFO_MENU,  //  предложение выбора вариантов внесения информации
    AWAITING_NEW_INFO, // ожидание ввода новой информации
    AWAITING_EDIT_INPUT  // ожидание ввода даты и суммы для исправления

}

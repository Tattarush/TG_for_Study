package com.bot;



import com.bot.DTO.FinanceRecord;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс-DAO (Data Access Object) для работы с финансовыми записями в таблице USER_NOTES
 * Отвечает ТОЛЬКО за чистый SQL (INSERT, SELECT)
 */

public class FinanceDAO {
    //Получаем логгер
    public static final Logger logger = LoggerFactory.getLogger(FinanceDAO.class);
    //Сохраняет новую JSON-строку в базу данных H2 Чистый INSERT

    public static void saveUserNote(long userId, String jsonString) {

        String sql = "INSERT INTO user_notes (user_id, note_text) VALUES (?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, jsonString);
            pstmt.executeUpdate();
            //Скобки {} означают подстановку параметра, как в printf
            logger.info("LOG [Database]: Запись для {} успешно добавлена",userId);


        } catch (SQLException e) {
            logger.error("Ошибка при INSERT в БД у юзера с ИД - {}",userId, e);
        }
    }

    //Достает ВСЕ записи пользователя из базы данных.

    public static List<String> getAllUserNotes(long userId) {
        List<String> notes = new ArrayList<>();
        String sql = "SELECT note_text FROM user_notes WHERE user_id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String text = rs.getString("note_text");
                    if (text != null && !text.trim().isEmpty()) {
                        notes.add(text);
                    }
                }
                logger.info("Выгрузка всех записей юзера {} успешно произведена",userId);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при SELECT из БД у юзера - {}",userId, e);
        }
        return notes;
    }

    //TODO - класс удаление есть , но кнопки удалить щзапись нет , есть только редактирвоание, добавь
    public static void deleteUserNoteByDate(long userId, String dateTarget) {

        List<String> jsons = getAllUserNotes(userId);
        Gson gson = new Gson();
        //запрос для удаления строки где совпадают юзе и точный текст
        String sqlDelete = "DELETE FROM user_notes WHERE user_id = ? AND note_text = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
            for (String json : jsons) {
                FinanceRecord record = gson.fromJson(json, FinanceRecord.class);
                //если дата совпала удаляем эту строку
                if (dateTarget.equals(record.getDate())) {
                    pstmt.setLong(1, userId);
                    pstmt.setString(2, json);
                    pstmt.executeUpdate();
                }
            }
            logger.info("LOG [Database]: Старые записи за {} удалены (если они были)", dateTarget);
        } catch (SQLException e) {
            logger.error("Ошибка удаления записи из БД у юзера {}", userId, e);
        }
    }
}

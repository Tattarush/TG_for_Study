package com.bot;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс-DAO (Data Access Object) для работы с финансовыми записями в таблице USER_NOTES.
 * Отвечает ТОЛЬКО за чистый SQL (INSERT, SELECT).
 */

public class FinanceDAO {

    //Сохраняет новую JSON-строку в базу данных H2 (Чистый INSERT).

    public static void saveUserNote(long userId, String jsonString) {

        String sql = "INSERT INTO user_notes (user_id, note_text) VALUES (?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, jsonString);

            pstmt.executeUpdate();
            System.out.println("LOG [Database]: Запись для " + userId + " успешно добавлена.");

        } catch (SQLException e) {
            System.out.println("Ошибка при INSERT в БД");
            e.printStackTrace();
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
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при SELECT из БД");
            e.printStackTrace();
        }
        return notes;
    }

}

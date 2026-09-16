import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {

        DatabaseManager.initDatabase();

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            TelegramBot myBot = new TelegramBot();

            telegramBotsApi.registerBot(myBot);

            System.out.println("Бот запущен");

        } catch (TelegramApiException e) {
            System.out.println("Ошибка подключения к боту " + e.getMessage());
            e.printStackTrace();
        }

    }
}

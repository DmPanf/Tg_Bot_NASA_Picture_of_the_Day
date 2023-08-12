import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

public class NASABot extends TelegramLongPollingBot {

    private static final String TOKEN = "YOUR_BOT_TOKEN";
    private static final String BOT_USERNAME = "YOUR_BOT_USERNAME";
    private static final String NASA_API_URL = "https://api.nasa.gov/planetary/apod?api_key=YOUR_NASA_API_KEY&date=";

    public static void main(String[] args) throws Exception {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new NASABot());
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();

            LocalDate date = getRandomDate();
            String imageUrl = fetchImageURL(date);

            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(imageUrl);
            try {
                execute(sendPhoto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String fetchImageURL(LocalDate date) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(NASA_API_URL + date.toString());

            String json = EntityUtils.toString(client.execute(request).getEntity());
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("url")) {
                return jsonObject.getString("url");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LocalDate getRandomDate() {
        Random random = new Random();
        int day = random.nextInt(365) + 1;  // random day of the year
        return LocalDate.ofYearDay(LocalDate.now().getYear(), day);
    }
}

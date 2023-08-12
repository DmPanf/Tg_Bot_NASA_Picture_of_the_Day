import khttp.get
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.time.LocalDate
import kotlin.random.Random

class NASABot : TelegramLongPollingBot() {

    companion object {
        const val TOKEN = "YOUR_BOT_TOKEN"
        const val BOT_USERNAME = "YOUR_BOT_USERNAME"
        const val NASA_API_URL = "https://api.nasa.gov/planetary/apod?api_key=YOUR_NASA_API_KEY&date="

        @JvmStatic
        fun main(args: Array<String>) {
            val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
            telegramBotsApi.registerBot(NASABot())
        }
    }

    override fun getBotUsername(): String = BOT_USERNAME

    override fun getBotToken(): String = TOKEN

    override fun onUpdateReceived(update: Update?) {
        update?.let {
            if (it.hasMessage() && it.message.hasText()) {
                val chatId = it.message.chatId.toString()
                val date = getRandomDate()
                val imageUrl = fetchImageURL(date)

                val sendPhoto = SendPhoto()
                sendPhoto.chatId = chatId
                sendPhoto.photo = imageUrl
                try {
                    execute(sendPhoto)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchImageURL(date: LocalDate): String? {
        val response = get("$NASA_API_URL$date")
        return response.jsonObject.optString("url", null)
    }

    private fun getRandomDate(): LocalDate {
        val day = Random.nextInt(365) + 1  // random day of the year
        return LocalDate.ofYearDay(LocalDate.now().year, day)
    }
}

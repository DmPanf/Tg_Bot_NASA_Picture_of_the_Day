# go get github.com/go-telegram-bot-api/telegram-bot-api
# go run nasa_bot.go

package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"math/rand"
	"net/http"
	"time"

	tgbotapi "github.com/go-telegram-bot-api/telegram-bot-api"
)

const (
	TELEGRAM_TOKEN = "YOUR_TELEGRAM_BOT_TOKEN"
	NASA_API_KEY   = "YOUR_NASA_API_KEY"
	NASA_API_URL   = "https://api.nasa.gov/planetary/apod?api_key=%s&date=%s"
)

func getRandomDate() string {
	currentYear := time.Now().Year()
	randomTime := time.Date(currentYear, time.Month(rand.Intn(12)+1), rand.Intn(28)+1, 0, 0, 0, 0, time.UTC)
	return randomTime.Format("2006-01-02")
}

func fetchNASAImage(date string) string {
	resp, err := http.Get(fmt.Sprintf(NASA_API_URL, NASA_API_KEY, date))
	if err != nil {
		return ""
	}
	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return ""
	}

	var result map[string]interface{}
	json.Unmarshal(body, &result)

	url, ok := result["url"].(string)
	if !ok {
		return ""
	}
	return url
}

func main() {
	bot, err := tgbotapi.NewBotAPI(TELEGRAM_TOKEN)
	if err != nil {
		panic(err)
	}

	u := tgbotapi.NewUpdate(0)
	u.Timeout = 60

	updates, err := bot.GetUpdatesChan(u)

	for update := range updates {
		if update.Message == nil {
			continue
		}

		date := getRandomDate()
		url := fetchNASAImage(date)

		if url == "" {
			msg := tgbotapi.NewMessage(update.Message.Chat.ID, "Sorry, couldn't fetch the photo.")
			bot.Send(msg)
			continue
		}

		photo := tgbotapi.NewPhotoUpload(update.Message.Chat.ID, url)
		bot.Send(photo)
	}
}

-- luarocks install lua-requests

local requests = require("requests")

local TELEGRAM_TOKEN = "YOUR_TELEGRAM_BOT_TOKEN"
local NASA_API_KEY = "YOUR_NASA_API_KEY"
local BASE_TELEGRAM_URL = "https://api.telegram.org/bot" .. TELEGRAM_TOKEN
local NASA_API_URL = "https://api.nasa.gov/planetary/apod?api_key=" .. NASA_API_KEY .. "&date="

local function getRandomDate()
    return os.date("%Y-%m-%d", math.random(os.time({year=os.date("%Y"), month=1, day=1}), os.time()))
end

local function fetchNASAImage(date)
    local response = requests.get(NASA_API_URL .. date)
    if response.status_code == 200 then
        local data = response.json()
        return data.url
    end
    return nil
end

local function getUpdates(offset)
    local response = requests.get(BASE_TELEGRAM_URL .. "/getUpdates?timeout=100&offset=" .. (offset or ""))
    if response.status_code == 200 then
        return response.json().result
    end
    return {}
end

local function sendMessage(chat_id, text)
    requests.get(BASE_TELEGRAM_URL .. "/sendMessage?chat_id=" .. chat_id .. "&text=" .. text)
end

local function sendPhoto(chat_id, photo_url)
    requests.get(BASE_TELEGRAM_URL .. "/sendPhoto?chat_id=" .. chat_id .. "&photo=" .. photo_url)
end

local offset

while true do
    local updates = getUpdates(offset)
    for _, update in ipairs(updates) do
        if update.message and update.message.text then
            local chat_id = update.message.chat.id
            local date = getRandomDate()
            local photo_url = fetchNASAImage(date)
            if photo_url then
                sendPhoto(chat_id, photo_url)
            else
                sendMessage(chat_id, "Sorry, couldn't fetch the photo.")
            end
        end
        offset = update.update_id + 1
    end
    os.execute("sleep " .. tonumber(1))
end

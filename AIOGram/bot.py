# pip install aiogram aiohttp

import aiohttp
import random
from datetime import datetime, timedelta
from aiogram import Bot, types
from aiogram.dispatcher import Dispatcher
from aiogram.types import InputFile
from aiogram.utils import executor

API_TOKEN = 'YOUR_TELEGRAM_BOT_TOKEN'
NASA_API_KEY = 'YOUR_NASA_API_KEY'
API_URL = "https://api.nasa.gov/planetary/apod?api_key=" + NASA_API_KEY

bot = Bot(token=API_TOKEN)
dp = Dispatcher(bot)

async def fetch_nasa_image(date=None):
    params = {}
    if date:
        params['date'] = date

    async with aiohttp.ClientSession() as session:
        async with session.get(API_URL, params=params) as response:
            data = await response.json()
            if 'url' in data:
                return data['url'], data.get('title', "No title")
            return None, None

@dp.message_handler(commands=['start', 'help'])
async def send_welcome(message: types.Message):
    await message.reply("Привет! Нажмите /photo чтобы получить случайное фото дня от NASA за текущий год!")

@dp.message_handler(commands=['photo'])
async def send_random_nasa_photo(message: types.Message):
    current_year = datetime.now().year
    random_date = datetime(current_year, random.randint(1, 12), random.randint(1, 28)).strftime('%Y-%m-%d')
    
    image_url, title = await fetch_nasa_image(random_date)

    if not image_url:
        # Если нет изображения за случайную дату, получим последнее доступное изображение
        image_url, title = await fetch_nasa_image()
    
    if image_url:
        await bot.send_photo(chat_id=message.chat.id, photo=image_url, caption=title)
    else:
        await message.reply("Извините, не удалось загрузить изображение. Попробуйте позже.")

if __name__ == '__main__':
    executor.start_polling(dp, skip_updates=True)

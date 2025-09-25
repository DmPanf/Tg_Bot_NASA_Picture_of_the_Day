import gradio as gr
import requests
import random
import datetime

NASA_API_KEY = "DEMO_KEY"  # замените на свой ключ NASA
NASA_API_URL = "https://api.nasa.gov/planetary/apod"

def get_random_date():
    """Случайная дата между 1995-06-16 (старт APOD) и сегодня"""
    start_date = datetime.date(1995, 6, 16)
    end_date = datetime.date.today()
    delta = (end_date - start_date).days
    random_days = random.randint(0, delta)
    return start_date + datetime.timedelta(days=random_days)

def fetch_nasa_image():
    """Запрос к API NASA"""
    date = get_random_date().strftime("%Y-%m-%d")
    params = {"api_key": NASA_API_KEY, "date": date}
    response = requests.get(NASA_API_URL, params=params)
    if response.status_code == 200:
        data = response.json()
        if data.get("media_type") == "image":
            return data["url"], f"📅 Date: {date}\n\n📝 {data.get('title', '')}\n\n{data.get('explanation', '')}"
        else:
            return None, f"На {date} доступно видео: {data.get('url')}"
    return None, "Ошибка при запросе к API NASA"

# --- Gradio UI ---
with gr.Blocks() as demo:
    gr.Markdown("## 🚀 NASA Astronomy Picture of the Day (Random)")
    gr.Markdown("Каждый раз загружается случайное изображение из архива NASA APOD.")

    btn = gr.Button("🔄 Получить случайное изображение")
    image = gr.Image(type="filepath", label="NASA APOD")
    output = gr.Textbox(label="Информация", lines=8)

    btn.click(fn=fetch_nasa_image, outputs=[image, output])

demo.launch(server_name="0.0.0.0", share=True)

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import json
import time

URL = "https://x.psychometrix.co.il"
LOGIN_URL = f"{URL}/pages/loginout/login.aspx"
WORDS_URL = f"{URL}/learning-systems/words"

with open("scraping/credentials.json", "r", encoding="utf-8") as f:
    credentials = json.load(f)

USERNAME = credentials["USERNAME"]
PASSWORD = credentials["PASSWORD"]


def login(driver):
    driver.get(url=LOGIN_URL)
    time.sleep(3)

    username_input = driver.find_element(By.NAME, "_ctl0:page_content:txt_UserName")
    password_input = driver.find_element(By.NAME, "_ctl0:page_content:txt_Password")

    username_input.send_keys(USERNAME)
    password_input.send_keys(PASSWORD)

    password_input.send_keys(Keys.RETURN)

    time.sleep(3)

def scrape_unit(driver, unit_number):
    unit_url = f"{WORDS_URL}/?u={unit_number}"
    driver.get(unit_url)
    word_cards = driver.find_elements(By.CSS_SELECTOR, "[class*='word-card']")
    
    unit_dict = {}

    for card in word_cards:
        driver.execute_script("arguments[0].scrollIntoView(true);", card)
        
        card.click()
        
        meaning = WebDriverWait(driver, 10).until(
            EC.visibility_of(card.find_element(By.CLASS_NAME, "meaning"))
        ).text.strip()
        
        word = card.find_element(By.CLASS_NAME, "word").text.strip()
        
        unit_dict[word] = meaning

    with open(f"data/unit_{unit_number}.json", "w", encoding="utf-8") as f:
        json.dump(unit_dict, f, ensure_ascii=False, indent=2)

def scrape_all_units(driver):
    for i in range(1, 11):
        scrape_unit(driver, i)

if (__name__ == "__main__"):
    driver = webdriver.Chrome()
    login(driver)
    scrape_all_units(driver)
    driver.close()
    
    
    

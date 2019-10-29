from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import time
from pynput.keyboard import Key, Controller
from selenium.webdriver.common import keys
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from google.cloud import storage
from datetime import datetime
from PIL import Image
import os

def upload_to_bucket(blob_name, path_to_file, bucket_name):
    storage_client = storage.Client.from_service_account_json('happy.json')
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(blob_name)
    blob.upload_from_filename(path_to_file)
    return blob.public_url

def resize(img_dir, img_name, ratio = 2):
    img_path = os.path.join(img_dir, img_name)
    with Image.open(img_path) as img:
        basewidth, baseheight = img.size
        img = img.resize((basewidth*ratio,baseheight*ratio), Image.ANTIALIAS)
        to_img_path = os.path.join(img_dir, 'big-'+img_name)
        img.save(to_img_path)

options = Options()
url = "big-blue-button.url.com"
websiteAddress = "http://"+url

prefs = {
    "profile.default_content_setting_values.plugins": 1,
    "profile.content_settings.plugin_whitelist.adobe-flash-player": 1,
    "profile.content_settings.exceptions.plugins.*,*.per_resource.adobe-flash-player": 1,
    "PluginsAllowedForUrls": websiteAddress
}



options.add_experimental_option("prefs",prefs)
browser = webdriver.Chrome(options=options)


browser.get("chrome://settings/content/siteDetails?site=http://"+url)
time.sleep(2)
actions = ActionChains(browser)

actions = actions.send_keys(Keys.TAB * 21)
actions = actions.send_keys(Keys.SPACE)
actions = actions.send_keys("a")
actions = actions.send_keys(Keys.ENTER)

actions.perform()
time.sleep(2)
browser.implicitly_wait(30)
browser.get(websiteAddress)
time.sleep(2)
username = browser.find_element_by_id('username')
# do the same for password if needed
username.click()
username.send_keys("Kris", Keys.ENTER)
time.sleep(1)


keyboard = Controller()
keyboard.press(Key.alt)
keyboard.press('a')
keyboard.release('a')
keyboard.release(Key.alt)

time.sleep(15)
body = browser.find_element_by_id('content')
body.click()
actions = ActionChains(browser)
actions = actions.send_keys(Keys.TAB * 5)
actions = actions.send_keys(Keys.ENTER)

actions.perform()
time.sleep(5)
browser.save_screenshot('screen_shot.png')
dt = datetime.today().strftime('%Y-%m-%d-%H-%M-%S')

resize('.','screen_shot.png', 2)
upload_to_bucket(dt+"-screenshot.png","big-screen_shot.png", "softwaremill")
browser.close()

from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import time
from selenium import webdriver
from pynput.keyboard import Key, Controller
from selenium.webdriver.common import keys
from selenium.webdriver.chrome.options import Options

from PIL import Image
import os

# websiteAddress = "http://www.foxcrow.com"
websiteAddress = "http://www.softwaremill.com"
profile = webdriver.FirefoxProfile()
profile.set_preference('plugin.state.flash', 2)
# profile.set_preference('general.useragent.override', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:59.0) Gecko/20100101 Firefox/59.0')
profile.set_preference('dom.ipc.plugins.enabled.libflashplayer.so','true')
browser = webdriver.Firefox(profile)
browser.implicitly_wait(30)
browser.get(websiteAddress)
# run_flush = browser.find_element_by_id('app')
# run_flush.click()
time.sleep(5)
# keyboard = Controller()
# keyboard.press(Key.alt)
# keyboard.press('a')
# keyboard.release('a')
# keyboard.release(Key.alt)
time.sleep(5)
browser.save_screenshot('screen_shot2.png')


def resize(img_dir, img_name, ratio = 2):
    img_path = os.path.join(img_dir, img_name)
    with Image.open(img_path) as img:
        basewidth, baseheight = img.size
        img = img.resize((basewidth*ratio,baseheight*ratio), Image.ANTIALIAS)
        to_img_path = os.path.join(img_dir, 'big-'+img_name)
        img.save(to_img_path)

resize('.','screen_shot2.png', 2)
browser.close()
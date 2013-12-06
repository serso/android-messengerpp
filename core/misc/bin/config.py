from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
from com.android.monkeyrunner.easy import EasyMonkeyDevice
from com.android.monkeyrunner.easy import By
import time


def get_package():
    return 'org.solovyev.android.messenger'


def get_start_activity():
    return '.StartActivity'


def get_actions(device_name):
    tests = {'startMainActivity': lambda device: start_activity(device, '.StartActivity'),
             'startPreferencesActivity': lambda device: start_activity(device, '.preferences.PreferencesActivity'),
             'startAccountsActivity': lambda device: start_activity(device, '.accounts.AccountsActivity'),
             'startAboutActivity': lambda device: start_activity(device, '.about.AboutActivity'),
             'openMenu': open_menu
    }

    for name, action in get_device_actions().get(device_name, {}).iteritems():
        tests[name] = action

    return tests


def get_device_actions():
    return {'Nexus-4': {
        'scrollContacts': scroll_contacts,
        'openContact': open_contact,
        'openContactAndReturn': open_contact_and_return
    }}


# Util methods

def get_display_width(device):
    return int(device.getProperty('display.width'))


def get_display_height(device):
    return int(device.getProperty('display.height'))


# Actions

def open_menu(device):
    start_activity(device, '.StartActivity')
    device.press('KEYCODE_MENU', MonkeyDevice.DOWN_AND_UP)


def scroll_contacts(device, should_start_activity=True):
    if should_start_activity:
        start_activity(device, '.StartActivity')
        time.sleep(2)

    width = get_display_width(device)
    height = get_display_height(device)
    device.drag((width / 2, height / 2 + height / 3), (width / 2, height / 2), 0.2, 1)


def open_contact(device):
    scroll_contacts(device)
    time.sleep(2)
    x = get_display_width(device) / 2
    y = get_display_height(device) / 2
    device.touch(x, y, MonkeyDevice.DOWN_AND_UP)


def open_contact_and_return(device):
    open_contact(device)
    time.sleep(3)
    device.press('KEYCODE_BACK', MonkeyDevice.DOWN_AND_UP)


def filter_contacts(device):
    easy_device = EasyMonkeyDevice(device)
    start_activity(device, '.StartActivity')

    easy_device.touch(By.id('id/mpp_menu_toggle_filter_box'), MonkeyDevice.DOWN_AND_UP)


def start_activity(device, activity):
    run_component = get_package() + '/' + get_package() + activity
    device.startActivity(component=run_component)

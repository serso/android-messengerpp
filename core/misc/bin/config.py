from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
from com.android.monkeyrunner.easy import EasyMonkeyDevice
from com.android.monkeyrunner.easy import By
import time


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
        'verticalScrollInMainActivity': vertical_scroll
    }}


def open_menu(device):
    start_activity(device, '.StartActivity')
    device.press('KEYCODE_MENU', "DOWN_AND_UP")
    time.sleep(3)


def vertical_scroll(device):
    start_activity(device, '.StartActivity')

    width = int(device.getProperty('display.width'))
    height = int(device.getProperty('display.height'))
    device.drag((width / 2, height / 2 + height / 3), (width / 2, height / 2), 0.2, 1)

    time.sleep(1)


def open_contacts_filter(device):
    easy_device = EasyMonkeyDevice(device)
    start_activity(device, '.StartActivity')

    easy_device.touch(By.id('id/mpp_menu_toggle_filter_box'), MonkeyDevice.DOWN_AND_UP)
    time.sleep(1)


def start_activity(device, activity):
    run_component = get_package() + '/' + get_package() + activity
    device.startActivity(component=run_component)
    time.sleep(5)


def get_package():
    return 'org.solovyev.android.messenger'
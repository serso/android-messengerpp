from subprocess import check_output, Popen, call
from time import sleep

from android.idtype import IdType
from appiumtest import AppiumTest, get_env_variable


DEVICE_WAIT_TIME = 10

DEVICE_ANDROID = "android"
DEVICE_SELENDROID = "selendroid"

# last available port
EMULATOR_PORT = "5584"


class AndroidTest(AppiumTest):
    def prepare_device(self):
        super(AndroidTest, self).prepare_device()

        self.start_or_reuse_avd()

    def get_avd_name(self):
        return "Default"

    def start_or_reuse_avd(self):
        serial = find_running_device()

        if not serial:
            # we don't want to stop the emulator after test as we want to reuse it
            Popen([get_emulator_path(), "-avd", self.get_avd_name(), "-port", EMULATOR_PORT])
            serial = make_emulator_serial()

        self.wait_avd(serial)

    def wait_avd(self, serial):
        waiting_process = Popen([get_adb_path(), "-s", serial, "wait-for-device"])

        i = 0
        while i < DEVICE_WAIT_TIME:
            i += 1
            if not waiting_process.poll():
                break
            sleep(1)

        if waiting_process.poll():
            waiting_process.terminate()
            raise Exception("Too long wait time for AVD with serial: " + serial)

    def check_capabilities(self, capabilities):
        super(AndroidTest, self).check_capabilities(capabilities)

        device = capabilities['device']
        if device != DEVICE_ANDROID and device != DEVICE_SELENDROID:
            raise ValueError(
                DEVICE_ANDROID + " and " + DEVICE_SELENDROID + " are only supported devices, got: " + device)

        if not capabilities['app-package']:
            raise ValueError("Package name must be set")
        else:
            self.package_name = capabilities['app-package']

        if not capabilities['app-activity']:
            raise ValueError("Start activity must be set")

        if not capabilities['app']:
            raise ValueError("APK path must be set")

    #
    # API METHODS
    #

    def open_menu(self):
        self.driver.execute_script("mobile: keyevent", {"keycode": 82})
        sleep(0.2)

    def go_back(self):
        self.driver.execute_script("mobile: keyevent", {"keycode": 4})
        sleep(0.2)

    def r_id(self, resource_id, id_type=IdType.PACKAGE):
        """Returns fully qualified Android resource id name"""
        if not IdType.is_valid(id_type):
            raise ValueError("Invalid ID type: " + id_type)

        if id_type == IdType.PACKAGE:
            return self.package_name + ":id/" + resource_id
        elif id_type == IdType.ANDROID:
            return "android:id/" + resource_id
        else:
            return resource_id

    def find_element_by_id(self, resource_id, id_type=IdType.PACKAGE):
        """
        Method tries to find element with specified resource_id. ID might be relative (belongs to current app package),
        android and fully qualified.

        :type resource_id: str
        :type id_type: str
        :rtype: selenium.webdriver.remote.webelement.WebElement
        """
        return self.driver.find_element_by_id(self.r_id(resource_id, id_type))

    def find_elements_by_id(self, resource_id, id_type=IdType.PACKAGE):
        """
        Method tries to find elements with specified resource_id. ID might be relative (belongs to current app package),
        android and fully qualified.

        :type resource_id: str
        :type id_type: str
        :rtype: list
        """
        return self.driver.find_elements_by_id(self.r_id(resource_id, id_type))

    def find_element_by_name(self, name):
        """
        Method tries to find element with specified name.

        :type name: str
        :rtype: selenium.webdriver.remote.webelement.WebElement
        """
        return self.driver.find_element_by_name(name)


def uninstall_app(package_name):
    if not package_name:
        raise ValueError("Package name must be set")

    for device in find_running_devices():
        call([get_adb_path(), "-s", device, "uninstall", package_name])


def get_android_home():
    return get_env_variable("ANDROID_HOME", "Android SDK folder", "/opt/android/sdk")


def get_adb_path():
    return get_android_home() + "/platform-tools/adb"


def get_emulator_path():
    return get_android_home() + "/tools/emulator"


def make_emulator_serial():
    return "emulator-" + EMULATOR_PORT


def find_running_devices():
    devices = check_output([get_adb_path(), "devices"]).splitlines()

    devices = devices[1:len(devices) - 1]
    for index, device in enumerate(devices):
        devices[index] = device.split("\t")[0]

    return devices


def find_running_device():
    devices = find_running_devices()

    # NOTE: we can find our device by port but we still can't force Appium to use it (Appium will use the first
    # device from the list) => just continue
    if len(devices) > 0:
        return devices[0]

    return None
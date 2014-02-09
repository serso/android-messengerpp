from subprocess import check_output, Popen
from time import sleep

from appiumtest import AppiumTest, get_env_variable
from idtype import IdType


DEVICE_WAIT_TIME = 10

DEVICE_ANDROID = "Android"
DEVICE_SELENDROID = "selendroid"

# last available port
EMULATOR_PORT = "5584"


class AndroidTest(AppiumTest):
    def prepare_device(self):
        super(AndroidTest, self).prepare_device()

        self.start_or_reuse_avd()

    def get_android_home(self):
        return get_env_variable("ANDROID_HOME", "Android SDK folder", "/opt/android/sdk")

    def get_adb_path(self):
        return self.get_android_home() + "/platform-tools/adb"

    def get_emulator_path(self):
        return self.get_android_home() + "/tools/emulator"

    def make_emulator_serial(self):
        return "emulator-" + EMULATOR_PORT

    def find_running_emulator(self):
        running_devices = check_output([self.get_adb_path(), "devices"]).splitlines()
        running_devices = running_devices[1:len(running_devices) - 1]

        # NOTE: we can find our device by port but we still can't force Appium to use it (Appium will use the first
        # device from the list) => just continue
        if len(running_devices) > 0:
            return running_devices[0].split("\t")[0]

        return None

    def get_avd_name(self):
        return "Default"

    def start_or_reuse_avd(self):
        serial = self.find_running_emulator()

        if not serial:
            # we don't want to stop the emulator after test as we want to reuse it
            Popen([self.get_emulator_path(), "-avd", self.get_avd_name(), "-port", EMULATOR_PORT])
            serial = self.make_emulator_serial()

        self.wait_avd(serial)

    def wait_avd(self, serial):
        waiting_process = Popen([self.get_adb_path(), "-s", serial, "wait-for-device"])

        i = 0
        while i < DEVICE_WAIT_TIME:
            i += 1
            if not waiting_process.poll():
                break
            sleep(1)

        if waiting_process.poll():
            waiting_process.terminate()
            raise Exception("Too long wait time for AVD with serial: " + serial)

    def check_desired_capabilities(self, capabilities):
        super(AndroidTest, self).check_desired_capabilities(capabilities)

        device = capabilities['device']
        if device is None:
            raise ValueError("Device must be set")
        elif device != DEVICE_ANDROID and device != DEVICE_SELENDROID:
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
        return self.driver.find_element_by_id(self.r_id(resource_id, id_type))

    def find_elements_by_id(self, resource_id, id_type=IdType.PACKAGE):
        return self.driver.find_elements_by_id(self.r_id(resource_id, id_type))


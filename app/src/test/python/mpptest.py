from time import sleep

from selenium.common.exceptions import NoSuchElementException

from android.androidtest import AndroidTest, DEVICE_ANDROID
from appiumtest import run_tests


PACKAGE_NAME = 'org.solovyev.android.messenger'


class MppTest(AndroidTest):
    should_skip_wizard = True

    def on_setup_done(self):
        super(MppTest, self).on_setup_done()
        if self.should_skip_wizard:
            self.skip_wizard()

    def create_capabilities(self):
        return {'device': DEVICE_ANDROID,
                'browserName': '',
                'version': '4.2',
                'app': '/home/serso/projects/java/android/messengerpp/app/target/android-messenger-app.apk',
                'app-package': PACKAGE_NAME,
                'app-wait-activity': '.wizard.WizardActivity',
                'device-ready-timeout': 5,
                'app-activity': '.StartActivity'}

    def skip_wizard(self):
        counter = 0
        next_button = self.find_element_by_id("acl_wizard_next_button")
        while next_button is not None:
            counter += 1
            next_button.click()
            try:
                next_button = self.find_element_by_id("acl_wizard_next_button")
            except NoSuchElementException:
                next_button = None

    def add_test_account(self):
        self.open_realms()
        test_realm = self.find_element_by_name("Test")
        test_realm.click()
        sleep(0.5)
        save_button = self.find_element_by_id("mpp_save_button")
        save_button.click()
        sleep(0.5)

    def open_realms(self):
        self.open_accounts()
        realms_menu_item = self.find_element_by_id("mpp_menu_add_account")
        realms_menu_item.click()

    def open_accounts(self):
        self.open_menu()
        accounts_menu_item = self.find_element_by_name('Accounts')
        accounts_menu_item.click()


if __name__ == '__main__':
    run_tests("tests", "*test.py")
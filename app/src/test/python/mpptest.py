from time import sleep

import sys
sys.path.append('appium')

from selenium.common.exceptions import NoSuchElementException

from android.androidtest import AndroidTest, DEVICE_ANDROID, uninstall_app
from appiumtest import run_tests, get_env_variable


PACKAGE_NAME = 'org.solovyev.android.messenger.dev'
RESOURCE_PACKAGE_NAME = 'org.solovyev.android.messenger'


class MppTest(AndroidTest):
    should_skip_wizard = True

    def on_setup_done(self):
        super(MppTest, self).on_setup_done()
        if self.should_skip_wizard:
            self.skip_wizard()

    def create_capabilities(self):
        mpp_home = get_env_variable("MPP_HOME", "Messenger++ project root",
                                    "/home/serso/projects/java/android/messengerpp")

        return {'device': DEVICE_ANDROID,
                'browserName': '',
                'version': '4.2',
                'app': mpp_home + '/app/target/android-messenger-app.apk',
                'app-package': PACKAGE_NAME,
                'app-resource-package': RESOURCE_PACKAGE_NAME,
                'app-wait-activity': RESOURCE_PACKAGE_NAME + '.wizard.WizardActivity',
                'device-ready-timeout': 5,
                'app-activity': RESOURCE_PACKAGE_NAME + '.StartActivity'}

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

    def open_contacts(self):
        self.go_home()
        self.open_tab(0)

    def open_chats(self):
        self.go_home()
        self.open_tab(1)

    def open_realms(self):
        self.open_accounts()
        realms_menu_item = self.find_element_by_id("mpp_menu_add_account")
        realms_menu_item.click()

    def open_accounts(self):
        self.go_home()
        self.open_menu()
        accounts_menu_item = self.find_element_by_name('Accounts')
        accounts_menu_item.click()

    def find_contact(self, contact):
        self.open_contacts()
        contacts = self.find_elements_by_id("mpp_li_contact_name_textview")
        contacts_found = [c for c in contacts if c.text == contact or c.text.startswith(contact + ' (')]
        if len(contacts_found) > 1:
            raise Exception("More than one count found: " + contact)
        return contacts_found[0]

    def open_contact(self, contact):
        self.find_contact(contact).click()

    def send_message(self, to, message):
        self.open_contact(to)

        message_edittext = self.find_element_by_id('mpp_message_bubble_body_edittext')
        message_edittext.send_keys(message)

        send_button = self.find_element_by_id('mpp_message_bubble_send_button')
        send_button.click()


if __name__ == '__main__':
    uninstall_app(PACKAGE_NAME)
    run_tests("tests", "*test.py")
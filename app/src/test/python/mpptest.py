import unittest

from selenium.common.exceptions import NoSuchElementException

from androidtest import AndroidTest, DEVICE_ANDROID


class MppTest(AndroidTest):
    should_skip_wizard = True

    def on_setup_done(self):
        super(MppTest, self).on_setup_done()
        if self.should_skip_wizard:
            self.skip_wizard()

    def create_desired_capabilities(self):
        return {'device': DEVICE_ANDROID,
                'browserName': '',
                'version': '4.2',
                'app': '/home/serso/projects/java/android/messengerpp/app/target/android-messenger-app.apk',
                'app-package': 'org.solovyev.android.messenger',
                'app-wait-activity': '.wizard.WizardActivity',
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


if __name__ == '__main__':
    suite = unittest.defaultTestLoader.discover(start_dir="tests", pattern="*test.py")
    runner = unittest.TextTestRunner()
    runner.run(suite)
import os
import unittest
from subprocess import Popen, PIPE, STDOUT
from time import sleep

from selenium import webdriver


class AppiumTest(unittest.TestCase):
    driver = None

    nodejs_process = None

    #
    # SETUP/TEARDOWN
    #

    def setUp(self):
        super(AppiumTest, self).setUp()

        try:
            self.start_nodejs()

            self.prepare_device()

            capabilities = self.create_desired_capabilities()
            self.check_desired_capabilities(capabilities)
            self.driver = webdriver.Remote('http://localhost:4723/wd/hub', capabilities)

            self.on_setup_done()
        except:
            self.stop_nodejs()
            raise

    def start_nodejs(self):
        """Tries to start Node JS web server with Appium module"""
        appium_home = get_env_variable("APPIUM_HOME", "Appium checkout folder", "~/projects/appium")
        nodejs_bin = get_env_variable('NODEJS_BIN', "Node JS executable", "/usr/bin/nodejs")

        working_directory = os.getcwd()
        os.chdir(appium_home)
        self.nodejs_process = Popen([nodejs_bin, "."], stdout=PIPE, stderr=STDOUT)
        os.chdir(working_directory)
        self.wait_nodejs()

    def wait_nodejs(self):
        """Waits Node JS server to start"""
        i = 0
        while True:
            nodejs_stdout = self.nodejs_process.stdout.readline()

            if "Appium running already" in nodejs_stdout:
                # let's try to reuse existing Node JS server
                break

            if not "socket.io started" in nodejs_stdout:
                if i == 10:
                    raise Exception("Unable to start nodejs")
                else:
                    i += 1
                    sleep(0.5)
            else:
                break

        sleep(0.5)

    def prepare_device(self):
        pass

    def on_setup_done(self):
        pass

    def create_desired_capabilities(self):
        raise NotImplementedError("create_desired_capabilities must be implemented")

    def check_desired_capabilities(self, capabilities):
        if len(capabilities) == 0:
            raise ValueError("Capabilities must be set properly")

        device = capabilities['device']
        if device is None:
            raise ValueError("Device must be set")

    def tearDown(self):
        super(AppiumTest, self).tearDown()

        if self.driver is not None:
            self.driver.quit()
            self.driver = None

        self.stop_nodejs()

    def stop_nodejs(self):
        """Stops Node JS if server was started"""
        if self.nodejs_process is not None:
            self.nodejs_process.terminate()
            self.nodejs_process = None


def new_no_env_variable_exception(name, description=None, example=None):
    message = name + " environment variable must be set"
    if description:
        message += " and should point to " + description

    message += ". "

    if example:
        message = "For example, '" + example + "'"

    return ValueError(message)


def get_env_variable(name, description=None, example=None):
    appium_home = os.environ.get(name)
    if not appium_home:
        raise new_no_env_variable_exception(name, description, example)
    return appium_home


def run_tests(directory=None, pattern="test*.py"):
    """Method runs all test files located in the directory and its subdirectories"""
    if not directory:
        raise ValueError("Tests directory must be set")

    suite = unittest.defaultTestLoader.discover(start_dir=directory, pattern=pattern)
    runner = unittest.TextTestRunner()
    runner.run(suite)
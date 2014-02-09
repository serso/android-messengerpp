import argparse


class AppiumParser:
    parser = None

    package = None

    start_activity = None

    map = {}

    def __init__(self):
        parser = argparse.ArgumentParser(description='Script runs Appium unit tests.')

        parser.add_argument('-t', '--tests', required=False, default="tests",
                            help="Test cases separated by ',' or directory. If directory is provided script tries "
                                 "to find all test cases in it and it's subdirectories according to the TESTS_PATTERN. "
                                 "Default: tests")

        parser.add_argument('-tp', '--tests-pattern', required=False, default="test*.py",
                            help='Test case file pattern. '
                                 'Pattern is used only when a directory provided as a location of test cases. '
                                 'Default: test*.py')

        device = parser.add_argument('-d', '--device', required=False, default="android",
                                     help="Appium 'device' desired capability. Type of device to be used: iphone, "
                                          "ipad, selendroid, android, mock_ios. "
                                          "Default: android")
        self.map[device.dest] = 'device'

        version = parser.add_argument('-v', '--version', required=False, default="4.2",
                                      help="Appium 'version' desired capability. For Android: 4.2/4.3, "
                                           "for iOS 6.0/6.1/7.0. "
                                           "Default: 4.2")
        self.map[version.dest] = 'version'

        app = parser.add_argument('-a', '--app', required=True,
                                  help="Appium 'app' desired capability. Location of the app's package")
        self.map[app.dest] = 'app'

        self.package = parser.add_argument('-p', '--package', required=False,
                                           help="Appium 'app-package' desired capability. "
                                                "Android only. Package name of the app")
        self.map[self.package.dest] = 'app-package'

        self.start_activity = parser.add_argument('-sa', '--start-activity', required=False,
                                                  help="Appium 'app-activity' desired capability. "
                                                       "Android only. Activity which should "
                                                       "be started")
        self.map[self.start_activity.dest] = 'app-activity'

        wait_activity = parser.add_argument('-wa', '--wait-activity', required=False,
                                            help="Appium 'app-wait-activity' desired capability. "
                                                 "Android only. Activity which should "
                                                 "be waited after app was started. "
                                                 "Default: equals to START_ACTIVITY")
        self.map[wait_activity.dest] = 'app-wait-activity'

        wait_timeout = parser.add_argument('-wt', '--wait-timeout', required=False, default=5, type=int,
                                           help="Appium 'device-ready-timeout' desired capability. "
                                                "Android only. Maximum device wait time in seconds. "
                                                "Default: 5")
        self.map[wait_timeout.dest] = 'device-ready-timeout'

        self.parser = parser

    def parse(self):
        args = vars(self.parser.parse_args())

        device = args['device']
        if str(device).lower() == 'android':
            self.package.required = True
            self.start_activity.required = True
            args = vars(self.parser.parse_args())

        return args

    def to_appium_args(self, args):
        appium_args = {}
        for k, v in self.map.iteritems():
            appium_args[v] = args[k]

        return appium_args
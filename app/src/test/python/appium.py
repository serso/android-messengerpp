#!/usr/bin/env python
import os
from pprint import pprint

from appiumparser import AppiumParser
from appiumtest import run_tests, AppiumTest


if __name__ == '__main__':
    p = AppiumParser()
    args = p.parse()
    appium_args = p.to_appium_args(args)

    print ("Running Appium with the following desired capabilities:")
    pprint(appium_args)

    device = str(appium_args['device']).lower()

    test = None
    if device in ['android', 'selendroid']:
        pass
        # test = CustomAndroidTest()
    elif device in ['iphone', 'ipad', 'mock_ios']:
        pass
        # test = CustomIosTest()
    else:
        raise ValueError("Device " + device + " is not supported")

    tests = args['tests']
    if os.path.isdir(tests):
        AppiumTest.overridden_capabilities = appium_args
        run_tests(tests, args['tests_pattern'])
    else:
        pass

        # test.capabilities = args
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
import time

import sys
import subprocess

import config

out_folder = sys.argv[1]
in_folder = sys.argv[2]
failed_folder = sys.argv[3]
device_name = sys.argv[4]

ACCEPTANCE = 0.99


def check_screenshot(filename_postfix):
    new_filename = out_folder + '/' + device_name + '_' + filename_postfix + '.png'
    new_screenshot = MonkeyRunner.loadImageFromFile(new_filename)

    old_filename = in_folder + '/' + device_name + '_' + filename_postfix + '.png'
    old_screenshot = MonkeyRunner.loadImageFromFile(old_filename)

    print("Comparing: " + old_filename + " and " + new_filename)
    if not new_screenshot.sameAs(old_screenshot, ACCEPTANCE):
        print("Comparison failed, creating visual comparison...")
        failed_filename = failed_folder + '/' + device_name + '_' + filename_postfix + '.png'
        subprocess.call(["/usr/bin/compare", old_filename, new_filename, failed_filename])
    return


for name, action in config.get_actions(device_name).iteritems():
    check_screenshot(name)

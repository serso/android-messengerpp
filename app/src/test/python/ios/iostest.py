from appiumtest import AppiumTest


class IosTest(AppiumTest):
    def prepare_device(self):
        super(IosTest, self).prepare_device()

    def check_capabilities(self, capabilities):
        super(IosTest, self).check_capabilities(capabilities)


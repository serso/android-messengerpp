import unittest

from mpptest import MppTest


class ContactsTest(MppTest):
    def test_contacts_should_be_sorted_correctly(self):
        self.add_test_account()
        self.go_back()
        self.go_back()

        raise NotImplementedError("Fix")


if __name__ == '__main__':
    unittest.main()
import unittest

from mpptest import MppTest


class AccountsTest(MppTest):
    def test_should_open_menu(self):
        super(AccountsTest, self).open_menu()


if __name__ == '__main__':
    unittest.main()
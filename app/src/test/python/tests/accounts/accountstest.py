import unittest

from android.idtype import IdType
from mpptest import MppTest


class AccountsTest(MppTest):
    def test_should_open_accounts_screen(self):
        self.open_accounts()
        title = self.find_element_by_id("action_bar_title", IdType.ANDROID)
        self.assertEqual(title.text, "Accounts")

    def test_should_add_test_account(self):
        self.add_test_account()
        self.go_back()

        accounts = self.find_element_by_id("list", IdType.ANDROID)
        account = accounts.find_element_by_id(self.r_id("mpp_li_account_name_textview"))
        self.assertEqual(account.text, "Test")


if __name__ == '__main__':
    unittest.main()
from time import sleep
import unittest

from mpptest import MppTest


class ContactsTest(MppTest):
    def test_contacts_should_be_sorted_correctly(self):
        self.add_test_account()
        sleep(1)
        self.open_contacts()

        contacts = self.find_elements_by_id("mpp_li_contact_name_textview")
        first_contact = contacts[0].text

        self.open_chats()
        chats = self.find_elements_by_id("mpp_li_chat_title_textview")
        first_chat = chats[0].text

        self.assertEqual(first_contact, first_chat)


if __name__ == '__main__':
    unittest.main()
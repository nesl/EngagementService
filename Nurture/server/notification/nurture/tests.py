import datetime

from django.test import TestCase

from nurture.models import AppUser, FileLog


class FileLogTestCase(TestCase):

    def test_get_path(self):

        user = AppUser.objects.create(code="36279")
        file_log = FileLog.objects.create(
                user=user,
                type='motion',
                uploaded_time=datetime.datetime(2018, 6, 13, 18, 3, 10),
        )

        print(file_log.get_path())
        self.assertTrue(file_log.get_path().endswith("/36279/motion/20180613-180310.txt"))

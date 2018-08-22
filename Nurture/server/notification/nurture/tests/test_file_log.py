import datetime

from django.test import TestCase

from nurture.models import AppUser, FileLog


class FileLogTestCase(TestCase):

    def test_get_path(self):

        user = AppUser.objects.create(
                code='36279',
                name='test user',
                status=AppUser.STATUS_ACTIVE,
                created_time=datetime.datetime(2018, 6, 13, 17, 55, 42),
                learning_agent=AppUser.LEARNING_AGENT_DEBUG,
        )
        file_log = FileLog.objects.create(
                user=user,
                type='motion',
                uploaded_time=datetime.datetime(2018, 6, 13, 18, 3, 10),
                filename='20180613-180310.txt',
        )

        self.assertTrue(file_log.get_path().endswith("/36279/motion/20180613-180310.txt"))

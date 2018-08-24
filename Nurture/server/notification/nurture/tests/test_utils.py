import datetime
import pytz

from django.test import TestCase

from nurture import utils
from nurture.models import ActionLog


class UtilsTestCase(TestCase):

    def test_is_file_extended(self):
        with open('/tmp/test_nt_blank', 'w') as fo:
            pass
        with open('/tmp/test_nt_pine', 'w') as fo:
            fo.write('pine')
        with open('/tmp/test_nt_pineapple', 'w') as fo:
            fo.write('pineapple')
        with open('/tmp/test_nt_banana', 'w') as fo:
            fo.write('banana')

        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_blank'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_pine'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_pineapple'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_banana'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_blank'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_pine'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_pineapple'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_banana'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_blank'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_pine'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_pineapple'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_banana'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_blank'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_pine'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_pineapple'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_banana'))

    def test_time_of_day_2_hms(self):
        self.assertEqual(utils.time_of_day_2_hms(0.000000), (0, 0, 0))
        self.assertEqual(utils.time_of_day_2_hms(0.000012), (0, 0, 1))
        self.assertEqual(utils.time_of_day_2_hms(0.505498), (12, 7, 55))
        self.assertEqual(utils.time_of_day_2_hms(0.999989), (23, 59, 59))

    def test_weekday_difference(self):
        self.assertEqual(utils.weekday_difference(3, 4), 1)
        self.assertEqual(utils.weekday_difference(3, 2), -1)
        self.assertEqual(utils.weekday_difference(3, 3), 0)
        self.assertEqual(utils.weekday_difference(3, 6), None)
        self.assertEqual(utils.weekday_difference(0, 1), 1)
        self.assertEqual(utils.weekday_difference(0, 6), -1)
        self.assertEqual(utils.weekday_difference(6, 0), 1)
        self.assertEqual(utils.weekday_difference(6, 5), -1)
        self.assertEqual(utils.weekday_difference(6, 4), None)

    def test_calibrate_sensor_time(self):
        # case 1, time difference within one minute
        calibrated = utils.calibrate_sensor_time(
                action_log_datetime=datetime.datetime(2018, 8, 22, 20, 21, 10),  # Wednesday
                sensor_time_of_day=0.848079,   # 20:21:14
                sensor_time_of_week=0.549726,   # Wednesday
        )
        self.assertEqual(calibrated.year, 2018)
        self.assertEqual(calibrated.month, 8)
        self.assertEqual(calibrated.day, 22)
        self.assertEqual(calibrated.hour, 20)
        self.assertEqual(calibrated.minute, 21)
        self.assertEqual(calibrated.second, 14)

        # case 2, app clock is slightly ahead of server clock and across different days
        calibrated = utils.calibrate_sensor_time(
                action_log_datetime=datetime.datetime(2018, 8, 22, 23, 57, 2),  # Wednesday
                sensor_time_of_day=0.000776,   # 00:01:07
                sensor_time_of_week=0.571540,   # Thursday
        )
        self.assertEqual(calibrated.year, 2018)
        self.assertEqual(calibrated.month, 8)
        self.assertEqual(calibrated.day, 23)
        self.assertEqual(calibrated.hour, 0)
        self.assertEqual(calibrated.minute, 1)
        self.assertEqual(calibrated.second, 7)
        
        # case 3, app clock is slightly behind of server clock and across different days
        calibrated = utils.calibrate_sensor_time(
                action_log_datetime=datetime.datetime(2018, 8, 20, 0, 3, 44),  # Monday
                sensor_time_of_day=0.998565,   # 23:57:56
                sensor_time_of_week=0.142653,   # Sunday
        )
        self.assertEqual(calibrated.year, 2018)
        self.assertEqual(calibrated.month, 8)
        self.assertEqual(calibrated.day, 19)
        self.assertEqual(calibrated.hour, 23)
        self.assertEqual(calibrated.minute, 57)
        self.assertEqual(calibrated.second, 56)
        
        # case 4, in different time zones
        calibrated = utils.calibrate_sensor_time(
                action_log_datetime=datetime.datetime(2018, 8, 18, 17, 21, 53),  # Saturday
                sensor_time_of_day=0.244480,   # 05:52:03
                sensor_time_of_week=0.034926,   # Sunday
        )
        self.assertEqual(calibrated.year, 2018)
        self.assertEqual(calibrated.month, 8)
        self.assertEqual(calibrated.day, 19)
        self.assertEqual(calibrated.hour, 5)
        self.assertEqual(calibrated.minute, 52)
        self.assertEqual(calibrated.second, 3)
        
        # case 5, error handling
        calibrated = utils.calibrate_sensor_time(
                action_log_datetime=datetime.datetime(2018, 8, 21, 6, 14, 23),  # Tuesday
                sensor_time_of_day=0.269526,   # 06:28:07
                sensor_time_of_week=0.752790,   # Friday
                best_effort=True,
        )
        self.assertEqual(calibrated.year, 2018)
        self.assertEqual(calibrated.month, 8)
        self.assertEqual(calibrated.day, 21)
        self.assertEqual(calibrated.hour, 6)
        self.assertEqual(calibrated.minute, 28)
        self.assertEqual(calibrated.second, 7)

    def test_get_calibrated_sensor_time_in_action_log(self):
        # case 1, continue case
        action_log = ActionLog(
                query_time=datetime.datetime(2018, 8, 23, 23, 58, 17, 861334, tzinfo=pytz.utc),
                reward_state_message="[];[0.7026273148148148,0.6718039021164021,still,others,4.0,silent,on];[continue]",
        )
        
        first_calibrated = utils.get_first_calibrated_sensor_time_in_action_log(action_log)
        self.assertEqual(first_calibrated.year, 2018)
        self.assertEqual(first_calibrated.month, 8)
        self.assertEqual(first_calibrated.day, 23)
        self.assertEqual(first_calibrated.hour, 16)
        self.assertEqual(first_calibrated.minute, 51)
        self.assertEqual(first_calibrated.second, 46)
        
        recent_calibrated = utils.get_recent_calibrated_sensor_time_in_action_log(action_log)
        self.assertEqual(recent_calibrated.year, 2018)
        self.assertEqual(recent_calibrated.month, 8)
        self.assertEqual(recent_calibrated.day, 23)
        self.assertEqual(recent_calibrated.hour, 16)
        self.assertEqual(recent_calibrated.minute, 51)
        self.assertEqual(recent_calibrated.second, 46)
        
        # case 2, discontinue case
        action_log = ActionLog(
                query_time=datetime.datetime(2018, 8, 20, 15, 52, 57, 971577, tzinfo=pytz.utc),
                reward_state_message="[];[0.36530092592592595,0.19504298941798942,walking,others,648.0,normal,on];[discontinue];[0.3659953703703704,0.19514219576719577,walking,others,649.0,normal,on]",
        )
        
        first_calibrated = utils.get_first_calibrated_sensor_time_in_action_log(action_log)
        self.assertEqual(first_calibrated.year, 2018)
        self.assertEqual(first_calibrated.month, 8)
        self.assertEqual(first_calibrated.day, 20)
        self.assertEqual(first_calibrated.hour, 8)
        self.assertEqual(first_calibrated.minute, 46)
        self.assertEqual(first_calibrated.second, 2)
        
        recent_calibrated = utils.get_recent_calibrated_sensor_time_in_action_log(action_log)
        self.assertEqual(recent_calibrated.year, 2018)
        self.assertEqual(recent_calibrated.month, 8)
        self.assertEqual(recent_calibrated.day, 20)
        self.assertEqual(recent_calibrated.hour, 8)
        self.assertEqual(recent_calibrated.minute, 47)
        self.assertEqual(recent_calibrated.second, 1)

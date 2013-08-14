package net.cattaka.android.humitemp.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateConverterUtil {
	private GregorianCalendar calendar;
	private GregorianCalendar localCalendar;
	public DateConverterUtil() {
		this.calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		this.localCalendar = new GregorianCalendar();
	}
	
	/**
	 * YYYYMMDDHHMM形式のlong型に変換する。
	 * @param time
	 * @return
	 */
	public long calcLongToYmdhm(long time) {
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		long result = year  * 100000000L
			+ month * 1000000L
			+ day * 10000L
			+ hour * 100L
			+ min;
		return result;
	}

	public long calcLongToLocalYmdhm(long time) {
		localCalendar.setTimeInMillis(time);
		int year = localCalendar.get(Calendar.YEAR);
		int month = localCalendar.get(Calendar.MONTH) + 1;
		int day = localCalendar.get(Calendar.DATE);
		int hour = localCalendar.get(Calendar.HOUR_OF_DAY);
		int min = localCalendar.get(Calendar.MINUTE);
		long result = year  * 100000000L
			+ month * 1000000L
			+ day * 10000L
			+ hour * 100L
			+ min;
		return result;
	}

	public long calcYmdhmToLong(long ymdhm) {
		int year = (int)(ymdhm / 100000000L);
		int month = (int)((ymdhm % 100000000L) / 1000000L) - 1;
		int day = (int)((ymdhm % 1000000L) / 10000L);
		int hour = (int)((ymdhm % 10000L) / 100L);
		int min = (int)(ymdhm % 100L);
		calendar.clear();
		calendar.set(year, month, day, hour, min);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * 
	 * @param year
	 * @param month 0〜11
	 * @return
	 */
	public long calcYearMonthToLong(int year, int month) {
		calendar.clear();
		calendar.set(year, month, 1, 0, 0);
		return calendar.getTimeInMillis();
	}

	public long calcYmdhmToLocalYmdhm(long ymdhm) {
		return calcLongToLocalYmdhm(calcYmdhmToLong(ymdhm));
	}
	
	public static long calcHrYearMonthToRawYearMonth(long hrYearMonth) {
		long year = hrYearMonth / 100;
		long month = hrYearMonth % 100;
		return year * 12 + month - 1;
	}
}

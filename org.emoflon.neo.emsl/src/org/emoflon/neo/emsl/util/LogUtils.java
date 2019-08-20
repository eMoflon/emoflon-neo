package org.emoflon.neo.emsl.util;

import java.util.Arrays;
import java.util.IllegalFormatException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This utility class simplifies the printing of log messages.
 *
 * Its methods support format strings (see
 * {@link String#format(String, Object...)}.
 *
 * Additionally, a method for conveniently printing {@link Throwable}s is
 * provided (see {@link #error(Logger, Throwable)}.
 */
public final class LogUtils {
	/**
	 * Basic logging method that logs a message (given as format string +
	 * optional arguments) using the given {@link Level}
	 *
	 * The method is built to be fail-safe, i.e. no exceptions will be thrown if the
	 * given format string is invalid. Instead, an error message is printed to the
	 * logger. If the logger is <code>null</code>, nothing happens.
	 *
	 * @param logger       the logger to log to
	 * @param t            the exception to log
	 * @param formatString the format string of the message
	 * @param arguments    the (optional) arguments of the format string of the
	 *                     message
	 */
	public static void log(final Logger logger, final Level level, final String formatString,
			final Object... arguments) {
		if (logger == null)
			return;

		if (level == null || logger.getLevel() == null || logger.getLevel().isGreaterOrEqual(level)) {
			logger.log(level, formatStringFailsafe(formatString, arguments));
		}
	}

	/**
	 * This method logs (i) a message (given as format string + optional arguments)
	 * and (ii) the stacktrace of the given {@link Throwable} as
	 * {@link Level#ERROR}.
	 *
	 * @param logger       the logger to log to
	 * @param formatString the format string of the message
	 * @param arguments    the (optional) arguments of the format string of the
	 *                     message
	 */
	public static void error(final Logger logger, final String formatString, final Object... arguments) {
		log(logger, Level.ERROR, formatString, arguments);
	}

	/**
	 * This method logs (i) a message (given as format string + optional arguments)
	 * and (ii) the stacktrace of the given {@link Throwable} as
	 * {@link Level#ERROR}.
	 *
	 * @param logger       the logger to log to
	 * @param t            the exception to log
	 * @param formatString the format string of the message
	 * @param arguments    the (optional) arguments of the format string of the
	 *                     message
	 */
	public static void error(final Logger logger, final Throwable t, final String formatString,
			final Object... arguments) {
		log(logger, Level.ERROR, "%s Reason:\n%s", String.format(formatString, arguments),
				WorkspaceHelper.printStacktraceToString(t));
	}

	/**
	 * This method solely logs the stacktrace of the given {@link Throwable}
	 * as{@link Level#ERROR}.
	 *
	 * The purpose of this method is to avoid the infamous invocations of
	 * {@link Throwable#printStackTrace()}', which hides critical problems from
	 * end-users.
	 *
	 * @param logger the logger to log to
	 * @param t      the exception to log
	 */
	public static void error(final Logger logger, final Throwable t) {
		error(logger, WorkspaceHelper.printStacktraceToString(t));
	}

	/**
	 * This method logs (i) a message (given as format string + optional arguments)
	 * and (ii) the stacktrace of the given {@link Throwable} as {@link Level#WARN}.
	 *
	 * @param logger       the logger to log to
	 * @param formatString the format string of the message
	 * @param arguments    the (optional) arguments of the format string of the
	 *                     message
	 */
	public static void warn(final Logger logger, final String formatString, final Object... arguments) {
		log(logger, Level.WARN, formatString, arguments);
	}

	/**
	 * This method logs (i) a message (given as format string + optional arguments)
	 * and (ii) the stacktrace of the given {@link Throwable} as {@link Level#INFO}.
	 *
	 * @param logger       the logger to log to
	 * @param formatString the format string of the message
	 * @param arguments    the (optional) arguments of the format string of the
	 *                     message
	 */
	public static void info(final Logger logger, final String formatString, final Object... arguments) {
		log(logger, Level.INFO, formatString, arguments);
	}

	/**
	 * This method logs (i) a message (given as format string + optional arguments)
	 * and (ii) the stacktrace of the given {@link Throwable} as
	 * {@link Level#DEBUG}.
	 *
	 * @param logger       the logger to log to
	 * @param formatString the format string of the message
	 * @param arguments    the (optional) arguments of the format string of the
	 *                     message
	 */
	public static void debug(final Logger logger, final String formatString, final Object... arguments) {
		log(logger, Level.DEBUG, formatString, arguments);
	}

	/**
	 * Fail-safe formatting method that catches potential problems during formatting
	 * 
	 * @param formatString the
	 * @param arguments
	 * @return
	 */
	private static String formatStringFailsafe(final String formatString, final Object... arguments) {
		if (formatString == null)
			return String.format("ERROR: The given format string is null");

		String str;
		try {
			str = String.format(formatString, arguments);
		} catch (final IllegalFormatException e) {
			// Fall-back result in case something is wrong with the formatting string.
			str = String.format("ERROR: Malformed format string for error message: formatString='%s', parameters='%s'",
					formatString, Arrays.deepToString(arguments));
		}
		return str;
	}
}
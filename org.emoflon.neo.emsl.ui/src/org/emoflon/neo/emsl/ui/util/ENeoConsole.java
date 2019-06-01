package org.emoflon.neo.emsl.ui.util;

import java.util.Calendar;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ENeoConsole extends AppenderSkeleton {

	private final static String CONSOLE_NAME = "eMoflon::Neo";
	private static IWorkbenchPage activePage;

	private void printMessage(String message, int color) {
		try {
			MessageConsole console = findConsole(CONSOLE_NAME);
			MessageConsoleStream out = console.newMessageStream();
			out.setColor(Display.getCurrent().getSystemColor(color));
			out.println("[" //
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"//
					+ Calendar.getInstance().get(Calendar.MINUTE) + ":"//
					+ Calendar.getInstance().get(Calendar.SECOND) + "] "//
					+ message);
			out.close();
			revealConsole(console);
		} catch (NullPointerException e) {
			System.out.println("[" //
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"//
					+ Calendar.getInstance().get(Calendar.MINUTE) + ":"//
					+ Calendar.getInstance().get(Calendar.SECOND) + "] "//
					+ message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];

		// No console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private void revealConsole(MessageConsole console) throws PartInitException {
		String id = IConsoleConstants.ID_CONSOLE_VIEW;

		if (activePage == null) {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			activePage = win.getActivePage();
		}

		if (activePage != null) {
			IConsoleView view = (IConsoleView) activePage.showView(id);
			view.display(console);
		}
	}

	public void printError(String message) {
		printMessage(message, SWT.COLOR_RED);
	}

	public void printInfo(String message) {
		printMessage(message, SWT.COLOR_BLUE);
	}

	public void printWarn(String message) {
		printMessage(message, SWT.COLOR_YELLOW);
	}

	public void printDebug(String message) {
		printMessage(message, SWT.COLOR_GRAY);
	}

	public static void setActivePage(IWorkbenchPage page) {
		activePage = page;
	}

	@Override
	public void close() {

	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		switch (event.getLevel().toInt()) {
		case Level.DEBUG_INT:
			printDebug(event.getRenderedMessage());
			break;
		case Level.INFO_INT:
			printInfo(event.getRenderedMessage());
			break;
		case Level.WARN_INT:
			printWarn(event.getRenderedMessage());
			break;
		case Level.ERROR_INT:
		case Level.FATAL_INT:
			printError(event.getRenderedMessage());
			break;
		default:
			break;
		}
	}
}

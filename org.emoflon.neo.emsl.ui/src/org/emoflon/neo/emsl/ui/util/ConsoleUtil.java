package org.emoflon.neo.emsl.ui.util;

import java.io.IOException;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleUtil {

	private static final String CONSOLE_NAME = "eMoflon::Neo";

	private static void printMessage(IWorkbenchPage activePage, String message, int color) {
		MessageConsole console = findConsole(CONSOLE_NAME);
		MessageConsoleStream out = console.newMessageStream();
		out.setColor(Display.getCurrent().getSystemColor(color));
		out.println("[" //
				+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"//
				+ Calendar.getInstance().get(Calendar.MINUTE) + ":"//
				+ Calendar.getInstance().get(Calendar.SECOND) + "] "//
				+ message);
		try {
			out.close();
			revealConsole(console, activePage);
		} catch (IOException | PartInitException e) {
			e.printStackTrace();
		}
	}

	private static MessageConsole findConsole(String name) {
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

	private static void revealConsole(MessageConsole console, IWorkbenchPage activePage) throws PartInitException {
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = (IConsoleView) activePage.showView(id);
		view.display(console);
	}

	public static void printError(IWorkbenchPage activePage, String message) {
		printMessage(activePage, message, SWT.COLOR_RED);
	}

	public static void printInfo(IWorkbenchPage activePage, String message) {
		printMessage(activePage, message, SWT.COLOR_BLUE);
	}

	public static void printBar(IWorkbenchPage activePage) {
		printInfo(activePage, "*******************************************");
	}
	
	public static void printDash(IWorkbenchPage activePage) {
		printInfo(activePage, "-------------------------------------------");
	}
}

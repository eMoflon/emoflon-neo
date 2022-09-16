package org.emoflon.ibex.neo.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.emoflon.ibex.neo.benchmark.util.BenchContainer;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;

public class ScaledBenchRunner<B extends NeoBench<?, BP>, BP extends BenchParameters> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
	
	private final static int MAX_TIMEOUTS = 5;
	private final static int MAX_EXCEPTIONS = 5;
	private final static int TIMEOUT_MINUTES = 10;
	
	protected final Class<B> benchClass;
	protected final Class<BP> paramsClass;
	protected final List<String> jvmArgs;
	protected final List<String[]> execArgs;
	protected final int repetitions;
	private File currentLogFile;

	public ScaledBenchRunner(Class<B> benchClass, Class<BP> paramsClass, List<String> jvmArgs, List<String[]> execArgs, int repetitions) {
		this.benchClass = benchClass;
		this.paramsClass = paramsClass;
		this.jvmArgs = jvmArgs;
		this.execArgs = execArgs;
		this.repetitions = repetitions;
	}

	public void run() throws Exception {
		BenchContainer<BP> benchCont = new BenchContainer();

		for (String[] args : this.execArgs) {
			int timeoutCounter = 0;
			int exceptionCounter = 0;
			for (int r = 0; r < this.repetitions; r++) {
				
				if(timeoutCounter >= MAX_TIMEOUTS) {
					System.out.println("Timeout (" + TIMEOUT_MINUTES + "min) for " + Arrays.asList(args));
					break;
				}
				
				if(exceptionCounter >= MAX_EXCEPTIONS) {
					System.out.println("Too many exceptions for " + Arrays.asList(args));
					break;
				}
				
				Process process = execute(this.benchClass, jvmArgs, Arrays.asList(args));
				InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
				BufferedReader reader = new BufferedReader(inputStreamReader);
				
				if(!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
					// count timeouts and restart repetition
					timeoutCounter++;
					r--;
					terminateProcess(process);
					continue;
				}
				
				if(process.exitValue() != 0) {
					StringBuilder b = new StringBuilder();
					String read = reader.readLine();
					while (read != null) {
						b.append(read);
						b.append("\n");
						read = reader.readLine();
					}
					System.err.println(b);
					// count exceptions and restart repetition if one is detected
					exceptionCounter++;
					r--;
					terminateProcess(process);
					continue;
				}
				
				// clean up log file if it is empty
				if(currentLogFile.length() == 0) {
					currentLogFile.delete();
				}
				
				StringBuilder b = new StringBuilder();
				String read = reader.readLine();
				while (read != null) {
					b.append(read);
					b.append("\n");
					read = reader.readLine();
				}

				if(b.isEmpty()) {
					continue;
				}
				BenchEntry<BP> benchEntry = new BenchEntry<>(b.toString(), paramsClass);
				benchCont.addBench(benchEntry);

				System.out.println(benchEntry);
			}
			
			if(timeoutCounter >= MAX_TIMEOUTS || exceptionCounter >= MAX_EXCEPTIONS) {
				// also step out of the outer for loop
				break;
			}
		}

		benchCont.print();
	}

	private void terminateProcess(Process process) throws InterruptedException {
		process.destroy();
		int counter=0;
		while(process.isAlive()) {
			Thread.sleep(10);
			counter++;
			if(counter>=100)
				process.destroyForcibly();
		}
	}

	protected Process execute(Class<?> clazz, List<String> jvmArgs, List<String> args) throws IOException, InterruptedException {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = clazz.getName();
		
		// create log file and redirect the error stream to it
		String logFolderPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath().toString().replace("bin/", "") + "log/";
		File logFolder = new File(logFolderPath);
		logFolder.mkdirs();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		File logFile = new File(logFolderPath + "log_" + args + DATE_FORMAT.format(timestamp) + ".txt");
		if(!logFile.exists())
			logFile.createNewFile();
		currentLogFile = logFile;
	        
		
		List<String> command = new ArrayList<>();
		command.add(javaBin);
		command.addAll(jvmArgs);
		command.add("-cp");
		command.add(classpath);
		command.add(className);
		command.addAll(args);
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectError(logFile);
		Process process = builder.start();
		return process;
	}

}

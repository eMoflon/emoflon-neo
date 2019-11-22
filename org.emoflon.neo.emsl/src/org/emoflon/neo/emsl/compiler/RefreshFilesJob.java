package org.emoflon.neo.emsl.compiler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class RefreshFilesJob extends Job {

	private List<IFile> files = new ArrayList<>();
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Refreshing derived GT files", 2);
		
		try {
			monitor.subTask("Waiting for build to complete...");
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
			monitor.worked(1);
		} catch (Exception e) {
			return Status.CANCEL_STATUS;
		}

		monitor.subTask("Now refreshing files: " + files);
		
		files.forEach(f -> {
			try {
				if(f.exists())
					f.touch(monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		});
		
		monitor.worked(1);
		monitor.done();
		return Status.OK_STATUS;
	}

	public RefreshFilesJob(List<IFile> files) {
		super("Refreshing files: " + files);
		this.files.addAll(files);
	}

}

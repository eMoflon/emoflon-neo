package org.emoflon.ibex.neo.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.delta.validation.InvalidDeltaException;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;
import org.emoflon.neo.engine.modules.ilp.*;
import org.emoflon.neo.emf.*;
import run.emf.modelimport.*;

import delta.impl.DeltaPackageImpl;

public abstract class NeoBench<OpStrat extends ILPBasedOperationalStrategy, BenchParams extends BenchParameters> {

	protected final String projectName;
	protected final String projectPath;
	protected final String genModelFolder = "gen-models";

	protected final String filename_src = "/src.xmi";
	protected final String filename_trg = "/trg.xmi";
	protected final String filename_corr = "/corr.xmi";
	protected final String filename_protocol = "/protocol.xmi";
	protected final String filename_delta = "/delta.xmi";

	protected final String workspacePath = "../";
	protected final URI base = URI.createPlatformResourceURI("/", true);
	protected ResourceSet rs;

	protected Resource source;
	protected Resource target;
	protected Resource corr;
	protected Resource protocol;
	protected Resource delta;

	protected int numOfElements = -1;

	public NeoBench(String projectName, String projectPath) {
		this.projectName = projectName;
		this.projectPath = projectPath;
	}
	
	public void importModels() {
		Neo4jImporter importer = new Neo4jImporter();
		initResourceSet();

		importer.importEMFModels(rs, "bolt://localhost:7687", "neo4j", "test");
	}
	
	public abstract ILPBasedOperationalStrategy initOpStrat();
	
	public Resource loadResource(String workspaceRelativePath) throws IOException {
		Resource res = createResource(workspaceRelativePath);
		try {
			res.load(null);
		} catch (FileNotFoundException e) {
			throw e; //new TGGFileNotFoundException(e, res.getURI());
		}
		EcoreUtil.resolveAll(res);
		return res;
	}
	
	public void createResources() {
		source = createResource(projectPath + "/instances" + filename_src);
		target = createResource(projectPath + "/instances" + filename_trg);
		corr = createResource(projectPath + "/instances" + filename_trg);
		protocol = createResource(projectPath + "/instances" + filename_protocol);
	}
	
	public BenchEntry genAndBench(BenchParams parameters, boolean saveTransformedModels) {
		try {
			ILPBasedOperationalStrategy opStrat = initOpStrat();
			createResources();

			ModelAndDeltaGenerator<?, ?, ?, ?, ?, BenchParams> mdGenerator = initModelAndDeltaGenerator(source, target, corr, protocol, delta);
			mdGenerator.gen(parameters);
			this.numOfElements = mdGenerator.getNumOfElements();

			return applyDeltaAndRun(opStrat, parameters, saveTransformedModels);
		} catch (IOException | InvalidDeltaException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void genAndStore(BenchParams parameters) {
		try {
			initResourceSet();
			initModels(projectName + "/" + genModelFolder + "/" + parameters.toString());

			//ModelAndDeltaGenerator<?, ?, ?, ?, ?, BenchParams> mdGenerator = initModelAndDeltaGenerator(source, target, corr, protocol, delta);
			//mdGenerator.gen(parameters);
			
			//TODO Open Models and DeltaGenerator
			this.numOfElements = mdGenerator.getNumOfElements();

			// TODO persist numOfElements!

			saveModels();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadModels(BenchParams parameters) throws IOException {
		String workspaceRelativeFolder = projectName + "/" + genModelFolder + "/" + parameters.toString();
		
		source = loadResource(workspaceRelativeFolder + filename_src);
		target = loadResource(workspaceRelativeFolder + filename_trg);
		corr = loadResource(workspaceRelativeFolder + filename_corr);
		protocol = loadResource(workspaceRelativeFolder + filename_protocol);

		DeltaPackageImpl.init();
		delta = loadResource(workspaceRelativeFolder + filename_delta);

		changeURI(source, "/instances" + filename_src);
		changeURI(target, "/instances" + filename_trg);
		changeURI(corr, "/instances" + filename_corr);
		changeURI(protocol, "/instances" + filename_protocol);

		EcoreUtil.resolveAll(rs);
	}

	private void changeURI(Resource r, String path) {
		URI uri = URI.createURI(projectPath + path);
		r.setURI(uri.resolve(base));
	}
	
	public BenchEntry loadAndBench(BenchParams parameters, boolean saveTransformedModels) {
		
		try {
			ILPBasedOperationalStrategy opStrat = initOpStrat();
			loadModels(parameters);

			return applyDeltaAndRun(opStrat, parameters, saveTransformedModels);
		} catch (IOException | InvalidDeltaException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract ModelAndDeltaGenerator<?, ?, ?, ?, ?, BenchParams> initModelAndDeltaGenerator(Resource s, Resource t, Resource c, Resource p,
			Resource d);

	protected abstract BenchEntry applyDeltaAndRun(ILPBasedOperationalStrategy opStrat, BenchParams parameters, boolean saveTransformedModels)
			throws IOException, InvalidDeltaException;

	private void initResourceSet() {
		rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap() //
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		try {
			rs.getURIConverter().getURIMap().put(URI.createPlatformResourceURI("/", true),
					URI.createFileURI(new File(workspacePath).getCanonicalPath() + File.separator));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Resource createResource(String workspaceRelativePath) {
		URI uri = URI.createURI(workspaceRelativePath);
		Resource res = rs.createResource(uri.resolve(base), ContentHandler.UNSPECIFIED_CONTENT_TYPE);
		return res;
	}

	private void initModels(String path) {
		source = createResource(path + filename_src);
		target = createResource(path + filename_trg);
		corr = createResource(path + filename_corr);
		protocol = createResource(path + filename_protocol);
		delta = createResource(path + filename_delta);
	}

	protected void saveModels() throws IOException {
		source.save(null);
		target.save(null);
		corr.save(null);
		protocol.save(null);
		delta.save(null);
	}

}

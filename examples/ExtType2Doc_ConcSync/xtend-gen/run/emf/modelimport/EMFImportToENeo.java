package run.emf.modelimport;

import java.util.Collection;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.neocore.ENeoUtil;
import run.ExtType2Doc_ConcSync_CO_Run;

@SuppressWarnings("all")
public class EMFImportToENeo {
  private static final Logger logger = Logger.getLogger(EMFImportToENeo.class);

  private static NeoCoreBuilder builder /* Skipped initializer because of errors */;

  public static void main(final String[] args) {
    try {
      EMFImportToENeo.loadModelsAndMetamodels("./emf/metamodels/", "./emf/gen-models/presDel-scaled_n64_c1_H/", "src.xmi", "trg.xmi", "corr.xmi");
      final ExtType2Doc_ConcSync_CO_Run app = new ExtType2Doc_ConcSync_CO_Run();
      final CheckOnlyOperationalStrategy result = app.runCheckOnly("src.xmi", "trg.xmi");
      Collection<Long> _determineInconsistentElements = result.determineInconsistentElements();
      String _plus = ("inconsistent elements: " + _determineInconsistentElements);
      EMFImportToENeo.logger.info(_plus);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        String _message = e.getMessage();
        String _plus_1 = ("Loading models and metamodels failed: " + _message);
        EMFImportToENeo.logger.info(_plus_1);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  private static XtextResourceSet createResourceSet() {
    return ENeoUtil.createEMSLStandaloneResourceSet(".");
  }

  private static void loadModel(final ResourceSet rs, final String uri, final String label) {
    final Resource resource = rs.getResource(URI.createURI(uri), true);
    resource.setURI(URI.createURI(label));
  }

  private static void loadMetamodel(final ResourceSet rs, final String uri) {
    Resource resource = rs.getResource(URI.createURI(uri), true);
    EObject _get = resource.getContents().get(0);
    final EPackage root = ((EPackage) _get);
    resource.setURI(URI.createURI(root.getNsURI()));
  }

  public static void loadModelsAndMetamodels(final String metamodelPath, final String modelPath, final String srcModel, final String trgModel, final String corrModel) {
    throw new Error("Unresolved compilation problems:"
      + "\nAPI_IbexToENeo cannot be resolved."
      + "\nrule_MigrateProject2DocContainer cannot be resolved"
      + "\nrule cannot be resolved"
      + "\nrule_MigratePackage2Folder cannot be resolved"
      + "\nrule cannot be resolved"
      + "\nrule_MigrateType2Doc cannot be resolved"
      + "\nrule cannot be resolved"
      + "\nrule_MigrateMethod2Entry cannot be resolved"
      + "\nrule cannot be resolved"
      + "\nrule_MigrateParam2Entry cannot be resolved"
      + "\nrule cannot be resolved"
      + "\nrule_MigrateField2Entry cannot be resolved"
      + "\nrule cannot be resolved"
      + "\nrule_MigrateJDoc2Annotation cannot be resolved"
      + "\nrule cannot be resolved"
      + "\napplyAll cannot be resolved"
      + "\ndetermineMatches cannot be resolved");
  }
}

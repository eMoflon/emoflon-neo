package run.emf.modelimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.emoflon.neo.api.exttype2doc_concsync.API_Common;
import org.emoflon.neo.api.exttype2doc_concsync.run.emf.modelimport.API_IbexToENeo;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.emf.Neo4jImporter;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.neocore.ENeoUtil;
import run.ExtType2Doc_ConcSync_CO_Run;

@SuppressWarnings("all")
public class EMFImportToENeo {
  public static void main(final String[] args) {
    try {
      List<Throwable> _ts = new ArrayList<Throwable>();
      NeoCoreBuilder builder = null;
      try {
        builder = API_Common.createBuilder();
        builder.clearDataBase();
        final Neo4jImporter importer = new Neo4jImporter();
        final String boltURL = "bolt://localhost:7687";
        final String dbName = "neo4j";
        final String passw = "test";
        final XtextResourceSet rs = EMFImportToENeo.createResourceSet();
        EMFImportToENeo.loadMetamodel(rs, "./emf/metamodels/ExtDocModel.ecore");
        EMFImportToENeo.loadMetamodel(rs, "./emf/metamodels/ExtTypeModel.ecore");
        EMFImportToENeo.loadMetamodel(rs, "./emf/metamodels/ExtType2Doc_ConcSync.ecore");
        EMFImportToENeo.loadModel(rs, "./emf/gen-models/presDel-scaled_n64_c1_H/src.xmi", "src.xmi");
        EMFImportToENeo.loadModel(rs, "./emf/gen-models/presDel-scaled_n64_c1_H/trg.xmi", "trg.xmi");
        EMFImportToENeo.loadModel(rs, "./emf/gen-models/presDel-scaled_n64_c1_H/corr.xmi", "corr.xmi");
        importer.importEMFModels(rs, boltURL, dbName, passw);
        final API_IbexToENeo ruleAPI = new API_IbexToENeo(builder);
        NeoRule _rule = ruleAPI.getRule_MigrateProject2DocContainer().rule();
        NeoRule _rule_1 = ruleAPI.getRule_MigratePackage2Folder().rule();
        NeoRule _rule_2 = ruleAPI.getRule_MigrateType2Doc().rule();
        NeoRule _rule_3 = ruleAPI.getRule_MigrateMethod2Entry().rule();
        NeoRule _rule_4 = ruleAPI.getRule_MigrateParam2Entry().rule();
        NeoRule _rule_5 = ruleAPI.getRule_MigrateField2Entry().rule();
        NeoRule _rule_6 = ruleAPI.getRule_MigrateJDoc2Annotation().rule();
        for (final NeoRule rule : Collections.<NeoRule>unmodifiableSet(CollectionLiterals.<NeoRule>newHashSet(_rule, _rule_1, _rule_2, _rule_3, _rule_4, _rule_5, _rule_6))) {
          rule.applyAll(rule.determineMatches());
        }
        InputOutput.<String>println("Migrated all corrs");
        final ExtType2Doc_ConcSync_CO_Run app = new ExtType2Doc_ConcSync_CO_Run();
        final CheckOnlyOperationalStrategy result = app.runCheckOnly("src.xmi", "trg.xmi");
        Collection<Long> _determineInconsistentElements = result.determineInconsistentElements();
        String _plus = ("inconsistent elements: " + _determineInconsistentElements);
        InputOutput.<String>println(_plus);
      } finally {
        if (builder != null) {
          try {
            builder.close();
          } catch (Throwable _t) {
            _ts.add(_t);
          }
        }
        if(!_ts.isEmpty()) throw Exceptions.sneakyThrow(_ts.get(0));
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
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
}

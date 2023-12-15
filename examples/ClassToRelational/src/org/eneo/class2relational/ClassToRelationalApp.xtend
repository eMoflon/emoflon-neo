package org.eneo.class2relational

import org.emoflon.neo.api.classtorelational.API_Common
import org.emoflon.neo.api.classtorelational.metamodels.API_Class
import org.emoflon.neo.api.classtorelational.metamodels.API_Relational
import org.emoflon.neo.api.classtorelational.models.correctness1.API_correctness1_class

class ClassToRelationalApp {
	
def static void main(String[] args) {
    try (val builder = API_Common.createBuilder) {
      // Retrieve handles for APIs
      val apiClass = new API_Class(builder)
      val apiCorrectess1Class = new API_correctness1_class(builder)
      val apiRel = new API_Relational(builder)
            
      // Empty database to ensure a clean slate
      builder.clearDataBase
                  
      // Export metamodels and models
      builder.exportEMSLEntityToNeo4j(apiClass.metamodel_Class_)
      builder.exportEMSLEntityToNeo4j(apiRel.metamodel_Relational_)
      builder.exportEMSLEntityToNeo4j(apiCorrectess1Class.model_Correctness1_class)
      
      val fwd = new ClassToRelational_FWD_OPT_Run_App("correctness1_class", "correctness1_relational")
   	  fwd.run
   }
 }
}
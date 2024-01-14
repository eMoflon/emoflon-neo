package org.eneo.class2relational

import org.emoflon.neo.api.classtorelational.API_Common
import org.emoflon.neo.api.classtorelational.metamodels.API_Class
import org.emoflon.neo.api.classtorelational.metamodels.API_Relational
import org.emoflon.neo.api.classtorelational.models.correctness1.API_change1
import org.emoflon.neo.api.classtorelational.models.correctness2.API_change2
import org.emoflon.neo.api.classtorelational.models.correctness3.API_change3
import org.emoflon.neo.api.classtorelational.models.correctness4.API_change4
import org.emoflon.neo.api.classtorelational.models.correctness5.API_change5
import org.emoflon.neo.api.classtorelational.models.correctness6.API_change6
import org.emoflon.neo.api.classtorelational.models.correctness7.API_change7
import org.emoflon.neo.api.classtorelational.models.correctness8.API_change8
import org.emoflon.neo.api.classtorelational.models.correctness9.API_change9
import org.emoflon.neo.api.classtorelational.models.correctness10.API_change10
import org.emoflon.neo.api.classtorelational.models.correctness11.API_change11
import org.emoflon.neo.api.classtorelational.models.correctness12.API_change12
import org.emoflon.neo.api.classtorelational.models.correctness13.API_change13
import java.util.ArrayList
import org.emoflon.neo.emsl.eMSL.Model

class ClassToRelationalApp {
	
def static void main(String[] args) {
    try (val builder = API_Common.createBuilder) {
      // Retrieve handles for APIs
      val apiClass = new API_Class(builder)
      val apiRel = new API_Relational(builder)
      var apiArray = new ArrayList<Model>()
      
      apiArray.add(new API_change1(builder).model_Class1)
      apiArray.add(new API_change2(builder).model_Class2)
      apiArray.add(new API_change3(builder).model_Class3)
      apiArray.add(new API_change4(builder).model_Class4)
      apiArray.add(new API_change5(builder).model_Class5)
      apiArray.add(new API_change6(builder).model_Class6)
      apiArray.add(new API_change7(builder).model_Class7)
      apiArray.add(new API_change8(builder).model_Class8)
      apiArray.add(new API_change9(builder).model_Class9)
      apiArray.add(new API_change10(builder).model_Class10)
      apiArray.add(new API_change11(builder).model_Class11)
      apiArray.add(new API_change12(builder).model_Class12)
      apiArray.add(new API_change13(builder).model_Class13)
      
      // Empty database to ensure a clean state
      builder.clearDataBase
                  
      // Export metamodels and models
      builder.exportEMSLEntityToNeo4j(apiClass.metamodel_Class_)
      builder.exportEMSLEntityToNeo4j(apiRel.metamodel_Relational_)
      
      for (var i=1; i<=13; i++) {
		  builder.exportEMSLEntityToNeo4j(apiArray.get(i-1))
		  
		  val fwd = new ClassToRelational_FWD_OPT_Run_App("class"+i, "correctness"+i+"relational")
		  var result = fwd.runForwardTransformation()
		  print(result.determineInconsistentElements())
   	  }
   }
 }
}
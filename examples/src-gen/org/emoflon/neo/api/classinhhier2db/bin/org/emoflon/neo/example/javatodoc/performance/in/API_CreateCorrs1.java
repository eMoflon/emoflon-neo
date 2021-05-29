/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin.org.emoflon.neo.example.javatodoc.performance.in;

import org.emoflon.neo.cypher.common.*;
import org.emoflon.neo.cypher.constraints.*;
import org.emoflon.neo.cypher.factories.*;
import org.emoflon.neo.cypher.models.*;
import org.emoflon.neo.cypher.patterns.*;
import org.emoflon.neo.cypher.rules.*;
import org.emoflon.neo.engine.api.patterns.*;
import org.emoflon.neo.engine.api.constraints.*;
import org.emoflon.neo.engine.api.rules.*;
import org.emoflon.neo.emsl.eMSL.*;
import org.emoflon.neo.emsl.util.*;
import org.neo4j.driver.Value;
import org.neo4j.driver.Record;
import org.eclipse.emf.common.util.URI;
import org.emoflon.neo.api.classinhhier2db.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_CreateCorrs1 {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_CreateCorrs1(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_CreateCorrs1(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs1.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_CreateCorrs1(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs1.msl#//@entities.0
	public CreateClazzToDoc1Access getRule_CreateClazzToDoc1() {
		return new CreateClazzToDoc1Access();
	}
	
	public class CreateClazzToDoc1Access extends NeoRuleCoAccess<CreateClazzToDoc1Data, CreateClazzToDoc1CoData, CreateClazzToDoc1Mask> {
		public final String _c100 = "c100";
		public final String _d100 = "d100";
		public final String _c101 = "c101";
		public final String _d101 = "d101";
		public final String _c102 = "c102";
		public final String _d102 = "d102";
		public final String _c103 = "c103";
		public final String _d103 = "d103";
		public final String _c104 = "c104";
		public final String _d104 = "d104";
		public final String _c105 = "c105";
		public final String _d105 = "d105";
		public final String _c106 = "c106";
		public final String _d106 = "d106";
		public final String _c107 = "c107";
		public final String _d107 = "d107";
		public final String _c108 = "c108";
		public final String _d108 = "d108";
		public final String _c109 = "c109";
		public final String _d109 = "d109";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(0);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateClazzToDoc1Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateClazzToDoc1Data(d));
		}
			
		@Override
		public Stream<CreateClazzToDoc1CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateClazzToDoc1CoData(d));
		}
		
		@Override
		public CreateClazzToDoc1Mask mask() {
			return new CreateClazzToDoc1Mask();
		}
	}
	
	public class CreateClazzToDoc1Data extends NeoData {
		public CreateClazzToDoc1Data(Record data) {
		
		}
	}
	
	public class CreateClazzToDoc1CoData extends NeoData {
		public CreateClazzToDoc1CoData(Record data) {
		
		}
	}
	
	public class CreateClazzToDoc1Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs1.msl#//@entities.1
	public CreateMethodToEntry1Access getRule_CreateMethodToEntry1() {
		return new CreateMethodToEntry1Access();
	}
	
	public class CreateMethodToEntry1Access extends NeoRuleCoAccess<CreateMethodToEntry1Data, CreateMethodToEntry1CoData, CreateMethodToEntry1Mask> {
		public final String _m110 = "m110";
		public final String _e110 = "e110";
		public final String _p171 = "p171";
		public final String _m111 = "m111";
		public final String _e111 = "e111";
		public final String _m112 = "m112";
		public final String _e112 = "e112";
		public final String _p155 = "p155";
		public final String _p158 = "p158";
		public final String _p169 = "p169";
		public final String _p173 = "p173";
		public final String _m113 = "m113";
		public final String _e113 = "e113";
		public final String _m114 = "m114";
		public final String _e114 = "e114";
		public final String _p150 = "p150";
		public final String _m115 = "m115";
		public final String _e115 = "e115";
		public final String _p174 = "p174";
		public final String _p162 = "p162";
		public final String _p165 = "p165";
		public final String _m116 = "m116";
		public final String _e116 = "e116";
		public final String _p183 = "p183";
		public final String _m117 = "m117";
		public final String _e117 = "e117";
		public final String _p185 = "p185";
		public final String _m118 = "m118";
		public final String _e118 = "e118";
		public final String _p184 = "p184";
		public final String _m119 = "m119";
		public final String _e119 = "e119";
		public final String _p190 = "p190";
		public final String _m120 = "m120";
		public final String _e120 = "e120";
		public final String _m121 = "m121";
		public final String _e121 = "e121";
		public final String _p187 = "p187";
		public final String _m122 = "m122";
		public final String _e122 = "e122";
		public final String _m123 = "m123";
		public final String _e123 = "e123";
		public final String _m124 = "m124";
		public final String _e124 = "e124";
		public final String _p156 = "p156";
		public final String _p160 = "p160";
		public final String _m125 = "m125";
		public final String _e125 = "e125";
		public final String _p178 = "p178";
		public final String _m126 = "m126";
		public final String _e126 = "e126";
		public final String _p198 = "p198";
		public final String _p164 = "p164";
		public final String _m127 = "m127";
		public final String _e127 = "e127";
		public final String _m128 = "m128";
		public final String _e128 = "e128";
		public final String _m129 = "m129";
		public final String _e129 = "e129";
		public final String _p166 = "p166";
		public final String _p163 = "p163";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateMethodToEntry1Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateMethodToEntry1Data(d));
		}
			
		@Override
		public Stream<CreateMethodToEntry1CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateMethodToEntry1CoData(d));
		}
		
		@Override
		public CreateMethodToEntry1Mask mask() {
			return new CreateMethodToEntry1Mask();
		}
	}
	
	public class CreateMethodToEntry1Data extends NeoData {
		public CreateMethodToEntry1Data(Record data) {
		
		}
	}
	
	public class CreateMethodToEntry1CoData extends NeoData {
		public CreateMethodToEntry1CoData(Record data) {
		
		}
	}
	
	public class CreateMethodToEntry1Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs1.msl#//@entities.2
	public CreateFieldToEntry1Access getRule_CreateFieldToEntry1() {
		return new CreateFieldToEntry1Access();
	}
	
	public class CreateFieldToEntry1Access extends NeoRuleCoAccess<CreateFieldToEntry1Data, CreateFieldToEntry1CoData, CreateFieldToEntry1Mask> {
		public final String _f130 = "f130";
		public final String _f131 = "f131";
		public final String _f132 = "f132";
		public final String _f133 = "f133";
		public final String _f134 = "f134";
		public final String _f135 = "f135";
		public final String _f136 = "f136";
		public final String _f137 = "f137";
		public final String _f138 = "f138";
		public final String _f139 = "f139";
		public final String _f140 = "f140";
		public final String _f141 = "f141";
		public final String _f142 = "f142";
		public final String _f143 = "f143";
		public final String _f144 = "f144";
		public final String _f145 = "f145";
		public final String _f146 = "f146";
		public final String _f147 = "f147";
		public final String _f148 = "f148";
		public final String _f149 = "f149";
		public final String _e130 = "e130";
		public final String _e131 = "e131";
		public final String _e132 = "e132";
		public final String _e133 = "e133";
		public final String _e134 = "e134";
		public final String _e135 = "e135";
		public final String _e136 = "e136";
		public final String _e137 = "e137";
		public final String _e138 = "e138";
		public final String _e139 = "e139";
		public final String _e140 = "e140";
		public final String _e141 = "e141";
		public final String _e142 = "e142";
		public final String _e143 = "e143";
		public final String _e144 = "e144";
		public final String _e145 = "e145";
		public final String _e146 = "e146";
		public final String _e147 = "e147";
		public final String _e148 = "e148";
		public final String _e149 = "e149";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(2);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateFieldToEntry1Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateFieldToEntry1Data(d));
		}
			
		@Override
		public Stream<CreateFieldToEntry1CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateFieldToEntry1CoData(d));
		}
		
		@Override
		public CreateFieldToEntry1Mask mask() {
			return new CreateFieldToEntry1Mask();
		}
	}
	
	public class CreateFieldToEntry1Data extends NeoData {
		public CreateFieldToEntry1Data(Record data) {
		
		}
	}
	
	public class CreateFieldToEntry1CoData extends NeoData {
		public CreateFieldToEntry1CoData(Record data) {
		
		}
	}
	
	public class CreateFieldToEntry1Mask extends NeoMask {
	}
}
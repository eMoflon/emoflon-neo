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
public class API_CreateCorrs2 {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_CreateCorrs2(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_CreateCorrs2(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs2.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_CreateCorrs2(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs2.msl#//@entities.0
	public CreateClazzToDoc2Access getRule_CreateClazzToDoc2() {
		return new CreateClazzToDoc2Access();
	}
	
	public class CreateClazzToDoc2Access extends NeoRuleCoAccess<CreateClazzToDoc2Data, CreateClazzToDoc2CoData, CreateClazzToDoc2Mask> {
		public final String _c200 = "c200";
		public final String _d200 = "d200";
		public final String _c201 = "c201";
		public final String _d201 = "d201";
		public final String _c202 = "c202";
		public final String _d202 = "d202";
		public final String _c203 = "c203";
		public final String _d203 = "d203";
		public final String _c204 = "c204";
		public final String _d204 = "d204";
		public final String _c205 = "c205";
		public final String _d205 = "d205";
		public final String _c206 = "c206";
		public final String _d206 = "d206";
		public final String _c207 = "c207";
		public final String _d207 = "d207";
		public final String _c208 = "c208";
		public final String _d208 = "d208";
		public final String _c209 = "c209";
		public final String _d209 = "d209";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(0);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateClazzToDoc2Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateClazzToDoc2Data(d));
		}
			
		@Override
		public Stream<CreateClazzToDoc2CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateClazzToDoc2CoData(d));
		}
		
		@Override
		public CreateClazzToDoc2Mask mask() {
			return new CreateClazzToDoc2Mask();
		}
	}
	
	public class CreateClazzToDoc2Data extends NeoData {
		public CreateClazzToDoc2Data(Record data) {
		
		}
	}
	
	public class CreateClazzToDoc2CoData extends NeoData {
		public CreateClazzToDoc2CoData(Record data) {
		
		}
	}
	
	public class CreateClazzToDoc2Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs2.msl#//@entities.1
	public CreateMethodToEntry2Access getRule_CreateMethodToEntry2() {
		return new CreateMethodToEntry2Access();
	}
	
	public class CreateMethodToEntry2Access extends NeoRuleCoAccess<CreateMethodToEntry2Data, CreateMethodToEntry2CoData, CreateMethodToEntry2Mask> {
		public final String _m210 = "m210";
		public final String _e210 = "e210";
		public final String _p269 = "p269";
		public final String _p255 = "p255";
		public final String _m211 = "m211";
		public final String _e211 = "e211";
		public final String _p270 = "p270";
		public final String _p276 = "p276";
		public final String _m212 = "m212";
		public final String _e212 = "e212";
		public final String _p268 = "p268";
		public final String _m213 = "m213";
		public final String _e213 = "e213";
		public final String _p257 = "p257";
		public final String _m214 = "m214";
		public final String _e214 = "e214";
		public final String _m215 = "m215";
		public final String _e215 = "e215";
		public final String _m216 = "m216";
		public final String _e216 = "e216";
		public final String _p259 = "p259";
		public final String _m217 = "m217";
		public final String _e217 = "e217";
		public final String _p290 = "p290";
		public final String _m218 = "m218";
		public final String _e218 = "e218";
		public final String _p294 = "p294";
		public final String _m219 = "m219";
		public final String _e219 = "e219";
		public final String _p253 = "p253";
		public final String _m220 = "m220";
		public final String _e220 = "e220";
		public final String _p263 = "p263";
		public final String _p281 = "p281";
		public final String _m221 = "m221";
		public final String _e221 = "e221";
		public final String _p266 = "p266";
		public final String _m222 = "m222";
		public final String _e222 = "e222";
		public final String _p273 = "p273";
		public final String _p258 = "p258";
		public final String _m223 = "m223";
		public final String _e223 = "e223";
		public final String _p286 = "p286";
		public final String _m224 = "m224";
		public final String _e224 = "e224";
		public final String _p260 = "p260";
		public final String _p279 = "p279";
		public final String _m225 = "m225";
		public final String _e225 = "e225";
		public final String _m226 = "m226";
		public final String _e226 = "e226";
		public final String _p267 = "p267";
		public final String _m227 = "m227";
		public final String _e227 = "e227";
		public final String _m228 = "m228";
		public final String _e228 = "e228";
		public final String _p295 = "p295";
		public final String _m229 = "m229";
		public final String _e229 = "e229";
		public final String _p288 = "p288";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateMethodToEntry2Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateMethodToEntry2Data(d));
		}
			
		@Override
		public Stream<CreateMethodToEntry2CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateMethodToEntry2CoData(d));
		}
		
		@Override
		public CreateMethodToEntry2Mask mask() {
			return new CreateMethodToEntry2Mask();
		}
	}
	
	public class CreateMethodToEntry2Data extends NeoData {
		public CreateMethodToEntry2Data(Record data) {
		
		}
	}
	
	public class CreateMethodToEntry2CoData extends NeoData {
		public CreateMethodToEntry2CoData(Record data) {
		
		}
	}
	
	public class CreateMethodToEntry2Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/javatodoc/performance/in/CreateCorrs2.msl#//@entities.2
	public CreateFieldToEntry2Access getRule_CreateFieldToEntry2() {
		return new CreateFieldToEntry2Access();
	}
	
	public class CreateFieldToEntry2Access extends NeoRuleCoAccess<CreateFieldToEntry2Data, CreateFieldToEntry2CoData, CreateFieldToEntry2Mask> {
		public final String _f230 = "f230";
		public final String _f231 = "f231";
		public final String _f232 = "f232";
		public final String _f233 = "f233";
		public final String _f234 = "f234";
		public final String _f235 = "f235";
		public final String _f236 = "f236";
		public final String _f237 = "f237";
		public final String _f238 = "f238";
		public final String _f239 = "f239";
		public final String _f240 = "f240";
		public final String _f241 = "f241";
		public final String _f242 = "f242";
		public final String _f243 = "f243";
		public final String _f244 = "f244";
		public final String _f245 = "f245";
		public final String _f246 = "f246";
		public final String _f247 = "f247";
		public final String _f248 = "f248";
		public final String _f249 = "f249";
		public final String _e230 = "e230";
		public final String _e231 = "e231";
		public final String _e232 = "e232";
		public final String _e233 = "e233";
		public final String _e234 = "e234";
		public final String _e235 = "e235";
		public final String _e236 = "e236";
		public final String _e237 = "e237";
		public final String _e238 = "e238";
		public final String _e239 = "e239";
		public final String _e240 = "e240";
		public final String _e241 = "e241";
		public final String _e242 = "e242";
		public final String _e243 = "e243";
		public final String _e244 = "e244";
		public final String _e245 = "e245";
		public final String _e246 = "e246";
		public final String _e247 = "e247";
		public final String _e248 = "e248";
		public final String _e249 = "e249";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(2);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateFieldToEntry2Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateFieldToEntry2Data(d));
		}
			
		@Override
		public Stream<CreateFieldToEntry2CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateFieldToEntry2CoData(d));
		}
		
		@Override
		public CreateFieldToEntry2Mask mask() {
			return new CreateFieldToEntry2Mask();
		}
	}
	
	public class CreateFieldToEntry2Data extends NeoData {
		public CreateFieldToEntry2Data(Record data) {
		
		}
	}
	
	public class CreateFieldToEntry2CoData extends NeoData {
		public CreateFieldToEntry2CoData(Record data) {
		
		}
	}
	
	public class CreateFieldToEntry2Mask extends NeoMask {
	}
}
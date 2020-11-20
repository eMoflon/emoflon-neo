/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.org.emoflon.neo.example.facebooktoinstagram;

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
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Record;
import org.eclipse.emf.common.util.URI;
import org.emoflon.neo.api.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_ModelForTesting {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_ModelForTesting(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_ModelForTesting(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		spec = (EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/TestSuiteTGG/src/org/emoflon/neo/example/facebooktoinstagram/ModelForTesting.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI);
		this.builder = builder;
	}

	//:~> platform:/resource/TestSuiteTGG/src/org/emoflon/neo/example/facebooktoinstagram/ModelForTesting.msl#//@entities.0
	public Model getModel_ConsistentSource(){
		return (Model) spec.getEntities().get(0);
	}
	
	//:~> platform:/resource/TestSuiteTGG/src/org/emoflon/neo/example/facebooktoinstagram/ModelForTesting.msl#//@entities.1
	public Model getModel_ConsistentTarget(){
		return (Model) spec.getEntities().get(1);
	}
	
	//:~> platform:/resource/TestSuiteTGG/src/org/emoflon/neo/example/facebooktoinstagram/ModelForTesting.msl#//@entities.2
	public ConsistentTripleAccess getRule_ConsistentTriple() {
		return new ConsistentTripleAccess();
	}
	
	public class ConsistentTripleAccess extends NeoRuleCoAccess<ConsistentTripleData, ConsistentTripleCoData, ConsistentTripleMask> {
		public final String _fb = "fb";
		public final String _f1 = "f1";
		public final String _f2 = "f2";
		public final String _in = "in";
		public final String _i1 = "i1";
		public final String _i2 = "i2";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(2);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<ConsistentTripleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ConsistentTripleData(d));
		}
			
		@Override
		public Stream<ConsistentTripleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ConsistentTripleCoData(d));
		}
		
		@Override
		public ConsistentTripleMask mask() {
			return new ConsistentTripleMask();
		}
	}
	
	public class ConsistentTripleData extends NeoData {
		public final FbNode _fb;
		public final Fb_corr_0_inRel _fb_corr_0_in;
		public final F1Node _f1;
		public final F1_corr_0_i1Rel _f1_corr_0_i1;
		public final F2Node _f2;
		public final F2_corr_0_i2Rel _f2_corr_0_i2;
		public final InNode _in;
		public final I1Node _i1;
		public final I2Node _i2;
		
		public ConsistentTripleData(Record data) {
			var _fb = data.get("fb");
			this._fb = new FbNode(_fb);
			var _fb_corr_0_in = data.get("fb_corr_0_in");
			this._fb_corr_0_in = new Fb_corr_0_inRel(_fb_corr_0_in);
			var _f1 = data.get("f1");
			this._f1 = new F1Node(_f1);
			var _f1_corr_0_i1 = data.get("f1_corr_0_i1");
			this._f1_corr_0_i1 = new F1_corr_0_i1Rel(_f1_corr_0_i1);
			var _f2 = data.get("f2");
			this._f2 = new F2Node(_f2);
			var _f2_corr_0_i2 = data.get("f2_corr_0_i2");
			this._f2_corr_0_i2 = new F2_corr_0_i2Rel(_f2_corr_0_i2);
			var _in = data.get("in");
			this._in = new InNode(_in);
			var _i1 = data.get("i1");
			this._i1 = new I1Node(_i1);
			var _i2 = data.get("i2");
			this._i2 = new I2Node(_i2);
		}
		
		
		public class FbNode {
			public String _description;
			
			public FbNode(Value _fb) {
				if(!_fb.get("description").isNull())
					this._description = _fb.get("description").asString();
			}
		}
		
		public class Fb_corr_0_inRel {
			public String __type_;
		
			public Fb_corr_0_inRel(Value _fb_corr_0_in) {
				if(!_fb_corr_0_in.get("_type_").isNull())
					this.__type_ = _fb_corr_0_in.get("_type_").asString();
			}
		}
		public class F1Node {
			public String _name;
			
			public F1Node(Value _f1) {
				if(!_f1.get("name").isNull())
					this._name = _f1.get("name").asString();
			}
		}
		
		public class F1_corr_0_i1Rel {
			public String __type_;
		
			public F1_corr_0_i1Rel(Value _f1_corr_0_i1) {
				if(!_f1_corr_0_i1.get("_type_").isNull())
					this.__type_ = _f1_corr_0_i1.get("_type_").asString();
			}
		}
		public class F2Node {
			public String _name;
			
			public F2Node(Value _f2) {
				if(!_f2.get("name").isNull())
					this._name = _f2.get("name").asString();
			}
		}
		
		public class F2_corr_0_i2Rel {
			public String __type_;
		
			public F2_corr_0_i2Rel(Value _f2_corr_0_i2) {
				if(!_f2_corr_0_i2.get("_type_").isNull())
					this.__type_ = _f2_corr_0_i2.get("_type_").asString();
			}
		}
		public class InNode {
			public String _description;
			
			public InNode(Value _in) {
				if(!_in.get("description").isNull())
					this._description = _in.get("description").asString();
			}
		}
		
		public class I1Node {
			public String _name;
			
			public I1Node(Value _i1) {
				if(!_i1.get("name").isNull())
					this._name = _i1.get("name").asString();
			}
		}
		
		public class I2Node {
			public String _name;
			
			public I2Node(Value _i2) {
				if(!_i2.get("name").isNull())
					this._name = _i2.get("name").asString();
			}
		}
		
	}
	
	public class ConsistentTripleCoData extends NeoData {
		public final FbNode _fb;
		public final Fb_corr_0_inRel _fb_corr_0_in;
		public final F1Node _f1;
		public final F1_corr_0_i1Rel _f1_corr_0_i1;
		public final F2Node _f2;
		public final F2_corr_0_i2Rel _f2_corr_0_i2;
		public final InNode _in;
		public final I1Node _i1;
		public final I2Node _i2;
	
		public ConsistentTripleCoData(Record data) {
			var _fb = data.get("fb");
			this._fb = new FbNode(_fb);
			var _fb_corr_0_in = data.get("fb_corr_0_in");
			this._fb_corr_0_in = new Fb_corr_0_inRel(_fb_corr_0_in);
			var _f1 = data.get("f1");
			this._f1 = new F1Node(_f1);
			var _f1_corr_0_i1 = data.get("f1_corr_0_i1");
			this._f1_corr_0_i1 = new F1_corr_0_i1Rel(_f1_corr_0_i1);
			var _f2 = data.get("f2");
			this._f2 = new F2Node(_f2);
			var _f2_corr_0_i2 = data.get("f2_corr_0_i2");
			this._f2_corr_0_i2 = new F2_corr_0_i2Rel(_f2_corr_0_i2);
			var _in = data.get("in");
			this._in = new InNode(_in);
			var _i1 = data.get("i1");
			this._i1 = new I1Node(_i1);
			var _i2 = data.get("i2");
			this._i2 = new I2Node(_i2);
		}
		
	
		public class FbNode {
			public String _description;
			
			public FbNode(Value _fb) {
				if(!_fb.get("description").isNull())
					this._description = _fb.get("description").asString();
			}
		}
		
		public class Fb_corr_0_inRel {
			public String __type_;
		
			public Fb_corr_0_inRel(Value _fb_corr_0_in) {
				if(!_fb_corr_0_in.get("_type_").isNull())
					this.__type_ = _fb_corr_0_in.get("_type_").asString();
			}
		}
		public class F1Node {
			public String _name;
			
			public F1Node(Value _f1) {
				if(!_f1.get("name").isNull())
					this._name = _f1.get("name").asString();
			}
		}
		
		public class F1_corr_0_i1Rel {
			public String __type_;
		
			public F1_corr_0_i1Rel(Value _f1_corr_0_i1) {
				if(!_f1_corr_0_i1.get("_type_").isNull())
					this.__type_ = _f1_corr_0_i1.get("_type_").asString();
			}
		}
		public class F2Node {
			public String _name;
			
			public F2Node(Value _f2) {
				if(!_f2.get("name").isNull())
					this._name = _f2.get("name").asString();
			}
		}
		
		public class F2_corr_0_i2Rel {
			public String __type_;
		
			public F2_corr_0_i2Rel(Value _f2_corr_0_i2) {
				if(!_f2_corr_0_i2.get("_type_").isNull())
					this.__type_ = _f2_corr_0_i2.get("_type_").asString();
			}
		}
		public class InNode {
			public String _description;
			
			public InNode(Value _in) {
				if(!_in.get("description").isNull())
					this._description = _in.get("description").asString();
			}
		}
		
		public class I1Node {
			public String _name;
			
			public I1Node(Value _i1) {
				if(!_i1.get("name").isNull())
					this._name = _i1.get("name").asString();
			}
		}
		
		public class I2Node {
			public String _name;
			
			public I2Node(Value _i2) {
				if(!_i2.get("name").isNull())
					this._name = _i2.get("name").asString();
			}
		}
		
	}
	
	public class ConsistentTripleMask extends NeoMask {
		public ConsistentTripleMask setFb(Long value) {
			nodeMask.put("fb", value);
			return this;
		}
		public ConsistentTripleMask setFbDescription(String value) {
			attributeMask.put("fb.description", value);
			return this;
		}
		public ConsistentTripleMask setFb_corr_0_in_type_(String value) {
			attributeMask.put("fb_corr_0_in._type_", value);
			return this;
		}
		public ConsistentTripleMask setF1(Long value) {
			nodeMask.put("f1", value);
			return this;
		}
		public ConsistentTripleMask setF1Name(String value) {
			attributeMask.put("f1.name", value);
			return this;
		}
		public ConsistentTripleMask setF1_corr_0_i1_type_(String value) {
			attributeMask.put("f1_corr_0_i1._type_", value);
			return this;
		}
		public ConsistentTripleMask setF2(Long value) {
			nodeMask.put("f2", value);
			return this;
		}
		public ConsistentTripleMask setF2Name(String value) {
			attributeMask.put("f2.name", value);
			return this;
		}
		public ConsistentTripleMask setF2_corr_0_i2_type_(String value) {
			attributeMask.put("f2_corr_0_i2._type_", value);
			return this;
		}
		public ConsistentTripleMask setIn(Long value) {
			nodeMask.put("in", value);
			return this;
		}
		public ConsistentTripleMask setInDescription(String value) {
			attributeMask.put("in.description", value);
			return this;
		}
		public ConsistentTripleMask setI1(Long value) {
			nodeMask.put("i1", value);
			return this;
		}
		public ConsistentTripleMask setI1Name(String value) {
			attributeMask.put("i1.name", value);
			return this;
		}
		public ConsistentTripleMask setI2(Long value) {
			nodeMask.put("i2", value);
			return this;
		}
		public ConsistentTripleMask setI2Name(String value) {
			attributeMask.put("i2.name", value);
			return this;
		}
	}
	
	//:~> platform:/resource/TestSuiteTGG/src/org/emoflon/neo/example/facebooktoinstagram/ModelForTesting.msl#//@entities.3
	public Model getModel_InConsistentTarget1(){
		return (Model) spec.getEntities().get(3);
	}
	
	//:~> platform:/resource/TestSuiteTGG/src/org/emoflon/neo/example/facebooktoinstagram/ModelForTesting.msl#//@entities.4
	public Model getModel_InConsistentSource1(){
		return (Model) spec.getEntities().get(4);
	}
	
	//:~> platform:/resource/TestSuiteTGG/src/org/emoflon/neo/example/facebooktoinstagram/ModelForTesting.msl#//@entities.5
	public Model getModel_InConsistentSource2(){
		return (Model) spec.getEntities().get(5);
	}
}

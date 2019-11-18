/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api;

import org.emoflon.neo.cypher.common.*;
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
import org.emoflon.neo.api.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_Facebook {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_Facebook(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_Facebook(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot){
		spec = (EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/FacebookToInstagramFASE/src/Facebook.msl", platformResourceURIRoot, platformPluginURIRoot);
		this.builder = builder;
	}

	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.0
	public Metamodel getMetamodel_FacebookLanguage(){
		return (Metamodel) spec.getEntities().get(0);
	}
	
	public static final String FacebookLanguage__Network = "FacebookLanguage__Network";
	public static final String FacebookLanguage__Friendship = "FacebookLanguage__Friendship";
	public static final String FacebookLanguage__User = "FacebookLanguage__User";
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.1
	public IConstraint getConstraint_NoDoubleFaceBookUsers() {
		var c = (Constraint) spec.getEntities().get(1);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.2
	public DoubleFaceBookUsersAccess getPattern_DoubleFaceBookUsers() {
		return new DoubleFaceBookUsersAccess();
	}
	
	public class DoubleFaceBookUsersAccess extends NeoPatternAccess<DoubleFaceBookUsersData,DoubleFaceBookUsersMask> {
		public final String n = "n";
		public final String u = "u";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(2);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<DoubleFaceBookUsersData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new DoubleFaceBookUsersData(d));
		}
		
		@Override
		public DoubleFaceBookUsersMask mask() {
			return new DoubleFaceBookUsersMask();
		}
	}
	
	public class DoubleFaceBookUsersData extends NeoData {
		public final NNode n;
		public final N_users_0_uRel n_users_0_u;
		public final N_users_1_uRel n_users_1_u;
		public final UNode u;
		
		public DoubleFaceBookUsersData(Record data) {
			var n = data.get("n");
			this.n = new NNode(n);
			var n_users_0_u = data.get("n_users_0_u");
			this.n_users_0_u = new N_users_0_uRel(n_users_0_u);
			var n_users_1_u = data.get("n_users_1_u");
			this.n_users_1_u = new N_users_1_uRel(n_users_1_u);
			var u = data.get("u");
			this.u = new UNode(u);
		}
		
		
		public class NNode {
			public String description;
			
			public NNode(Value n) {
				if(!n.get("description").isNull())
					this.description = n.get("description").asString();
			}
		}
		
		public class N_users_0_uRel {
		
			public N_users_0_uRel(Value n_users_0_u) {
			}
		}
		public class N_users_1_uRel {
		
			public N_users_1_uRel(Value n_users_1_u) {
			}
		}
		public class UNode {
			public String name;
			
			public UNode(Value u) {
				if(!u.get("name").isNull())
					this.name = u.get("name").asString();
			}
		}
		
	}
	
	public class DoubleFaceBookUsersMask extends NeoMask {
		public DoubleFaceBookUsersMask setN(Long value) {
			nodeMask.put("n", value);
			return this;
		}
		public DoubleFaceBookUsersMask setNDescription(String value) {
			attributeMask.put("n.description", value);
			return this;
		}
		public DoubleFaceBookUsersMask setU(Long value) {
			nodeMask.put("u", value);
			return this;
		}
		public DoubleFaceBookUsersMask setUName(String value) {
			attributeMask.put("u.name", value);
			return this;
		}
	
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.3
	public IConstraint getConstraint_NoDoubleFriendship() {
		var c = (Constraint) spec.getEntities().get(3);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.4
	public DoubleFriendshipAccess getPattern_DoubleFriendship() {
		return new DoubleFriendshipAccess();
	}
	
	public class DoubleFriendshipAccess extends NeoPatternAccess<DoubleFriendshipData,DoubleFriendshipMask> {
		public final String n = "n";
		public final String f1 = "f1";
		public final String f2 = "f2";
		public final String u1 = "u1";
		public final String u2 = "u2";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(4);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<DoubleFriendshipData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new DoubleFriendshipData(d));
		}
		
		@Override
		public DoubleFriendshipMask mask() {
			return new DoubleFriendshipMask();
		}
	}
	
	public class DoubleFriendshipData extends NeoData {
		public final NNode n;
		public final N_friendships_0_f1Rel n_friendships_0_f1;
		public final N_friendships_1_f2Rel n_friendships_1_f2;
		public final N_users_2_u1Rel n_users_2_u1;
		public final N_users_3_u2Rel n_users_3_u2;
		public final F1Node f1;
		public final F1_friends_0_u1Rel f1_friends_0_u1;
		public final F1_friends_1_u2Rel f1_friends_1_u2;
		public final F2Node f2;
		public final F2_friends_0_u1Rel f2_friends_0_u1;
		public final F2_friends_1_u2Rel f2_friends_1_u2;
		public final U1Node u1;
		public final U2Node u2;
		
		public DoubleFriendshipData(Record data) {
			var n = data.get("n");
			this.n = new NNode(n);
			var n_friendships_0_f1 = data.get("n_friendships_0_f1");
			this.n_friendships_0_f1 = new N_friendships_0_f1Rel(n_friendships_0_f1);
			var n_friendships_1_f2 = data.get("n_friendships_1_f2");
			this.n_friendships_1_f2 = new N_friendships_1_f2Rel(n_friendships_1_f2);
			var n_users_2_u1 = data.get("n_users_2_u1");
			this.n_users_2_u1 = new N_users_2_u1Rel(n_users_2_u1);
			var n_users_3_u2 = data.get("n_users_3_u2");
			this.n_users_3_u2 = new N_users_3_u2Rel(n_users_3_u2);
			var f1 = data.get("f1");
			this.f1 = new F1Node(f1);
			var f1_friends_0_u1 = data.get("f1_friends_0_u1");
			this.f1_friends_0_u1 = new F1_friends_0_u1Rel(f1_friends_0_u1);
			var f1_friends_1_u2 = data.get("f1_friends_1_u2");
			this.f1_friends_1_u2 = new F1_friends_1_u2Rel(f1_friends_1_u2);
			var f2 = data.get("f2");
			this.f2 = new F2Node(f2);
			var f2_friends_0_u1 = data.get("f2_friends_0_u1");
			this.f2_friends_0_u1 = new F2_friends_0_u1Rel(f2_friends_0_u1);
			var f2_friends_1_u2 = data.get("f2_friends_1_u2");
			this.f2_friends_1_u2 = new F2_friends_1_u2Rel(f2_friends_1_u2);
			var u1 = data.get("u1");
			this.u1 = new U1Node(u1);
			var u2 = data.get("u2");
			this.u2 = new U2Node(u2);
		}
		
		
		public class NNode {
			public String description;
			
			public NNode(Value n) {
				if(!n.get("description").isNull())
					this.description = n.get("description").asString();
			}
		}
		
		public class N_friendships_0_f1Rel {
		
			public N_friendships_0_f1Rel(Value n_friendships_0_f1) {
			}
		}
		public class N_friendships_1_f2Rel {
		
			public N_friendships_1_f2Rel(Value n_friendships_1_f2) {
			}
		}
		public class N_users_2_u1Rel {
		
			public N_users_2_u1Rel(Value n_users_2_u1) {
			}
		}
		public class N_users_3_u2Rel {
		
			public N_users_3_u2Rel(Value n_users_3_u2) {
			}
		}
		public class F1Node {
			public LocalDate startFrom;
			
			public F1Node(Value f1) {
				if(!f1.get("startFrom").isNull())
					this.startFrom = f1.get("startFrom").asLocalDate();
			}
		}
		
		public class F1_friends_0_u1Rel {
		
			public F1_friends_0_u1Rel(Value f1_friends_0_u1) {
			}
		}
		public class F1_friends_1_u2Rel {
		
			public F1_friends_1_u2Rel(Value f1_friends_1_u2) {
			}
		}
		public class F2Node {
			public LocalDate startFrom;
			
			public F2Node(Value f2) {
				if(!f2.get("startFrom").isNull())
					this.startFrom = f2.get("startFrom").asLocalDate();
			}
		}
		
		public class F2_friends_0_u1Rel {
		
			public F2_friends_0_u1Rel(Value f2_friends_0_u1) {
			}
		}
		public class F2_friends_1_u2Rel {
		
			public F2_friends_1_u2Rel(Value f2_friends_1_u2) {
			}
		}
		public class U1Node {
			public String name;
			
			public U1Node(Value u1) {
				if(!u1.get("name").isNull())
					this.name = u1.get("name").asString();
			}
		}
		
		public class U2Node {
			public String name;
			
			public U2Node(Value u2) {
				if(!u2.get("name").isNull())
					this.name = u2.get("name").asString();
			}
		}
		
	}
	
	public class DoubleFriendshipMask extends NeoMask {
		public DoubleFriendshipMask setN(Long value) {
			nodeMask.put("n", value);
			return this;
		}
		public DoubleFriendshipMask setNDescription(String value) {
			attributeMask.put("n.description", value);
			return this;
		}
		public DoubleFriendshipMask setF1(Long value) {
			nodeMask.put("f1", value);
			return this;
		}
		public DoubleFriendshipMask setF1StartFrom(LocalDate value) {
			attributeMask.put("f1.startFrom", value);
			return this;
		}
		public DoubleFriendshipMask setF2(Long value) {
			nodeMask.put("f2", value);
			return this;
		}
		public DoubleFriendshipMask setF2StartFrom(LocalDate value) {
			attributeMask.put("f2.startFrom", value);
			return this;
		}
		public DoubleFriendshipMask setU1(Long value) {
			nodeMask.put("u1", value);
			return this;
		}
		public DoubleFriendshipMask setU1Name(String value) {
			attributeMask.put("u1.name", value);
			return this;
		}
		public DoubleFriendshipMask setU2(Long value) {
			nodeMask.put("u2", value);
			return this;
		}
		public DoubleFriendshipMask setU2Name(String value) {
			attributeMask.put("u2.name", value);
			return this;
		}
	
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.5
	public IConstraint getConstraint_NoInterFriendship() {
		var c = (Constraint) spec.getEntities().get(5);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.6
	public InterFriendshipAccess getPattern_InterFriendship() {
		return new InterFriendshipAccess();
	}
	
	public class InterFriendshipAccess extends NeoPatternAccess<InterFriendshipData,InterFriendshipMask> {
		public final String n1 = "n1";
		public final String n2 = "n2";
		public final String f1 = "f1";
		public final String u1 = "u1";
		public final String u2 = "u2";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(6);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<InterFriendshipData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InterFriendshipData(d));
		}
		
		@Override
		public InterFriendshipMask mask() {
			return new InterFriendshipMask();
		}
	}
	
	public class InterFriendshipData extends NeoData {
		public final N1Node n1;
		public final N1_users_0_u1Rel n1_users_0_u1;
		public final N2Node n2;
		public final N2_users_0_u2Rel n2_users_0_u2;
		public final F1Node f1;
		public final F1_friends_0_u1Rel f1_friends_0_u1;
		public final F1_friends_1_u2Rel f1_friends_1_u2;
		public final U1Node u1;
		public final U2Node u2;
		
		public InterFriendshipData(Record data) {
			var n1 = data.get("n1");
			this.n1 = new N1Node(n1);
			var n1_users_0_u1 = data.get("n1_users_0_u1");
			this.n1_users_0_u1 = new N1_users_0_u1Rel(n1_users_0_u1);
			var n2 = data.get("n2");
			this.n2 = new N2Node(n2);
			var n2_users_0_u2 = data.get("n2_users_0_u2");
			this.n2_users_0_u2 = new N2_users_0_u2Rel(n2_users_0_u2);
			var f1 = data.get("f1");
			this.f1 = new F1Node(f1);
			var f1_friends_0_u1 = data.get("f1_friends_0_u1");
			this.f1_friends_0_u1 = new F1_friends_0_u1Rel(f1_friends_0_u1);
			var f1_friends_1_u2 = data.get("f1_friends_1_u2");
			this.f1_friends_1_u2 = new F1_friends_1_u2Rel(f1_friends_1_u2);
			var u1 = data.get("u1");
			this.u1 = new U1Node(u1);
			var u2 = data.get("u2");
			this.u2 = new U2Node(u2);
		}
		
		
		public class N1Node {
			public String description;
			
			public N1Node(Value n1) {
				if(!n1.get("description").isNull())
					this.description = n1.get("description").asString();
			}
		}
		
		public class N1_users_0_u1Rel {
		
			public N1_users_0_u1Rel(Value n1_users_0_u1) {
			}
		}
		public class N2Node {
			public String description;
			
			public N2Node(Value n2) {
				if(!n2.get("description").isNull())
					this.description = n2.get("description").asString();
			}
		}
		
		public class N2_users_0_u2Rel {
		
			public N2_users_0_u2Rel(Value n2_users_0_u2) {
			}
		}
		public class F1Node {
			public LocalDate startFrom;
			
			public F1Node(Value f1) {
				if(!f1.get("startFrom").isNull())
					this.startFrom = f1.get("startFrom").asLocalDate();
			}
		}
		
		public class F1_friends_0_u1Rel {
		
			public F1_friends_0_u1Rel(Value f1_friends_0_u1) {
			}
		}
		public class F1_friends_1_u2Rel {
		
			public F1_friends_1_u2Rel(Value f1_friends_1_u2) {
			}
		}
		public class U1Node {
			public String name;
			
			public U1Node(Value u1) {
				if(!u1.get("name").isNull())
					this.name = u1.get("name").asString();
			}
		}
		
		public class U2Node {
			public String name;
			
			public U2Node(Value u2) {
				if(!u2.get("name").isNull())
					this.name = u2.get("name").asString();
			}
		}
		
	}
	
	public class InterFriendshipMask extends NeoMask {
		public InterFriendshipMask setN1(Long value) {
			nodeMask.put("n1", value);
			return this;
		}
		public InterFriendshipMask setN1Description(String value) {
			attributeMask.put("n1.description", value);
			return this;
		}
		public InterFriendshipMask setN2(Long value) {
			nodeMask.put("n2", value);
			return this;
		}
		public InterFriendshipMask setN2Description(String value) {
			attributeMask.put("n2.description", value);
			return this;
		}
		public InterFriendshipMask setF1(Long value) {
			nodeMask.put("f1", value);
			return this;
		}
		public InterFriendshipMask setF1StartFrom(LocalDate value) {
			attributeMask.put("f1.startFrom", value);
			return this;
		}
		public InterFriendshipMask setU1(Long value) {
			nodeMask.put("u1", value);
			return this;
		}
		public InterFriendshipMask setU1Name(String value) {
			attributeMask.put("u1.name", value);
			return this;
		}
		public InterFriendshipMask setU2(Long value) {
			nodeMask.put("u2", value);
			return this;
		}
		public InterFriendshipMask setU2Name(String value) {
			attributeMask.put("u2.name", value);
			return this;
		}
	
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.7
	public IConstraint getConstraint_NoDoubleParents() {
		var c = (Constraint) spec.getEntities().get(7);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.8
	public DoubleParentsAccess getPattern_DoubleParents() {
		return new DoubleParentsAccess();
	}
	
	public class DoubleParentsAccess extends NeoPatternAccess<DoubleParentsData,DoubleParentsMask> {
		public final String u1 = "u1";
		public final String u2 = "u2";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(8);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<DoubleParentsData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new DoubleParentsData(d));
		}
		
		@Override
		public DoubleParentsMask mask() {
			return new DoubleParentsMask();
		}
	}
	
	public class DoubleParentsData extends NeoData {
		public final U1Node u1;
		public final U1_parents_0_u2Rel u1_parents_0_u2;
		public final U1_parents_1_u2Rel u1_parents_1_u2;
		public final U2Node u2;
		
		public DoubleParentsData(Record data) {
			var u1 = data.get("u1");
			this.u1 = new U1Node(u1);
			var u1_parents_0_u2 = data.get("u1_parents_0_u2");
			this.u1_parents_0_u2 = new U1_parents_0_u2Rel(u1_parents_0_u2);
			var u1_parents_1_u2 = data.get("u1_parents_1_u2");
			this.u1_parents_1_u2 = new U1_parents_1_u2Rel(u1_parents_1_u2);
			var u2 = data.get("u2");
			this.u2 = new U2Node(u2);
		}
		
		
		public class U1Node {
			public String name;
			
			public U1Node(Value u1) {
				if(!u1.get("name").isNull())
					this.name = u1.get("name").asString();
			}
		}
		
		public class U1_parents_0_u2Rel {
		
			public U1_parents_0_u2Rel(Value u1_parents_0_u2) {
			}
		}
		public class U1_parents_1_u2Rel {
		
			public U1_parents_1_u2Rel(Value u1_parents_1_u2) {
			}
		}
		public class U2Node {
			public String name;
			
			public U2Node(Value u2) {
				if(!u2.get("name").isNull())
					this.name = u2.get("name").asString();
			}
		}
		
	}
	
	public class DoubleParentsMask extends NeoMask {
		public DoubleParentsMask setU1(Long value) {
			nodeMask.put("u1", value);
			return this;
		}
		public DoubleParentsMask setU1Name(String value) {
			attributeMask.put("u1.name", value);
			return this;
		}
		public DoubleParentsMask setU2(Long value) {
			nodeMask.put("u2", value);
			return this;
		}
		public DoubleParentsMask setU2Name(String value) {
			attributeMask.put("u2.name", value);
			return this;
		}
	
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.9
	public IConstraint getConstraint_NoDoubleSibling() {
		var c = (Constraint) spec.getEntities().get(9);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.10
	public DoubleSiblingAccess getPattern_DoubleSibling() {
		return new DoubleSiblingAccess();
	}
	
	public class DoubleSiblingAccess extends NeoPatternAccess<DoubleSiblingData,DoubleSiblingMask> {
		public final String u1 = "u1";
		public final String u2 = "u2";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(10);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<DoubleSiblingData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new DoubleSiblingData(d));
		}
		
		@Override
		public DoubleSiblingMask mask() {
			return new DoubleSiblingMask();
		}
	}
	
	public class DoubleSiblingData extends NeoData {
		public final U1Node u1;
		public final U1_siblings_0_u2Rel u1_siblings_0_u2;
		public final U1_siblings_1_u2Rel u1_siblings_1_u2;
		public final U2Node u2;
		
		public DoubleSiblingData(Record data) {
			var u1 = data.get("u1");
			this.u1 = new U1Node(u1);
			var u1_siblings_0_u2 = data.get("u1_siblings_0_u2");
			this.u1_siblings_0_u2 = new U1_siblings_0_u2Rel(u1_siblings_0_u2);
			var u1_siblings_1_u2 = data.get("u1_siblings_1_u2");
			this.u1_siblings_1_u2 = new U1_siblings_1_u2Rel(u1_siblings_1_u2);
			var u2 = data.get("u2");
			this.u2 = new U2Node(u2);
		}
		
		
		public class U1Node {
			public String name;
			
			public U1Node(Value u1) {
				if(!u1.get("name").isNull())
					this.name = u1.get("name").asString();
			}
		}
		
		public class U1_siblings_0_u2Rel {
		
			public U1_siblings_0_u2Rel(Value u1_siblings_0_u2) {
			}
		}
		public class U1_siblings_1_u2Rel {
		
			public U1_siblings_1_u2Rel(Value u1_siblings_1_u2) {
			}
		}
		public class U2Node {
			public String name;
			
			public U2Node(Value u2) {
				if(!u2.get("name").isNull())
					this.name = u2.get("name").asString();
			}
		}
		
	}
	
	public class DoubleSiblingMask extends NeoMask {
		public DoubleSiblingMask setU1(Long value) {
			nodeMask.put("u1", value);
			return this;
		}
		public DoubleSiblingMask setU1Name(String value) {
			attributeMask.put("u1.name", value);
			return this;
		}
		public DoubleSiblingMask setU2(Long value) {
			nodeMask.put("u2", value);
			return this;
		}
		public DoubleSiblingMask setU2Name(String value) {
			attributeMask.put("u2.name", value);
			return this;
		}
	
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.11
	public IConstraint getConstraint_NoDoubleSpouses() {
		var c = (Constraint) spec.getEntities().get(11);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/FacebookToInstagramFASE/src/Facebook.msl#//@entities.12
	public DoubleSpousesAccess getPattern_DoubleSpouses() {
		return new DoubleSpousesAccess();
	}
	
	public class DoubleSpousesAccess extends NeoPatternAccess<DoubleSpousesData,DoubleSpousesMask> {
		public final String u1 = "u1";
		public final String u2 = "u2";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(12);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<DoubleSpousesData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new DoubleSpousesData(d));
		}
		
		@Override
		public DoubleSpousesMask mask() {
			return new DoubleSpousesMask();
		}
	}
	
	public class DoubleSpousesData extends NeoData {
		public final U1Node u1;
		public final U1_spouse_0_u2Rel u1_spouse_0_u2;
		public final U1_spouse_1_u2Rel u1_spouse_1_u2;
		public final U2Node u2;
		
		public DoubleSpousesData(Record data) {
			var u1 = data.get("u1");
			this.u1 = new U1Node(u1);
			var u1_spouse_0_u2 = data.get("u1_spouse_0_u2");
			this.u1_spouse_0_u2 = new U1_spouse_0_u2Rel(u1_spouse_0_u2);
			var u1_spouse_1_u2 = data.get("u1_spouse_1_u2");
			this.u1_spouse_1_u2 = new U1_spouse_1_u2Rel(u1_spouse_1_u2);
			var u2 = data.get("u2");
			this.u2 = new U2Node(u2);
		}
		
		
		public class U1Node {
			public String name;
			
			public U1Node(Value u1) {
				if(!u1.get("name").isNull())
					this.name = u1.get("name").asString();
			}
		}
		
		public class U1_spouse_0_u2Rel {
		
			public U1_spouse_0_u2Rel(Value u1_spouse_0_u2) {
			}
		}
		public class U1_spouse_1_u2Rel {
		
			public U1_spouse_1_u2Rel(Value u1_spouse_1_u2) {
			}
		}
		public class U2Node {
			public String name;
			
			public U2Node(Value u2) {
				if(!u2.get("name").isNull())
					this.name = u2.get("name").asString();
			}
		}
		
	}
	
	public class DoubleSpousesMask extends NeoMask {
		public DoubleSpousesMask setU1(Long value) {
			nodeMask.put("u1", value);
			return this;
		}
		public DoubleSpousesMask setU1Name(String value) {
			attributeMask.put("u1.name", value);
			return this;
		}
		public DoubleSpousesMask setU2(Long value) {
			nodeMask.put("u2", value);
			return this;
		}
		public DoubleSpousesMask setU2Name(String value) {
			attributeMask.put("u2.name", value);
			return this;
		}
	
	}
}
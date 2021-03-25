package org.emoflon.ibex.neo.benchmark.exttype2doc.shortCut;

import org.emoflon.ibex.neo.benchmark.util.BenchParameters;
import org.emoflon.ibex.neo.benchmark.util.ScaleOrientation;

public class ExtType2Doc_ShortCut_Params extends BenchParameters {

	public final int num_of_root_packages;
	public final int[] horizontal_package_scales;
	public final boolean[] types_for_packages;

	public final int num_of_root_types;
	public final int type_inheritance_depth;
	public final int horizontal_type_inheritance_scale;
	public final int num_of_fields;
	public final int num_of_methods;
	public final int num_of_parameters;

	public final int num_of_conflicts;

	public ExtType2Doc_ShortCut_Params(String name, int modelScale, ScaleOrientation scaleOrientation, int numOfChanges) {
		super(name, modelScale, scaleOrientation, numOfChanges);

		switch (scaleOrientation) {
		case HORIZONTAL:
			num_of_root_packages = modelScale;
			break;
		case VERTICAL:
			num_of_root_packages = 1;
			break;
		default:
			num_of_root_packages = -1;
			break;
		}

		horizontal_package_scales = new int[] { 3, 1, 2 };
		types_for_packages = new boolean[] { false, false, true };

		num_of_root_types = 3;
		type_inheritance_depth = 3;
		horizontal_type_inheritance_scale = 3;
		num_of_fields = 3;
		num_of_methods = 3;
		num_of_parameters = 2;

		num_of_conflicts = numOfChanges;
	}

}

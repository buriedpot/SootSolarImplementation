/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2014 Raja Vallee-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

package nju.lab.tool.soot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * add args which soot need to use
 */
public abstract class SootAna {

	protected List<String> getArgs(String[] jarFilePaths) {
		List<String> argsList = new ArrayList<String>();
		addClassPath(argsList, jarFilePaths);
		if(argsList.size()==0) {//this class can't analysis
			return argsList;
		}

		addGenArg(argsList);
		addCgArgs(argsList);
		addIgrArgs(argsList);
		return argsList;
	}
	protected static void addSparkCgArgs(List<String> argsList) {
		argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "on", }));
		argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "all-reachable:false"}));
		argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "implicit-entry:true"}));
		argsList.addAll(Arrays.asList(new String[] {"-p", "cg.cha", "enabled:false"}));
		argsList.addAll(Arrays.asList(new String[] {"-p", "cg.spark", "enabled:true"}));
		argsList.addAll(Arrays.asList(new String[] {"-p", "cg.spark", "apponly:true"}));
		argsList.addAll(Arrays.asList(new String[] {"-p", "cg.spark", "propagator:solar"}));
//		argsList.addAll(Arrays.asList(new String[] {"-p", "wjpp.myTrans", "on"}));
		//argsList.addAll(Arrays.asList(new String[] {"-p", "cg.myTrans", "on"}));
		//argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "specify-entrypoints:true"}));
		// CallGraphPack
	}
	protected abstract void addCgArgs(List<String> argsList);

	protected void addClassPath(List<String> argsList, String[] jarFilePaths) {
		for (String jarFilePath : jarFilePaths) {
			if (new File(jarFilePath).exists()) {
				if (canAna(jarFilePath)) {
					argsList.add("-process-dir");
					argsList.add(jarFilePath);
				}else {
				}
			} else {
			}

		}
	}

	protected boolean canAna(String jarFilePath) {
//		return true;
		if(jarFilePath.contains("\\asm\\")&&jarFilePath.contains("6")) {
			return false;
		}
		return true;
	}

	protected void addGenArg(List<String> argsList) {


		argsList.add("-ire");
		//argsList.add("--app");
		argsList.add("-allow-phantom-refs");
		//argsList.add("-allow-phantom-refs");
		argsList.add("-allow-phantom-elms");
		argsList.add("-no-writeout-body-releasing");
		argsList.add("-w");

	}

	protected void addIgrArgs(List<String> argsList) {
		argsList.addAll(Arrays.asList(new String[] { "-p", "wjop", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "wjap", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "jtp", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "jop", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "jap", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "bb", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "tag", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-f", "n", }));//no output
	}
}

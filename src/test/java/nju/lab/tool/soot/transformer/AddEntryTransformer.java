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

package nju.lab.tool.soot.transformer;

import soot.EntryPoints;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddEntryTransformer extends SceneTransformer {
    List<SootMethod> specified_entrys;
    List<String> specified_sootMethods;
    AddEntryTransformer() {
        specified_sootMethods = new ArrayList<>();
    }
    AddEntryTransformer(List<String> specified_entrys) {
        specified_sootMethods = specified_entrys;
        if (specified_entrys == null) {
            specified_sootMethods = new ArrayList<>();
        }
    }
    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        specified_entrys = new ArrayList<>();
        for (String sig : specified_sootMethods) {
            for (SootMethod appMthd : EntryPoints.v().methodsOfApplicationClasses()) {
                if (sig.equals(appMthd.getSignature())) {
                    this.specified_entrys.add(appMthd);
                }
            }

        }
        Scene.v().setEntryPoints(specified_entrys);
    }
}

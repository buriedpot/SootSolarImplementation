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

package soot.jimple.spark.solver.myreflection;


import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.spark.solver.Propagator;

public class MyReflectionAnalysis {

    private Propagator solver;


    private MyReflectionModel model;


    public MyReflectionAnalysis setPropagator(Propagator solver) {
        this.solver = solver;
        return this;
    }

    public MyReflectionAnalysis() {
        this.model = new MyReflectionModel();
    }

    public void onNewPointsToSet(VarNode varNode, PointsToSetInternal pts) {
        if (model.isRelevantVar(varNode)) {
            model.handleNewPointsToSet(varNode, pts);
        }
    }





}

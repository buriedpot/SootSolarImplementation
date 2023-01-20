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

package soot.jimple.spark.solver.myreflection.utils;

import soot.ArrayType;
import soot.Scene;
import soot.SootClass;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.ClassConstantNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.StringConstantNode;
import soot.jimple.spark.solver.myreflection.MyReflectionModel;

public class CSObjs {

    public static String toString(Node obj) {
        if (obj instanceof StringConstantNode) {
            return ((StringConstantNode) obj).getString();
        }
        else {
            return null;
        }
    }

    public static SootClass toClass(Node obj) {
        if (obj instanceof ClassConstantNode) {
            return Scene.v().getSootClassUnsafe(((ClassConstantNode) obj).getClassConstant().value);
        }

        return null;
    }


    public static boolean isUnknownClassObj(Node n) {
        return n instanceof StringConstantNode && ((StringConstantNode) n).getString().equals(MyReflectionModel.UNKNOWN_CLASS);
    }

    public static boolean isArrayAllocNode(Node n) {
        return n instanceof AllocNode && n.getType() instanceof ArrayType;

    }

    public static int getArrayLength(Node n) {
        if (!isArrayAllocNode(n)) {
            return -1;
        }
        AllocNode n1 = (AllocNode) n;
        JNewArrayExpr newExpr = (JNewArrayExpr) n1.getNewExpr();
        IntConstant size = (IntConstant) newExpr.getSize();
        return size.value;
    }
}

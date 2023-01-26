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

package nju.lab.tool.test;

import nju.lab.tool.soot.CgAna;
import soot.jimple.toolkits.callgraph.CallGraph;

public class SparkMain {
    public static void main(String[] args) {
        //任意取消掉其中一行的注释，可在控制台查看调用图打印结果
        // 行尾not pass代表此测试用例没有通过
        CallGraph result = CgAna.getInstance().generateCallGraphSpark(SparkMain.class.getClassLoader().getResource("myreflection/Inheritance").getPath());
        System.out.println(result);

        result = CgAna.getInstance().generateCallGraphSpark(SparkMain.class.getClassLoader().getResource("myreflection/DuplicateName").getPath());
        System.out.println(result);

        result = CgAna.getInstance().generateCallGraphSpark(SparkMain.class.getClassLoader().getResource("myreflection/ArgsRefine").getPath());
        System.out.println(result);

        result = CgAna.getInstance().generateCallGraphSpark(SparkMain.class.getClassLoader().getResource("myreflection/Basic").getPath());
        System.out.println(result);

//        CallGraph result = CgAna.getInstance().generateCallGraphSpark(SparkMain.class.getClassLoader().getResource("myreflection/UnknownClassName").getPath()); // not pass
//        CallGraph result = CgAna.getInstance().generateCallGraphSpark(SparkMain.class.getClassLoader().getResource("myreflection/RecvType").getPath()); // nott pass

        // result.get("org.openscience.cdk.config.Isotopes")
    }
}

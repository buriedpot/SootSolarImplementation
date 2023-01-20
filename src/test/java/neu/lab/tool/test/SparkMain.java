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

package neu.lab.tool.test;

import neu.lab.tool.soot.CgAna;
import soot.jimple.toolkits.callgraph.CallGraph;

public class SparkMain {
    public static void main(String[] args) {

        CallGraph result = CgAna.getInstance().generateCallGraphSpark("C:\\Works\\javaworks\\soot\\src\\test\\resources\\myreflection\\Basic");
//                        .deconstruct("C:\\Users\\buriedpot\\.m2\\repository\\org\\openscience\\cdk\\cdk-core\\2.7.1\\cdk-core-2.7.1.jar");
        System.out.println(result);
        // result.get("org.openscience.cdk.config.Isotopes")
    }
}

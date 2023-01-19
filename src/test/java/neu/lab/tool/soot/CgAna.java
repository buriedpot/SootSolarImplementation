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


package neu.lab.tool.soot;

import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.util.ArrayList;
import java.util.List;

public class CgAna extends SootAna {
    private static class CgAnaHolder {
        private static final CgAna INSTANCE = new CgAna();
    }

    private CgAna() {
    }

    public static CgAna getInstance() {
        return CgAnaHolder.INSTANCE;
    }


    public CallGraph generateCallGraphSpark(List<String> jarFilePaths) {
        ArrayList<String> sootArgs = new ArrayList<>();
        addGenArg(sootArgs);
        addIgrArgs(sootArgs);
        addSparkCgArgs(sootArgs);

        jarFilePaths.forEach(jarFilePath -> {
            sootArgs.add("-process-dir");
            sootArgs.add(jarFilePath);
        });
        soot.Main.main(sootArgs.toArray(new String[0]));
        CallGraph callGraph = Scene.v().getCallGraph();
        soot.G.reset();
        return callGraph;

    }

    public CallGraph generateCallGraphSpark(String jarFilePath) {

        List<String> jarFilePaths = new ArrayList<>();
        jarFilePaths.add(jarFilePath);
        return generateCallGraphSpark(jarFilePaths);

    }

    @Override
    protected void addCgArgs(List<String> argsList) {

    }
}

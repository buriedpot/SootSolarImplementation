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

import soot.*;
import soot.jimple.ClassConstant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.spark.solver.PropSolar;
import soot.jimple.spark.solver.myreflection.utils.CSObjs;
import soot.jimple.toolkits.callgraph.ReflectionModel;
import soot.tagkit.Host;
import soot.tagkit.LinkTag;
import soot.tagkit.Tag;
import soot.util.HashMultiMap;
import soot.util.MultiMap;
import soot.util.queue.QueueReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyReflectionModel implements ReflectionModel {

    protected static final int BASE = -1;

    public static final String UNKNOWN_CLASS = "#UnknownClass";

    public PAG pag;

    public PropSolar propagator;


    protected final Map<SootMethod, int[]> relevantVarIndexes = new HashMap<>();

    protected final MultiMap<VarNode, Stmt> relevantVars
            = new HashMultiMap<>();

    protected void registerRelevantVarIndexes(SootMethod api, int... indexes) {
        relevantVarIndexes.put(api, indexes);
    }

    public void handleAllInvokes() {
        QueueReader<MethodOrMethodContext> listener = Scene.v().getReachableMethods().listener();
        while (listener.hasNext()) {
            MethodOrMethodContext next = listener.next();
            SootMethod currentMethod = next.method();
            currentMethod.retrieveActiveBody().getUnits().stream()
                    .filter(unit -> (unit instanceof JInvokeStmt || unit instanceof JAssignStmt))
                    .forEach(unit -> {
                        handleInvoke(currentMethod, (Stmt) unit);
                    });
        }
    }




    protected void registerVarAndHandler() {
        SootMethod classForName = Scene.v().getMethod("<java.lang.Class: java.lang.Class forName(java.lang.String)>");
        registerRelevantVarIndexes(classForName, 0);
    }

    private void registerAPIHandler(SootMethod classForName, Object o) {

    }

    /**
     * 处理反射相关的函数调用包括forName等
     *
     * @param container 容器
     * @param stmt      支撑
     */
    public void handleInvoke(SootMethod container, Stmt stmt) {
        InvokeExpr invokeExpr = null;
        // 没有左值的函数调用语句
        if (stmt instanceof JInvokeStmt) {
            invokeExpr = stmt.getInvokeExpr();
        }
        // 有左值的函数调用语句
        else if (stmt instanceof JAssignStmt) {
            Value rightOp = ((JAssignStmt) stmt).getRightOp();
            if (rightOp instanceof InvokeExpr) {
                invokeExpr = (InvokeExpr) rightOp;
            }
        }
        if (invokeExpr == null) {
            return;
        }
        SootMethod target = invokeExpr.getMethod();
        if (target != null) {
            int [] indexes = relevantVarIndexes.get(target);
            if (indexes != null) {
                for (int i : indexes) {
                    relevantVars.put(getArg(container, invokeExpr, i), stmt);
                }
            }
        }
    }


    /**
     * 得到参数
     * For invocation r = v.foo(a0, a1, ..., an);
     * when points-to set of v or any ai (0 <= i <= n) changes,
     * this convenient method returns points-to sets relevant arguments.
     * For case v/ai == csVar.getVar(), this method returns pts,
     * otherwise, it just returns current points-to set of v/ai.
     * 获取一个方法调用中v a0、、an其中一份的pointToSet
     *
     * @param pts        changed part of csVar
     * @param indexes    indexes of the relevant arguments
     * @param container  容器
     * @param varNode    var节点
     * @param invokeExpr 调用expr
     * @return {@link List}<{@link PointsToSet}>
     */
    protected List<PointsToSet> getArgs(
            SootMethod container, VarNode varNode, PointsToSet pts, InvokeExpr invokeExpr, int... indexes) {
        List<PointsToSet> args = new ArrayList<>(indexes.length);
        for (int i : indexes) {
            VarNode arg = getArg(container, invokeExpr, i);
            if (arg.equals(varNode)) {
                args.add(pts);
            } else {
                // CSVar csArg = csManager.getCSVar(csVar.getContext(), arg);
                args.add(arg.makeP2Set());
            }
        }
        return args;
    }

    private VarNode getArg(SootMethod container, InvokeExpr invokeExpr, int i) {
        Value value = (i == BASE) ?
                ((InstanceInvokeExpr) invokeExpr).getBase() :
                invokeExpr.getArg(i);
        return pag.makeLocalVarNode(value, value.getType(), container);
    }

    @Override
    public void methodInvoke(SootMethod container, Stmt invokeStmt) {

    }


    /**
     * 方法调用
     *
     * @param container 容器
     * @param varNode   var节点
     * @param pts       分
     * @param stmt      可能是JInvokeStmt也可能是JAssignStmt
     */
    public void methodInvoke(SootMethod container, VarNode varNode, PointsToSet pts, Stmt stmt) {

    }

    @Override
    public void classNewInstance(SootMethod source, Stmt s) {

    }

    @Override
    public void contructorNewInstance(SootMethod source, Stmt s) {

    }

    @Override
    public void classForName(SootMethod source, Stmt s) {

    }


    /**
     * 类名称
     *
     * @param varNode pointer
     * @param pts     pointstoset
     * @param stmt    JAssignStmt or JInvokeStmt
     */
    public void classForName(VarNode varNode, PointsToSetInternal pts, Stmt stmt) {
        // 有左值的才有意义，不然获取类干啥
        if (!(stmt instanceof JAssignStmt)) {
            return;
        }
        SootMethod container = getContainer(varNode);

        Value leftOp = ((JAssignStmt) stmt).getLeftOp();
        pts.forall(new P2SetVisitor() {
            @Override
            public void visit(Node obj) {
                String className = CSObjs.toString(obj);
                if (className == null) {
                    AllocNode unknownClassObj = pag.makeStringConstantNode(UNKNOWN_CLASS);
//                    Obj clsObj = heapModel.getConstantObj(ClassLiteral.get(unknown));
//                    CSObj csObj = csManager.getCSObj(defaultHctx, clsObj);
                    propagator.addValuePointsTo(container, leftOp, unknownClassObj);
                    return;
                }
                SootClass klass = Scene.v().getSootClassUnsafe(className, true);

                if (klass == null) {
                    // TODO known but not exists, must process?
                    return;
                }
//                solver.initializeClass(klass);
                if (leftOp != null) {
                    // {c^t} if o_i^String \belongs pt(cName)
                    // 这里ClassConstant
                    AllocNode classObj = pag.makeClassConstantNode(ClassConstant.v(className));
                    //  CSObj csObj = csManager.getCSObj(defaultHctx, clsObj);
                    propagator.addValuePointsTo(container, leftOp, classObj);

                }
            }
        });
    }


    public MyReflectionModel setPag(PAG pag) {
        this.pag = pag;
        return this;
    }

    public void handleNewPointsToSet(VarNode varNode, PointsToSetInternal pointsToSet) {
        relevantVars.get(varNode).forEach(invokeRelatedStmt -> {

            SootMethod target = invokeRelatedStmt.getInvokeExpr().getMethod();
            if (target != null) {
                if ("<java.lang.Class: java.lang.Class forName(java.lang.String)>".equals(target.getSignature())) {
                    classForName(varNode, pointsToSet, invokeRelatedStmt);
                }
                else if ("<java.lang.Class: java.lang.Class forName(java.lang.String)>".equals(target.getSignature())){

                }

            }
        });

    }


    public boolean isRelevantVar(VarNode var) {
        return relevantVars.containsKey(var);
    }

    public SootMethod getContainer(VarNode varNode) {
        SootMethod container = null;
        Tag varNodeTag = pag.getNodeTags().get(varNode);

        if (varNodeTag instanceof LinkTag) {
            Host link = ((LinkTag) varNodeTag).getLink();
            if (link instanceof SootMethod) {
                container = (SootMethod) link;
            }
        }
        return container;
    }

}

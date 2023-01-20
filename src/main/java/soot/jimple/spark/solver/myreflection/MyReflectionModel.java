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
import soot.jimple.*;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.spark.solver.PropSolar;
import soot.jimple.spark.solver.myreflection.utils.CSObjs;
import soot.jimple.spark.solver.myreflection.utils.SootUtil;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReflectionModel;
import soot.tagkit.Host;
import soot.tagkit.LinkTag;
import soot.tagkit.Tag;
import soot.toDex.SootToDexUtils;
import soot.util.HashMultiMap;
import soot.util.MultiMap;
import soot.util.queue.QueueReader;

import java.util.*;

public class MyReflectionModel implements ReflectionModel {

    protected static final int BASE = -1;

    public static final String UNKNOWN_CLASS = "#UnknownClass";
    public static final String ARRAY_LENGTH = "#ArrayLength";

    public PAG pag;

    public PropSolar propagator;
    public List<Edge> edges = new ArrayList<>();


    protected final Map<SootMethod, int[]> relevantVarIndexes = new HashMap<>();

    protected final MultiMap<VarNode, Stmt> relevantVars
            = new HashMultiMap<>();

    protected void registerRelevantVarIndexes(SootMethod api, int... indexes) {
        relevantVarIndexes.put(api, indexes);
    }


    public MyReflectionModel(PropSolar propagator) {
        this.propagator = propagator;
        this.pag = propagator.getPag();
        registerVarAndHandler();

        handleAllInvokes();
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
    public void handleAllArrayStore() {
        QueueReader<MethodOrMethodContext> listener = Scene.v().getReachableMethods().listener();
        while (listener.hasNext()) {
            MethodOrMethodContext next = listener.next();
            SootMethod currentMethod = next.method();
            currentMethod.retrieveActiveBody().getUnits().stream()
                    .filter(unit -> (unit instanceof JAssignStmt))
                    .forEach(unit -> {
                        JAssignStmt arrayStoreStmt = (JAssignStmt) unit;
                        if (arrayStoreStmt.getLeftOp() instanceof ArrayRef) {
                            JArrayRef arrayRef = (JArrayRef) arrayStoreStmt.getLeftOp();
                            Value base = arrayRef.getBase();
                            LocalVarNode baseVarNode = pag.findLocalVarNode(base);
//                            baseVarNode.getAllFieldRefs().stream().filter(fieldRefNode -> {fieldRefNode instanceof ArrayElement}).forEach(fieldRef -> {
//                                Value rightOp = arrayStoreStmt.getRightOp();
//                                VarNode rVarNode = pag.findLocalVarNode(rightOp);
//                                rVarNode = rVarNode == null ? pag.findGlobalVarNode(rightOp) : rVarNode;
//                                rVarNode = rVarNode == null ? pag.makeLocalVarNode(rightOp,
//                                        rightOp.getType(), currentMethod) : rVarNode;
//                                fieldRef.makeP2Set().add(rVarNode);
////                                propagator.addVarNodeToWorkList(fieldRef);
//                            });
                        }
                    });
        }
    }


    protected void registerVarAndHandler() {
        SootMethod classForName = Scene.v().getMethod("<java.lang.Class: java.lang.Class forName(java.lang.String)>");
        registerRelevantVarIndexes(classForName, 0);

        SootMethod getMethod = Scene.v().getMethod("<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>");
        registerRelevantVarIndexes(getMethod, BASE, 0);

        SootMethod methodInvoke = Scene.v().getMethod("<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>");
        registerRelevantVarIndexes(methodInvoke, BASE, 0, 1);
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
            int[] indexes = relevantVarIndexes.get(target);
            if (indexes != null) {
                for (int i : indexes) {
                    relevantVars.put(getArg(container, invokeExpr, i), stmt);
                }
            }
            if (
                    "<java.lang.String: void <init>(java.lang.String)>"
                            .equals(target.getSignature())) {
                InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
                LocalVarNode to = pag.findLocalVarNode(instanceInvokeExpr.getBase());
                LocalVarNode from = pag.findLocalVarNode(instanceInvokeExpr.getArgs().get(0));
                pag.addSimpleEdge(from, to);
                propagator.addVarNodeToWorkList(from);
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
//        if (value instanceof StringConstant) {
//            return pag.makeGlobalVarNode(pag.makeStringConstantNode(((StringConstant) value).value), RefType.v("java.lang.String"));
//        }
        LocalVarNode localVarNode = pag.findLocalVarNode(value);

        LocalVarNode result = pag.makeLocalVarNode(value, value.getType(), container);
        if (localVarNode == null && value instanceof StringConstant) {
            result.makeP2Set().add(pag.makeStringConstantNode(((StringConstant) value).value));
            propagator.addVarNodeToWorkList(result);
        }
        return result;
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

        Value leftOp = ((JAssignStmt) stmt).getLeftOp();
        pts.forall(new P2SetVisitor() {
            @Override
            public void visit(Node obj) {
                String className = CSObjs.toString(obj);
                if (className == null) {
                    AllocNode unknownClassObj = pag.makeStringConstantNode(UNKNOWN_CLASS);
//                    Obj clsObj = heapModel.getConstantObj(ClassLiteral.get(unknown));
//                    CSObj csObj = csManager.getCSObj(defaultHctx, clsObj);
                    LocalVarNode clzVarNode = pag.findLocalVarNode(leftOp);
                    clzVarNode.makeP2Set().getNewSet().add(unknownClassObj);
                    propagator.addVarNodeToWorkList(clzVarNode);
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
                    AllocNode classObj = pag.makeClassConstantNode(ClassConstant.v(SootToDexUtils.getDexClassName(className)));
                    //  CSObj csObj = csManager.getCSObj(defaultHctx, clsObj);
                    LocalVarNode clzVarNode = pag.findLocalVarNode(leftOp);
                    clzVarNode.makeP2Set().getNewSet().add(classObj);
                    propagator.addVarNodeToWorkList(clzVarNode);

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
                } else if ("<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>".equals(target.getSignature())) {
                    methodInvoke(varNode, pointsToSet, invokeRelatedStmt);
                } else if ("<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>"
                        .equals(target.getSignature())) {
                    getMethod(varNode, pointsToSet, invokeRelatedStmt);
                }
            }
        });

    }

    private void methodInvoke(VarNode varNode, PointsToSetInternal pointsToSet, Stmt stmt) {

        // 有左值的才有意义，不然获取类干啥
        if (!(stmt instanceof JAssignStmt || stmt instanceof JInvokeStmt)) {
            return;
        }
        InstanceInvokeExpr invokeExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();

//        Context context = csVar.getContext();

        Value mVal = invokeExpr.getBase();
        Value recvVal = invokeExpr.getArg(0);
        Value argsVal = invokeExpr.getArg(1);

        LocalVarNode mVarNode = pag.findLocalVarNode(mVal);
        SootMethod container = mVarNode.getMethod();
        VarNode recvVarNode = pag.makeLocalVarNode(mVal, mVal.getType(), container);
        VarNode argsVarNode = pag.findLocalVarNode(argsVal);

        List<PointsToSetInternal> args = getArgs(container, varNode, pointsToSet, invokeExpr, BASE, 0, 1);
        PointsToSetInternal mtdObjs = args.get(0);
        PointsToSetInternal recvObjs = args.get(1);
        PointsToSetInternal argsObjs = args.get(2);

//        List<String> allPossibleA = getMethodInvokeCastTargetClass(invoke.getLValue());
        List<String> allPossibleTr = new ArrayList<>();
        if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            List<String> allPossibleA = SootUtil.getInstance().getMethodInvokeCastTargetClass(container, jAssignStmt.getLeftOp());
            allPossibleTr = SootUtil.getInstance().getThisAndAllRelatedClasses(allPossibleA);
        }
        if (allPossibleTr.size() == 0) allPossibleTr.add(null);


        List<String> finalAllPossibleTr = allPossibleTr;
        List<String> finalAllPossibleTr1 = allPossibleTr;
        mtdObjs.forall(new P2SetVisitor() {
            @Override
            public void visit(Node n) {
                if (n instanceof UJMethod) {
                    UJMethod ujMethod = (UJMethod) n;
                    if (ujMethod.isKnown()) {
                        List<SootMethod> allPossibleSootMethod = SootUtil.getInstance().MTD(ujMethod);
                        for (SootMethod possibleTarget : allPossibleSootMethod) {
                            if (possibleTarget.isStatic()) {
                                edges.add(new Edge(container, stmt, possibleTarget, Kind.STATIC));
                            }
                            else {
                                recvObjs.forall(new P2SetVisitor() {
                                    @Override
                                    public void visit(Node n) {
                                        addReflectiveCallEdge(container, stmt, possibleTarget, Kind.VIRTUAL, n);
                                    }
                                });
                            }
                        }
                    }

                    if (ujMethod.isDeclaringClassUnknown()) {
                        recvObjs.forall(new P2SetVisitor() {
                            @Override
                            public void visit(Node n) {
                                String possibleDeclaringClass = n.getType().toString();
                                UJMethod inferResult = new UJMethod(pag, ujMethod);
                                inferResult.setDeclaringClass(possibleDeclaringClass);
                                mVarNode.makeP2Set().add(inferResult);
                            }
                        });
                    }

                    if (ujMethod.isDeclaringClassUnknown() && !ujMethod.isSubsignatureUnknown()) {
                        boolean[] hasUnknownTypeObject = {false};
                        recvObjs.forall(new P2SetVisitor() {
                            @Override
                            public void visit(Node n) {
                                if (CSObjs.isUnknownClassObj(n)) {
                                    hasUnknownTypeObject[0] = true;
                                }
                            }
                        });

                        if (hasUnknownTypeObject[0]) {
                            if (ujMethod.isReturnTypeUnknown() || finalAllPossibleTr1.contains(ujMethod.returnType())) {
                                // 利用methodName和一会儿推导出来的ptp，来推测可能的定义类
                                if (!ujMethod.isMethodNameUnknown()) {
                                    List<String> arrTypes = new ArrayList<String>();
                                    List<Integer> argsLengths = new ArrayList<>();
                                    calculatePossibleArrayElementTypes(argsObjs, argsLengths, arrTypes);
                                    List<List<String>> ptpResult = new ArrayList<>();
                                    for (Integer argsLength : argsLengths) {
                                        ptpResult.addAll(SootUtil.getInstance().ptp3(arrTypes, argsLength));

                                    }

                                    if (!ujMethod.isParametersUnknown() && SootUtil.getInstance().belongsToPtp(ujMethod.parameters(), ptpResult)) {
                                        Set<String> possibleDeclaringClasses = SootUtil.getInstance().M(ujMethod.returnType(), ujMethod.methodName(), ujMethod.parameters());
                                        for (String possibleDeclaringClass : possibleDeclaringClasses) {
                                            UJMethod inferResult = new UJMethod(pag, ujMethod);
                                            ujMethod.setDeclaringClass(possibleDeclaringClass);
                                            mtdObjs.add(ujMethod);
                                        }
                                    }
                                }


                            }
                        }
                    }
                    // m−u ∈ pt(m)
                    // ------------------------
                    //pt(m) ⊇ { m−s | s.p ∈ Ptp(args), s.tr ≪: A, s.nm = u}
                    //[I-InvSig]
                    if (ujMethod.isParametersUnknown()) {
                        List<String> tmpArrTypes = new ArrayList<>();
                        List<Integer> argsLengths = new ArrayList<>();
                        calculatePossibleArrayElementTypes(argsObjs, argsLengths, tmpArrTypes);
                        List<String> arrTypes = new ArrayList<>(new HashSet<>(tmpArrTypes));
                        List<List<String>> ptpResult = new ArrayList<>();
                        for (Integer argsLength : argsLengths) {
                            ptpResult.addAll(SootUtil.getInstance().ptp3(arrTypes, argsLength));
                        }
                        for (List<String> onePtp : ptpResult) {
                            for (String oneTr : finalAllPossibleTr) {
                                // 构造m-s
                                UJMethod inferResult = new UJMethod(pag, ujMethod);
                                inferResult.setReturnType(oneTr);
                                inferResult.setParameters(onePtp);
                                mVarNode.makeP2Set().add(inferResult);
                            }
                        }
                    }

                }
            }
        });
        if (!mVarNode.makeP2Set().getNewSet().isEmpty()) {
            propagator.addVarNodeToWorkList(mVarNode);
        }
    }

    private void getMethod(VarNode varNode, PointsToSetInternal pointsToSet, Stmt stmt) {
        // 有左值的才有意义，不然获取方法干啥
        if (!(stmt instanceof JAssignStmt)) {
            return;
        }
        JAssignStmt jAssignStmt = (JAssignStmt) stmt;
        InstanceInvokeExpr invokeExpr = (InstanceInvokeExpr) jAssignStmt.getInvokeExpr();

        Value mVal = jAssignStmt.getLeftOp();
        Value clzVal = invokeExpr.getBase();
        Value mNameVal = invokeExpr.getArg(0);

        LocalVarNode clzVarNode = pag.findLocalVarNode(clzVal);
        SootMethod container = clzVarNode.getMethod();
        VarNode mVarNode = pag.makeLocalVarNode(mVal, mVal.getType(), container);
        VarNode mNameVarNode = pag.findLocalVarNode(mNameVal);
        if (mNameVarNode == null/* && mNameVal instanceof StringConstant*/) {
            mNameVarNode = pag.makeLocalVarNode(mNameVal, mNameVal.getType(), container);
            mNameVarNode.makeP2Set().getNewSet().add(pag.makeStringConstantNode(((StringConstant) mNameVal).value));
            propagator.addVarNodeToWorkList(mNameVarNode);
            this.relevantVars.put(mNameVarNode, stmt);
        }

        List<PointsToSetInternal> args = getArgs(container, varNode, pointsToSet, invokeExpr, BASE, 0);


        PointsToSetInternal clzObjs = args.get(0);
        PointsToSetInternal mObjs = mVarNode.makeP2Set();
        PointsToSetInternal mNameObjs = args.get(1);


        clzObjs.forall(new P2SetVisitor() {
            @Override
            public void visit(Node n) {
                SootClass possibleSootClass = CSObjs.toClass(n);
                String possibleClassName = possibleSootClass != null ? possibleSootClass.getName() : null;
                // if c-=c^t and o_iString belongsto SC
                mNameObjs.forall(new P2SetVisitor() {
                    @Override
                    public void visit(Node n) {
                        String mName = CSObjs.toString(n);
                        // if c− = ct ∧ o_i^String \belongs SC
                        UJMethod ujMethod = new UJMethod(pag,
                                possibleClassName, UJMethod.UNKNOWN_STRING,
                                mName, UJMethod.UNKNOWN_LIST);
                        mObjs.add(ujMethod);
                    }
                });
            }
        });
        if (!mObjs.getNewSet().isEmpty()) {
            propagator.addVarNodeToWorkList(mVarNode);
        }
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


    public MyReflectionModel setPropagator(PropSolar propagator) {
        this.propagator = propagator;
        return this;
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
    protected List<PointsToSetInternal> getArgs(SootMethod container,
            VarNode varNode, PointsToSetInternal pts, InvokeExpr invokeExpr, int... indexes) {
        List<PointsToSetInternal> args = new ArrayList<>(indexes.length);
        for (int i : indexes) {
            VarNode arg = getArg(container, invokeExpr, i);
            if (arg.equals(varNode)) {
                args.add(pts);
            } else {
                args.add(arg.makeP2Set());
            }
        }
        return args;
    }


    public void calculatePossibleArrayElementTypes(PointsToSetInternal argsObjs, List<Integer> argsLengths, List<String> arrayTypes) {
        argsObjs.forall(new P2SetVisitor() {
            @Override
            public void visit(Node n) {
                if (CSObjs.isArrayAllocNode(n)) {
                    argsLengths.add(CSObjs.getArrayLength(n));
                    AllocNode arrayAllocNode = (AllocNode) n;
                    arrayAllocNode.getFields().forEach(field -> {
                        field.makeP2Set().forall(new P2SetVisitor() {
                            @Override
                            public void visit(Node n) {
                                if (n instanceof AllocNode) {
                                    arrayTypes.addAll(SootUtil.getInstance().getThisAndAllSuperClasses(n.getType().toString()));
                                }
                            }
                        });
                    });
                }
            }
        });
    }

    private void addReflectiveCallEdge(SootMethod container, Stmt stmt, SootMethod target, Kind kind, Node recvObj) {
        if (!target.isConstructor() && !target.isStatic()) {
            assert recvObj != null;
            target = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClassUnsafe(recvObj.getType().toString()),
                    target);
            if (target == null) {
                return;
            }
        }
        edges.add(new Edge(container, stmt, target, kind));
    }

}

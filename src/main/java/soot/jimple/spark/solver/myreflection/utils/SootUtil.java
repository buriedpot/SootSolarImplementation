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

import com.google.common.collect.Lists;
import soot.*;
import soot.jimple.CastExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.spark.pag.UJMethod;
import soot.jimple.spark.pag.VarNode;

import java.util.*;
import java.util.stream.Collectors;

public class SootUtil {

    private static class SootUtilHolder {
        private static final SootUtil INSTANCE = new SootUtil();
    }

    private SootUtil() {
    }

    public static SootUtil getInstance() {
        return SootUtilHolder.INSTANCE;
    }


    public List<SootMethod> MTD(UJMethod ujMethod) {
        List<SootMethod> result = new ArrayList<>();
        String returnType = ujMethod.returnType();
        String methodName = ujMethod.methodName();
        List<String> parameters = ujMethod.parameters();
        if (ujMethod.isDeclaringClassUnknown()) {
            Scene.v().getClasses().forEach(jClass -> {
                result.addAll(jClass.getMethods().stream().filter(declaredMethod ->
                        declaredMethod.getName().equals(methodName) &&
                                equalList(declaredMethod.getParameterTypes().stream()
                                        .map(Type::toString)
                                        .collect(Collectors.toList()), parameters)
                ).collect(Collectors.toSet()));
            });
        }
        // dispatch
        else {
            if (!ujMethod.isMethodNameUnknown()) {
                String currentClass = ujMethod.declaringClass();
                SootClass currentSootClass = Scene.v().getSootClass(currentClass);
                while (currentSootClass != null) {
                    List<SootMethod> sootMethodStream = currentSootClass.getMethods().stream().filter(
                            declaredMethod -> declaredMethod.getName().equals(methodName) &&
                                    equalList(declaredMethod.getParameterTypes().stream().map(Type::toString)
                                            .collect(Collectors.toList()), parameters)).collect(Collectors.toList());
                    if (sootMethodStream.size() > 0) {
                        result.add(sootMethodStream.get(0));
                        return result;
                    }
                    currentSootClass = currentSootClass.getSuperclassUnsafe();
                }
            }
            else {
                String currentClass = ujMethod.declaringClass();
                SootClass currentSootClass = Scene.v().getSootClass(currentClass);
                while (currentSootClass != null) {
                    List<SootMethod> SootMethodStream = currentSootClass.getMethods().stream().filter(
                            declaredMethod ->
                                    equalList(declaredMethod.getParameterTypes().stream().map(Type::toString)
                                            .collect(Collectors.toList()), parameters)).collect(Collectors.toList());
                    if (SootMethodStream.size() > 0) {
                        result.addAll(SootMethodStream);
                        return result;
                    }
                    currentSootClass = currentSootClass.getSuperclassUnsafe();
                }
            }

        }
        return result;

    }

    public boolean equalList(List<String> list1, List<String> list2) {
        if (list1 == list2) {
            return true;
        }
        if ((list1 == null) ^ (list2 == null)) {
            return false;
        }

        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;

    }




    /**
     * <:
     *
     * @param className 类名
     * @return {@link List}<{@link String}>
     */
    public List<String> getThisAndAllSuperClasses(String className) {
        List<String> result = new ArrayList<String>();
        SootClass thisClass = Scene.v().getSootClassUnsafe(className);
        while (thisClass != null) {
            result.add(thisClass.getName());
            thisClass = thisClass.getSuperclassUnsafe();
        }
        return result;
    }


    /**
     * ptp3
     * 参考论文的，无法精确分析数组类型时的PTP实现
     * 参数1是pt(o-i.arr)的所有对象type。参数2是每个实参的declaredType
     * TODO 没完全理解
     *
     * @param arrTypes 加勒比海盗类型
     * @return {@link List}<{@link List}<{@link String}>>
     */
    public List<List<String>> ptp3(Collection<String> arrTypes, int argsLen) {
        if (argsLen == 0) {
            List<List<String>> noArgsResult = new ArrayList<>();
            noArgsResult.add(new ArrayList<>());
            return noArgsResult;
        }
        List<List<String>> eachTypeSuperTypes = new ArrayList<List<String>>();
        for (int i = 0; i < argsLen; i++) {
            eachTypeSuperTypes.add((List<String>) arrTypes);
        }

        return Lists.cartesianProduct(eachTypeSuperTypes);
    }


    /**
     * get方法调用目标类
     * 获取所有var作为右值的cast语句
     *
     * @param container 容器
     * @param val       瓦尔
     * @return {@link List}<{@link String}>
     */
    public List<String> getMethodInvokeCastTargetClass(SootMethod container, Value val) {
        return container.retrieveActiveBody().getUnits().stream().filter(stmt -> {
            if (stmt instanceof JAssignStmt) {
                JAssignStmt jAssignStmt = (JAssignStmt) stmt;
                Value rightOp = jAssignStmt.getRightOp();
                if (rightOp instanceof JCastExpr) {
                    JCastExpr jCastExpr = (JCastExpr) rightOp;
                    if (jCastExpr.getOp().equals(val)) {
                        return true;
                    }
                }
            }
            return false;
        }).map(stmt -> ((JCastExpr) ((JAssignStmt) stmt).getRightOp()).getCastType().toString())
                .collect(Collectors.toList());
    }


    /**
     * <<:
     *
     * @return {@link List}<{@link String}>
     */
    public List<String> getThisAndAllRelatedClasses(List<String> classNames) {
        List<String> result = new ArrayList<String>();
        Set<String> mediate = new HashSet<String>();
        for (String className : classNames) {
            mediate.addAll(getThisAndAllRelatedClasses(className));
        }
        return new ArrayList<>(mediate);
    }

    /**
     * <<:
     *
     * @param className 类名
     * @return {@link List}<{@link String}>
     */
    public List<String> getThisAndAllRelatedClasses(String className) {
        SootClass thisClass = Scene.v().getSootClassUnsafe(className);
        if (thisClass == null) {
            return null;
        }
        Collection<SootClass> allSubclassesIncludeSelf = Scene.v().getFastHierarchy().getSubclassesOf(thisClass);
        Collection<SootClass> allRelatedClasses = getAllSuperClasses(className);
        allRelatedClasses.addAll(allSubclassesIncludeSelf);
        return allRelatedClasses.stream().map(SootClass::getName).collect(Collectors.toList());
    }


    /**
     * <:
     *
     * @param className 类名
     * @return {@link List}<{@link String}>
     */
    public List<SootClass> getAllSuperClasses(String className) {
        List<SootClass> result = new ArrayList<>();
        SootClass thisClass = Scene.v().getSootClassUnsafe(className);
        if (thisClass == null) {
            return null;
        }
        if (thisClass.getSuperclassUnsafe() == null) {
            return new ArrayList<>();
        }
        thisClass = thisClass.getSuperclassUnsafe();
        while (thisClass != null) {
            result.add(thisClass);
            thisClass = thisClass.getSuperclassUnsafe();
        }
        return result;
    }

    /**
     * 属于20元
     *
     * @param argTypes
     * @param ptp
     * @return boolean
     */
    public boolean belongsToPtp(List<String> argTypes, List<List<String>> ptp) {
        for (List<String> onePtp : ptp) {
            if (equalList(onePtp, argTypes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * the M function
     * 暂时不考虑returnType
     *
     * @param returnType 返回类型
     * @param methodName 方法名称
     * @param parameters
     * @return {@link Set}<{@link String}>
     */
    public Set<String> M(String returnType, String methodName, List<String> parameters) {
        return Scene.v().getClasses().stream().filter(jClass ->
                !jClass.getMethods().stream().filter(declaredMethod ->
                        declaredMethod.getName().equals(methodName) &&
                                equalList(declaredMethod.getParameterTypes().stream()
                                        .map(Type::toString)
                                        .collect(Collectors.toList()), parameters)

                ).collect(Collectors.toSet()).isEmpty()
        ).map(SootClass::getName).collect(Collectors.toSet());

    }

}

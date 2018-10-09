/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.security.tools.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.model.MethodReference;
import org.wso2.security.tools.util.JarScanner;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * MethodVisitorImpl - A visitor to visit a Java method.
 */
public class MethodVisitorImpl extends MethodVisitor {

    private static final Logger log = LoggerFactory.getLogger(MethodVisitorImpl.class);

    public static String methodName;
    private String logString;
    private int lineNumber;

    /**
     * Constructor to match the super.
     */
    public MethodVisitorImpl() {
        super(ASM6);
    }

    /**
     * Starts the visit of the method's code, if any (i.e. non abstract method).
     */
    @Override
    public void visitCode() {
        super.visitCode();
    }

    /**
     * Visits a method instruction. A method instruction is an instruction that invokes a method.
     *
     * @param opcode      the opcode of the type instruction to be visited. This opcode is either INVOKEVIRTUAL,
     *                    INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE.
     * @param owner       the internal name of the method's owner class (see getInternalName).
     * @param name        the method's name.
     * @param descriptor  the method's descriptor (see Type).
     * @param isInterface if the method's owner class is an interface.
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {

        JarScanner.methodReferences.add(new MethodReference(name, owner, MethodVisitorImpl.methodName,
                ClassVisitorImpl.className, lineNumber));

    }

    /**
     * Visits a line number declaration.
     *
     * @param line  a line number. This number refers to the source file from which the class was compiled.
     * @param start the first instruction corresponding to this line number.
     */
    @Override
    public void visitLineNumber(int line, Label start) {
        lineNumber = line;
        super.visitLineNumber(line, start);
    }

    /**
     * Visits the end of the method.
     */
    @Override
    public void visitEnd() {
        logString =  "Completed visiting method: " + methodName;
        log.debug(logString);

    }
}

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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * ClassVisitorImpl - A visitor to visit a Java class.
 */
public class ClassVisitorImpl extends ClassVisitor {

    private static final Logger log = LoggerFactory.getLogger(ClassVisitorImpl.class);

    private MethodVisitorImpl methodVisitor = new MethodVisitorImpl();

    private String logString;
    public static String className;
    public static String source;

    /**
     * Constructor to match the super.
     */
    public ClassVisitorImpl() {
        super(ASM6);
    }

    /**
     * Visits the header of the class.
     *
     * @param version    the class version.
     * @param access     the class's access flags (see Opcodes). This parameter also indicates if the class is
     *                   deprecated.
     * @param name       the internal name of the class (see getInternalName).
     * @param signature  the signature of this class. May be null if the class is not a generic one, and does not
     *                   extend or implement generic classes or interfaces.
     * @param superName  * superName - the internal of name of the super class (see getInternalName). For interfaces,
     *                   the super class is Object. May be null, but only for the Object class.
     * @param interfaces * interfaces - the internal names of the class's interfaces (see getInternalName).
     *                   May be null.
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
    }

    @Override
    public void visitSource(String source, String debug) {
        this.source = source;
    }

    /**
     * Visits a method of the class. This method must return a new MethodVisitor instance (or null)
     * each time it is called, i.e., it should not return a previously returned visitor.
     *
     * @param access     the method's access flags (see Opcodes). This parameter also indicates if the method is
     *                   synthetic and/or deprecated.
     * @param name       the method's name.
     * @param descriptor the method's descriptor (see Type).
     * @param signature  the method's signature. May be null if the method parameters, return type and exceptions
     *                   do not use generic types.
     * @param exceptions the internal names of the method's exception classes (see getInternalName). May be null.
     * @return an object to visit the byte code of the method, or null if this class visitor is not interested in
     * visiting the code of this method.
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        MethodVisitorImpl.methodName = name;
        return methodVisitor;
    }

    /**
     * Visits the end of the class. This method, which is the last one to be called, is used to inform the visitor
     * that all the fields and methods of the class have been visited.
     */
    public void visitEnd() {
        logString = "Completed visiting class: " + className;
        log.debug(logString);
    }
}

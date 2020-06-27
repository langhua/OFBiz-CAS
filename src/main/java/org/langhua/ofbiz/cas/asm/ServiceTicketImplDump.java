package org.langhua.ofbiz.cas.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ServiceTicketImplDump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, "org/jasig/cas/ticket/ServiceTicketImpl", null,
                "org/jasig/cas/ticket/AbstractTicket", new String[] { "org/jasig/cas/ticket/ServiceTicket" });

        {
            av0 = cw.visitAnnotation("Ljavax/persistence/Entity;", true);
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Ljavax/persistence/Table;", true);
            av0.visit("name", "SERVICETICKET");
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Ljavax/persistence/DiscriminatorColumn;", true);
            av0.visit("name", "TYPE");
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Ljavax/persistence/DiscriminatorValue;", true);
            av0.visit("value", "ST");
            av0.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "serialVersionUID", "J", null,
                    new Long(-4223319704861765405L));
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE, "service", "Lorg/jasig/cas/authentication/principal/Service;", null, null);
            {
                av0 = fv.visitAnnotation("Ljavax/persistence/Lob;", true);
                av0.visitEnd();
            }
            {
                av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
                av0.visit("name", "SERVICE");
                av0.visit("nullable", Boolean.FALSE);
                av0.visit("length", new Integer(2147483647));
                av0.visitEnd();
            }
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE, "fromNewLogin", "Z", null, null);
            {
                av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
                av0.visit("name", "FROM_NEW_LOGIN");
                av0.visit("nullable", Boolean.FALSE);
                av0.visitEnd();
            }
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE, "grantedTicketAlready", "Ljava/lang/Boolean;", null, null);
            {
                av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
                av0.visit("name", "TICKET_ALREADY_GRANTED");
                av0.visit("nullable", Boolean.FALSE);
                av0.visitEnd();
            }
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/AbstractTicket", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "grantedTicketAlready",
                    "Ljava/lang/Boolean;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicketImpl;Lorg/jasig/cas/authentication/principal/Service;ZLorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    null, null);
            {
                av0 = mv.visitParameterAnnotation(1, "Ljavax/validation/constraints/NotNull;", true);
                av0.visitEnd();
            }
            {
                av0 = mv.visitParameterAnnotation(2, "Ljavax/validation/constraints/NotNull;", true);
                av0.visitEnd();
            }
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/AbstractTicket", "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "grantedTicketAlready",
                    "Ljava/lang/Boolean;");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitLdcInsn("service cannot be null");
            mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
                    "(Ljava/lang/Object;Ljava/lang/String;)V", false);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn("ticket cannot be null");
            mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
                    "(Ljava/lang/Object;Ljava/lang/String;)V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "service",
                    "Lorg/jasig/cas/authentication/principal/Service;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "fromNewLogin", "Z");
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 6);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "isFromNewLogin", "()Z", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "fromNewLogin", "Z");
            mv.visitInsn(IRETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getService", "()Lorg/jasig/cas/authentication/principal/Service;", null,
                    null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "service",
                    "Lorg/jasig/cas/authentication/principal/Service;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "isValidFor", "(Lorg/jasig/cas/authentication/principal/Service;)Z", null,
                    null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/ServiceTicketImpl", "updateState", "()V", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "service",
                    "Lorg/jasig/cas/authentication/principal/Service;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/authentication/principal/Service", "matches",
                    "(Lorg/jasig/cas/authentication/principal/Service;)Z", true);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 1);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNONNULL, l0);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            Label l1 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l1);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(INSTANCEOF, "org/jasig/cas/ticket/ServiceTicket");
            Label l2 = new Label();
            mv.visitJumpInsn(IFNE, l2);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, "org/jasig/cas/ticket/Ticket");
            mv.visitVarInsn(ASTORE, 2);
            mv.visitTypeInsn(NEW, "org/apache/commons/lang3/builder/EqualsBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "org/apache/commons/lang3/builder/EqualsBuilder", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/Ticket", "getId", "()Ljava/lang/String;", true);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/ServiceTicketImpl", "getId", "()Ljava/lang/String;",
                    false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/builder/EqualsBuilder", "append",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Lorg/apache/commons/lang3/builder/EqualsBuilder;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/builder/EqualsBuilder", "isEquals", "()Z",
                    false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "grantProxyGrantingTicket",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)Lorg/jasig/cas/ticket/proxy/ProxyGrantingTicket;",
                    null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, null);
            Label l3 = new Label();
            mv.visitTryCatchBlock(l2, l3, l2, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ASTORE, 4);
            mv.visitInsn(MONITORENTER);
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "grantedTicketAlready",
                    "Ljava/lang/Boolean;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            Label l4 = new Label();
            mv.visitJumpInsn(IFEQ, l4);
            mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("PGT already generated for this ST. Cannot grant more than one TGT for ST");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V",
                    false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "org/jasig/cas/ticket/ServiceTicketImpl" }, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "grantedTicketAlready",
                    "Ljava/lang/Boolean;");
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(MONITOREXIT);
            mv.visitLabel(l1);
            Label l5 = new Label();
            mv.visitJumpInsn(GOTO, l5);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Throwable" });
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(MONITOREXIT);
            mv.visitLabel(l3);
            mv.visitInsn(ATHROW);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitTypeInsn(NEW, "org/jasig/cas/ticket/ProxyGrantingTicketImpl");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "service",
                    "Lorg/jasig/cas/authentication/principal/Service;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/ServiceTicketImpl", "getGrantingTicket",
                    "()Lorg/jasig/cas/ticket/TicketGrantingTicket;", false);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/ProxyGrantingTicketImpl", "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    false);
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/ServiceTicketImpl", "getGrantingTicket",
                    "()Lorg/jasig/cas/ticket/TicketGrantingTicket;", false);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/TicketGrantingTicket", "getProxyGrantingTickets",
                    "()Ljava/util/Collection;", true);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(7, 5);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getAuthentication", "()Lorg/jasig/cas/authentication/Authentication;",
                    null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicketImpl;Lorg/jasig/cas/authentication/principal/Service;ZLorg/jasig/cas/ticket/ExpirationPolicy;Ljava/lang/Long;Ljava/lang/Long;)V",
                    null, null);
            {
                av0 = mv.visitParameterAnnotation(1, "Ljavax/validation/constraints/NotNull;", true);
                av0.visitEnd();
            }
            {
                av0 = mv.visitParameterAnnotation(2, "Ljavax/validation/constraints/NotNull;", true);
                av0.visitEnd();
            }
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/AbstractTicket", "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/ticket/ExpirationPolicy;Ljava/lang/Long;Ljava/lang/Long;)V",
                    false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "grantedTicketAlready",
                    "Ljava/lang/Boolean;");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitLdcInsn("service cannot be null");
            mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
                    "(Ljava/lang/Object;Ljava/lang/String;)V", false);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn("ticket cannot be null");
            mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
                    "(Ljava/lang/Object;Ljava/lang/String;)V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "service",
                    "Lorg/jasig/cas/authentication/principal/Service;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/ServiceTicketImpl", "fromNewLogin", "Z");
            mv.visitInsn(RETURN);
            mv.visitMaxs(6, 8);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}

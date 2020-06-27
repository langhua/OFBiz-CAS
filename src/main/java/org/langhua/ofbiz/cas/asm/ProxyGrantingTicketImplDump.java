package org.langhua.ofbiz.cas.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ProxyGrantingTicketImplDump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_7, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, "org/jasig/cas/ticket/ProxyGrantingTicketImpl", null,
                "org/jasig/cas/ticket/TicketGrantingTicketImpl",
                new String[] { "org/jasig/cas/ticket/proxy/ProxyGrantingTicket" });

        {
            av0 = cw.visitAnnotation("Ljavax/persistence/Entity;", true);
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Ljavax/persistence/DiscriminatorValue;", true);
            av0.visit("value", "PGT");
            av0.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "serialVersionUID", "J", null,
                    new Long(-8126909926138945649L));
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 4);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(6, 6);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "grantProxyTicket",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/ExpirationPolicy;Z)Lorg/jasig/cas/ticket/proxy/ProxyTicket;",
                    null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitTypeInsn(NEW, "org/jasig/cas/ticket/ProxyTicketImpl");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/ProxyGrantingTicketImpl", "getCountOfUses", "()I",
                    false);
            Label l1 = new Label();
            mv.visitJumpInsn(IFNE, l1);
            mv.visitInsn(ICONST_1);
            Label l2 = new Label();
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_FULL, 5,
                    new Object[] { "org/jasig/cas/ticket/ProxyGrantingTicketImpl", "java/lang/String",
                            "org/jasig/cas/authentication/principal/Service", "org/jasig/cas/ticket/ExpirationPolicy",
                            Opcodes.INTEGER },
                    5, new Object[] { l0, l0, "java/lang/String", "org/jasig/cas/ticket/ProxyGrantingTicketImpl",
                            "org/jasig/cas/authentication/principal/Service" });
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_FULL, 5,
                    new Object[] { "org/jasig/cas/ticket/ProxyGrantingTicketImpl", "java/lang/String",
                            "org/jasig/cas/authentication/principal/Service", "org/jasig/cas/ticket/ExpirationPolicy",
                            Opcodes.INTEGER },
                    6, new Object[] { l0, l0, "java/lang/String", "org/jasig/cas/ticket/ProxyGrantingTicketImpl",
                            "org/jasig/cas/authentication/principal/Service", Opcodes.INTEGER });
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/ProxyTicketImpl", "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicketImpl;Lorg/jasig/cas/authentication/principal/Service;ZLorg/jasig/cas/ticket/ExpirationPolicy;)V",
                    false);
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/proxy/ProxyTicket", "getId",
                    "()Ljava/lang/String;", true);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/ProxyGrantingTicketImpl",
                    "updateServiceAndTrackSession",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Z)V", false);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(7, 6);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;Ljava/lang/Boolean;Ljava/lang/Long;Ljava/lang/Long;)V",
                    null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ALOAD, 8);
            mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "<init>",
                    "(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;Ljava/lang/Boolean;Ljava/lang/Long;Ljava/lang/Long;)V",
                    false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(9, 9);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}

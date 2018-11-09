package org.langhua.ofbiz.cas.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AbstractTicketDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER + ACC_ABSTRACT, "org/jasig/cas/ticket/AbstractTicket", null,
				"java/lang/Object", new String[] { "org/jasig/cas/ticket/Ticket", "org/jasig/cas/ticket/TicketState" });

		{
			av0 = cw.visitAnnotation("Ljavax/persistence/MappedSuperclass;", true);
			av0.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "serialVersionUID", "J", null,
					new Long(-8506442397878267555L));
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "expirationPolicy", "Lorg/jasig/cas/ticket/ExpirationPolicy;", null, null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Lob;", true);
				av0.visitEnd();
			}
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "EXPIRATION_POLICY");
				av0.visit("length", new Integer(2147483647));
				av0.visit("nullable", Boolean.FALSE);
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "id", "Ljava/lang/String;", null, null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Id;", true);
				av0.visitEnd();
			}
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "ID");
				av0.visit("nullable", Boolean.FALSE);
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "ticketGrantingTicket", "Lorg/jasig/cas/ticket/TicketGrantingTicket;", null,
					null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/ManyToOne;", true);
				av0.visit("targetEntity", Type.getType("Lorg/jasig/cas/ticket/TicketGrantingTicketImpl;"));
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "lastTimeUsed", "J", null, null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "LAST_TIME_USED");
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "previousLastTimeUsed", "J", null, null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "PREVIOUS_LAST_TIME_USED");
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "creationTime", "J", null, null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "CREATION_TIME");
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "countOfUses", "I", null, null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "NUMBER_OF_TIMES_USED");
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PROTECTED, "<init>", "()V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
					null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitLdcInsn("expirationPolicy cannot be null");
			mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
					"(Ljava/lang/Object;Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("id cannot be null");
			mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
					"(Ljava/lang/Object;Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "id", "Ljava/lang/String;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "creationTime", "J");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "lastTimeUsed", "J");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "expirationPolicy",
					"Lorg/jasig/cas/ticket/ExpirationPolicy;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "ticketGrantingTicket",
					"Lorg/jasig/cas/ticket/TicketGrantingTicket;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 4);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getId", "()Ljava/lang/String;", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "id", "Ljava/lang/String;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PROTECTED + ACC_FINAL, "updateState", "()V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "lastTimeUsed", "J");
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "previousLastTimeUsed", "J");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "lastTimeUsed", "J");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "countOfUses", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "countOfUses", "I");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getCountOfUses", "()I", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "countOfUses", "I");
			mv.visitInsn(IRETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getCreationTime", "()J", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "creationTime", "J");
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getGrantingTicket",
					"()Lorg/jasig/cas/ticket/TicketGrantingTicket;", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "ticketGrantingTicket",
					"Lorg/jasig/cas/ticket/TicketGrantingTicket;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getLastTimeUsed", "()J", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "lastTimeUsed", "J");
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getPreviousTimeUsed", "()J", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "previousLastTimeUsed", "J");
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "isExpired", "()Z", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/AbstractTicket", "getGrantingTicket",
					"()Lorg/jasig/cas/ticket/TicketGrantingTicket;", false);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/AbstractTicket", "expirationPolicy",
					"Lorg/jasig/cas/ticket/ExpirationPolicy;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/ExpirationPolicy", "isExpired",
					"(Lorg/jasig/cas/ticket/TicketState;)Z", true);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNE, l0);
			mv.visitVarInsn(ALOAD, 1);
			Label l1 = new Label();
			mv.visitJumpInsn(IFNULL, l1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/TicketGrantingTicket", "isExpired", "()Z", true);
			mv.visitJumpInsn(IFNE, l0);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "org/jasig/cas/ticket/TicketGrantingTicket" }, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/AbstractTicket", "isExpiredInternal", "()Z", false);
			mv.visitJumpInsn(IFNE, l0);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IRETURN);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PROTECTED, "isExpiredInternal", "()Z", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "hashCode", "()I", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitTypeInsn(NEW, "org/apache/commons/lang3/builder/HashCodeBuilder");
			mv.visitInsn(DUP);
			mv.visitIntInsn(BIPUSH, 13);
			mv.visitIntInsn(SIPUSH, 133);
			mv.visitMethodInsn(INVOKESPECIAL, "org/apache/commons/lang3/builder/HashCodeBuilder", "<init>", "(II)V",
					false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/AbstractTicket", "getId", "()Ljava/lang/String;",
					false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/builder/HashCodeBuilder", "append",
					"(Ljava/lang/Object;)Lorg/apache/commons/lang3/builder/HashCodeBuilder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/builder/HashCodeBuilder", "toHashCode", "()I",
					false);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(4, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "toString", "()Ljava/lang/String;", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/AbstractTicket", "getId", "()Ljava/lang/String;",
					false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "compareTo", "(Lorg/jasig/cas/ticket/Ticket;)I", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/AbstractTicket", "getId", "()Ljava/lang/String;",
					false);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/Ticket", "getId", "()Ljava/lang/String;", true);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I", false);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/ticket/ExpirationPolicy;Ljava/lang/Long;Ljava/lang/Long;)V",
					null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitLdcInsn("expirationPolicy cannot be null");
			mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
					"(Ljava/lang/Object;Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("id cannot be null");
			mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
					"(Ljava/lang/Object;Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "id", "Ljava/lang/String;");
			mv.visitVarInsn(ALOAD, 4);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNONNULL, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "creationTime", "J");
			Label l1 = new Label();
			mv.visitJumpInsn(GOTO, l1);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_FULL, 6,
					new Object[] { "org/jasig/cas/ticket/AbstractTicket", "java/lang/String",
							"org/jasig/cas/ticket/TicketGrantingTicket", "org/jasig/cas/ticket/ExpirationPolicy",
							"java/lang/Long", "java/lang/Long" },
					0, new Object[] {});
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "creationTime", "J");
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 5);
			Label l2 = new Label();
			mv.visitJumpInsn(IFNONNULL, l2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "lastTimeUsed", "J");
			Label l3 = new Label();
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "lastTimeUsed", "J");
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "expirationPolicy",
					"Lorg/jasig/cas/ticket/ExpirationPolicy;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/AbstractTicket", "ticketGrantingTicket",
					"Lorg/jasig/cas/ticket/TicketGrantingTicket;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 6);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "compareTo", "(Ljava/lang/Object;)I", null,
					null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, "org/jasig/cas/ticket/Ticket");
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/AbstractTicket", "compareTo",
					"(Lorg/jasig/cas/ticket/Ticket;)I", false);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}

package org.langhua.ofbiz.cas.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class TicketGrantingTicketImplDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, "org/jasig/cas/ticket/TicketGrantingTicketImpl", null,
				"org/jasig/cas/ticket/AbstractTicket", new String[] { "org/jasig/cas/ticket/TicketGrantingTicket" });

		{
			av0 = cw.visitAnnotation("Ljavax/persistence/Entity;", true);
			av0.visitEnd();
		}
		{
			av0 = cw.visitAnnotation("Ljavax/persistence/Table;", true);
			av0.visit("name", "TICKETGRANTINGTICKET");
			av0.visitEnd();
		}
		{
			av0 = cw.visitAnnotation("Ljavax/persistence/DiscriminatorColumn;", true);
			av0.visit("name", "TYPE");
			av0.visitEnd();
		}
		{
			av0 = cw.visitAnnotation("Ljavax/persistence/DiscriminatorValue;", true);
			av0.visit("value", "TGT");
			av0.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "serialVersionUID", "J", null,
					new Long(-8608149809180911599L));
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "LOGGER", "Lorg/slf4j/Logger;", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "authentication", "Lorg/jasig/cas/authentication/Authentication;", null,
					null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Lob;", true);
				av0.visitEnd();
			}
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "AUTHENTICATION");
				av0.visit("nullable", Boolean.FALSE);
				av0.visit("length", new Integer(2147483647));
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "expired", "Ljava/lang/Boolean;", null, null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "EXPIRED");
				av0.visit("nullable", Boolean.FALSE);
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "proxiedBy", "Lorg/jasig/cas/authentication/principal/Service;", null,
					null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "PROXIED_BY");
				av0.visit("nullable", Boolean.TRUE);
				av0.visit("length", new Integer(2147483647));
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "services", "Ljava/util/HashMap;",
					"Ljava/util/HashMap<Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;>;", null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Lob;", true);
				av0.visitEnd();
			}
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "SERVICES_GRANTED_ACCESS_TO");
				av0.visit("nullable", Boolean.FALSE);
				av0.visit("length", new Integer(2147483647));
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "supplementalAuthentications", "Ljava/util/ArrayList;",
					"Ljava/util/ArrayList<Lorg/jasig/cas/authentication/Authentication;>;", null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Lob;", true);
				av0.visitEnd();
			}
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
				av0.visit("name", "SUPPLEMENTAL_AUTHENTICATIONS");
				av0.visit("nullable", Boolean.FALSE);
				av0.visit("length", new Integer(2147483647));
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "proxyGrantingTickets", "Ljava/util/Set;",
					"Ljava/util/Set<Lorg/jasig/cas/ticket/proxy/ProxyGrantingTicket;>;", null);
			{
				av0 = fv.visitAnnotation("Ljavax/persistence/OneToMany;", true);
				av0.visit("targetEntity", Type.getType("Lorg/jasig/cas/ticket/TicketGrantingTicketImpl;"));
				av0.visit("mappedBy", "ticketGrantingTicket");
				av0.visitEnum("fetch", "Ljavax/persistence/FetchType;", "EAGER");
				av0.visitEnd();
			}
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitLdcInsn(Type.getType("Lorg/jasig/cas/ticket/TicketGrantingTicketImpl;"));
			mv.visitMethodInsn(INVOKESTATIC, "org/slf4j/LoggerFactory", "getLogger",
					"(Ljava/lang/Class;)Lorg/slf4j/Logger;", false);
			mv.visitFieldInsn(PUTSTATIC, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "LOGGER",
					"Lorg/slf4j/Logger;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/AbstractTicket", "<init>", "()V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "expired",
					"Ljava/lang/Boolean;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "services",
					"Ljava/util/HashMap;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/ArrayList");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "supplementalAuthentications",
					"Ljava/util/ArrayList;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/HashSet");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "proxyGrantingTickets",
					"Ljava/util/Set;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
					null, null);
			{
				av0 = mv.visitParameterAnnotation(3, "Ljavax/validation/constraints/NotNull;", true);
				av0.visitEnd();
			}
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/AbstractTicket", "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
					false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "expired",
					"Ljava/lang/Boolean;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "services",
					"Ljava/util/HashMap;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/ArrayList");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "supplementalAuthentications",
					"Ljava/util/ArrayList;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/HashSet");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "proxyGrantingTickets",
					"Ljava/util/Set;");
			mv.visitVarInsn(ALOAD, 3);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNULL, l0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitJumpInsn(IFNONNULL, l0);
			mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("Must specify proxiedBy when providing parent TGT");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V",
					false);
			mv.visitInsn(ATHROW);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_FULL, 6,
					new Object[] { "org/jasig/cas/ticket/TicketGrantingTicketImpl", "java/lang/String",
							"org/jasig/cas/authentication/principal/Service",
							"org/jasig/cas/ticket/TicketGrantingTicket", "org/jasig/cas/authentication/Authentication",
							"org/jasig/cas/ticket/ExpirationPolicy" },
					0, new Object[] {});
			mv.visitVarInsn(ALOAD, 4);
			mv.visitLdcInsn("authentication cannot be null");
			mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
					"(Ljava/lang/Object;Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "authentication",
					"Lorg/jasig/cas/authentication/Authentication;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "proxiedBy",
					"Lorg/jasig/cas/authentication/principal/Service;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 6);
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
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;)V",
					false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(6, 4);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getAuthentication",
					"()Lorg/jasig/cas/authentication/Authentication;", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "authentication",
					"Lorg/jasig/cas/authentication/Authentication;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_SYNCHRONIZED, "grantServiceTicket",
					"(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/ExpirationPolicy;ZZ)Lorg/jasig/cas/ticket/ServiceTicket;",
					null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitTypeInsn(NEW, "org/jasig/cas/ticket/ServiceTicketImpl");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "getCountOfUses", "()I",
					false);
			Label l1 = new Label();
			mv.visitJumpInsn(IFEQ, l1);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitJumpInsn(IFNE, l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_FULL, 6,
					new Object[] { "org/jasig/cas/ticket/TicketGrantingTicketImpl", "java/lang/String",
							"org/jasig/cas/authentication/principal/Service", "org/jasig/cas/ticket/ExpirationPolicy",
							Opcodes.INTEGER, Opcodes.INTEGER },
					5, new Object[] { l0, l0, "java/lang/String", "org/jasig/cas/ticket/TicketGrantingTicketImpl",
							"org/jasig/cas/authentication/principal/Service" });
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_FULL, 6,
					new Object[] { "org/jasig/cas/ticket/TicketGrantingTicketImpl", "java/lang/String",
							"org/jasig/cas/authentication/principal/Service", "org/jasig/cas/ticket/ExpirationPolicy",
							Opcodes.INTEGER, Opcodes.INTEGER },
					6, new Object[] { l0, l0, "java/lang/String", "org/jasig/cas/ticket/TicketGrantingTicketImpl",
							"org/jasig/cas/authentication/principal/Service", Opcodes.INTEGER });
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/ServiceTicketImpl", "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicketImpl;Lorg/jasig/cas/authentication/principal/Service;ZLorg/jasig/cas/ticket/ExpirationPolicy;)V",
					false);
			mv.visitVarInsn(ASTORE, 6);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/ServiceTicket", "getId", "()Ljava/lang/String;",
					true);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl",
					"updateServiceAndTrackSession",
					"(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Z)V", false);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(7, 7);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PROTECTED, "updateServiceAndTrackSession",
					"(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Z)V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "updateState", "()V",
					false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl",
					"getChainedAuthentications", "()Ljava/util/List;", false);
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(ISUB);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
			mv.visitTypeInsn(CHECKCAST, "org/jasig/cas/authentication/Authentication");
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/authentication/Authentication", "getPrincipal",
					"()Lorg/jasig/cas/authentication/principal/Principal;", true);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/authentication/principal/Service", "setPrincipal",
					"(Lorg/jasig/cas/authentication/principal/Principal;)V", true);
			mv.visitVarInsn(ILOAD, 3);
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "normalizePath",
					"(Lorg/jasig/cas/authentication/principal/Service;)Ljava/lang/String;", false);
			mv.visitVarInsn(ASTORE, 5);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "services",
					"Ljava/util/HashMap;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "values", "()Ljava/util/Collection;", false);
			mv.visitVarInsn(ASTORE, 6);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;", true);
			mv.visitVarInsn(ASTORE, 8);
			Label l1 = new Label();
			mv.visitJumpInsn(GOTO, l1);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_FULL, 9,
					new Object[] { "org/jasig/cas/ticket/TicketGrantingTicketImpl", "java/lang/String",
							"org/jasig/cas/authentication/principal/Service", Opcodes.INTEGER, "java/util/List",
							"java/lang/String", "java/util/Collection", Opcodes.TOP, "java/util/Iterator" },
					0, new Object[] {});
			mv.visitVarInsn(ALOAD, 8);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
			mv.visitTypeInsn(CHECKCAST, "org/jasig/cas/authentication/principal/Service");
			mv.visitVarInsn(ASTORE, 7);
			mv.visitVarInsn(ALOAD, 7);
			mv.visitMethodInsn(INVOKESTATIC, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "normalizePath",
					"(Lorg/jasig/cas/authentication/principal/Service;)Ljava/lang/String;", false);
			mv.visitVarInsn(ASTORE, 9);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 9);
			mv.visitMethodInsn(INVOKESTATIC, "org/apache/commons/lang3/StringUtils", "equals",
					"(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Z", false);
			mv.visitJumpInsn(IFEQ, l1);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitVarInsn(ALOAD, 7);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "remove", "(Ljava/lang/Object;)Z", true);
			mv.visitInsn(POP);
			mv.visitFieldInsn(GETSTATIC, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "LOGGER",
					"Lorg/slf4j/Logger;");
			mv.visitLdcInsn("Removed previous tickets for service: {}");
			mv.visitVarInsn(ALOAD, 7);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "trace", "(Ljava/lang/String;Ljava/lang/Object;)V",
					true);
			mv.visitJumpInsn(GOTO, l0);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 8);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
			mv.visitJumpInsn(IFNE, l2);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_FULL, 5,
					new Object[] { "org/jasig/cas/ticket/TicketGrantingTicketImpl", "java/lang/String",
							"org/jasig/cas/authentication/principal/Service", Opcodes.INTEGER, "java/util/List" },
					0, new Object[] {});
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "services",
					"Ljava/util/HashMap;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put",
					"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
			mv.visitInsn(POP);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 10);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "normalizePath",
					"(Lorg/jasig/cas/authentication/principal/Service;)Ljava/lang/String;", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/authentication/principal/Service", "getId",
					"()Ljava/lang/String;", true);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("?");
			mv.visitMethodInsn(INVOKESTATIC, "org/apache/commons/lang3/StringUtils", "substringBefore",
					"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(";");
			mv.visitMethodInsn(INVOKESTATIC, "org/apache/commons/lang3/StringUtils", "substringBefore",
					"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("#");
			mv.visitMethodInsn(INVOKESTATIC, "org/apache/commons/lang3/StringUtils", "substringBefore",
					"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_SYNCHRONIZED, "getServices", "()Ljava/util/Map;",
					"()Ljava/util/Map<Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;>;", null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "services",
					"Ljava/util/HashMap;");
			mv.visitMethodInsn(INVOKESTATIC, "com/google/common/collect/ImmutableMap", "copyOf",
					"(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "getProxyGrantingTickets", "()Ljava/util/Collection;",
					"()Ljava/util/Collection<Lorg/jasig/cas/ticket/proxy/ProxyGrantingTicket;>;", null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "proxyGrantingTickets",
					"Ljava/util/Set;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "removeAllServices", "()V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "services",
					"Ljava/util/HashMap;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "clear", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "isRoot", "()Z", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "getGrantingTicket",
					"()Lorg/jasig/cas/ticket/TicketGrantingTicket;", false);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNONNULL, l0);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IRETURN);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "markTicketExpired", "()V", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "expired",
					"Ljava/lang/Boolean;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getRoot", "()Lorg/jasig/cas/ticket/TicketGrantingTicket;",
					null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/TicketGrantingTicket", "getGrantingTicket",
					"()Lorg/jasig/cas/ticket/TicketGrantingTicket;", true);
			mv.visitVarInsn(ASTORE, 2);
			Label l0 = new Label();
			mv.visitJumpInsn(GOTO, l0);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "org/jasig/cas/ticket/TicketGrantingTicket",
					"org/jasig/cas/ticket/TicketGrantingTicket" }, 0, null);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/TicketGrantingTicket", "getGrantingTicket",
					"()Lorg/jasig/cas/ticket/TicketGrantingTicket;", true);
			mv.visitVarInsn(ASTORE, 2);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitJumpInsn(IFNONNULL, l1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "isExpiredInternal", "()Z", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "expired",
					"Ljava/lang/Boolean;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getSupplementalAuthentications", "()Ljava/util/List;",
					"()Ljava/util/List<Lorg/jasig/cas/authentication/Authentication;>;", null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "supplementalAuthentications",
					"Ljava/util/ArrayList;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getChainedAuthentications", "()Ljava/util/List;",
					"()Ljava/util/List<Lorg/jasig/cas/authentication/Authentication;>;", null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/util/ArrayList");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "getAuthentication",
					"()Lorg/jasig/cas/authentication/Authentication;", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
			mv.visitInsn(POP);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "getGrantingTicket",
					"()Lorg/jasig/cas/ticket/TicketGrantingTicket;", false);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNONNULL, l0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "unmodifiableList",
					"(Ljava/util/List;)Ljava/util/List;", false);
			mv.visitInsn(ARETURN);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "java/util/List" }, 0, null);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "getGrantingTicket",
					"()Lorg/jasig/cas/ticket/TicketGrantingTicket;", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/jasig/cas/ticket/TicketGrantingTicket",
					"getChainedAuthentications", "()Ljava/util/List;", true);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z", true);
			mv.visitInsn(POP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "unmodifiableList",
					"(Ljava/util/List;)Ljava/util/List;", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getProxiedBy",
					"()Lorg/jasig/cas/authentication/principal/Service;", null, null);
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "proxiedBy",
					"Lorg/jasig/cas/authentication/principal/Service;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "equals", "(Ljava/lang/Object;)Z", null, null);
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
			mv.visitTypeInsn(INSTANCEOF, "org/jasig/cas/ticket/TicketGrantingTicket");
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
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "getId",
					"()Ljava/lang/String;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/builder/EqualsBuilder", "append",
					"(Ljava/lang/Object;Ljava/lang/Object;)Lorg/apache/commons/lang3/builder/EqualsBuilder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/builder/EqualsBuilder", "isEquals", "()Z",
					false);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/authentication/principal/Service;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/authentication/Authentication;Lorg/jasig/cas/ticket/ExpirationPolicy;Ljava/lang/Boolean;Ljava/lang/Long;Ljava/lang/Long;)V",
					null, null);
			{
				av0 = mv.visitParameterAnnotation(3, "Ljavax/validation/constraints/NotNull;", true);
				av0.visitEnd();
			}
// ATTRIBUTE org.aspectj.weaver.MethodDeclarationLineNumber
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 7);
			mv.visitVarInsn(ALOAD, 8);
			mv.visitMethodInsn(INVOKESPECIAL, "org/jasig/cas/ticket/AbstractTicket", "<init>",
					"(Ljava/lang/String;Lorg/jasig/cas/ticket/TicketGrantingTicket;Lorg/jasig/cas/ticket/ExpirationPolicy;Ljava/lang/Long;Ljava/lang/Long;)V",
					false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "expired",
					"Ljava/lang/Boolean;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "services",
					"Ljava/util/HashMap;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/ArrayList");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "supplementalAuthentications",
					"Ljava/util/ArrayList;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/util/HashSet");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "proxyGrantingTickets",
					"Ljava/util/Set;");
			mv.visitVarInsn(ALOAD, 3);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNULL, l0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitJumpInsn(IFNONNULL, l0);
			mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("Must specify proxiedBy when providing parent TGT");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V",
					false);
			mv.visitInsn(ATHROW);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_FULL, 9, new Object[] { "org/jasig/cas/ticket/TicketGrantingTicketImpl",
					"java/lang/String", "org/jasig/cas/authentication/principal/Service",
					"org/jasig/cas/ticket/TicketGrantingTicket", "org/jasig/cas/authentication/Authentication",
					"org/jasig/cas/ticket/ExpirationPolicy", "java/lang/Boolean", "java/lang/Long", "java/lang/Long" },
					0, new Object[] {});
			mv.visitVarInsn(ALOAD, 4);
			mv.visitLdcInsn("authentication cannot be null");
			mv.visitMethodInsn(INVOKESTATIC, "org/springframework/util/Assert", "notNull",
					"(Ljava/lang/Object;Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "authentication",
					"Lorg/jasig/cas/authentication/Authentication;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "proxiedBy",
					"Lorg/jasig/cas/authentication/principal/Service;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitFieldInsn(PUTFIELD, "org/jasig/cas/ticket/TicketGrantingTicketImpl", "expired",
					"Ljava/lang/Boolean;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(6, 9);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}

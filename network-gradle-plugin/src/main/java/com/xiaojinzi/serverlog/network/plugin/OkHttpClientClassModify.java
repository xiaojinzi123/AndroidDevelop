package com.xiaojinzi.serverlog.network.plugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;

public class OkHttpClientClassModify implements Opcodes {

    public static final int ASM_API = ASM4;

    public static byte[] doModify(InputStream inputStream,
                                  String networkLogFirstInterceptorStr, String networkLogTailInterceptorStr) throws IOException {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassVisitor cv = new ClassVisitor(ASM_API, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                // 移除原有的方法
                if ("interceptors".equals(name) && "()Ljava/util/List;".equals(desc)) {
                    System.out.println(IOUtil.MODULE_TAG + "找到了 OkHttpClient interceptors 方法");
                    return null;
                } else  {
                    // System.out.println(IOUtil.MODULE_TAG + "OkHttpClient 中的其他方法, name = " + name);
                    // return null;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };

        ClassReader cr = new ClassReader(inputStream);
        cr.accept(cv, 0);

        generateInterceptorsMethod(cw, networkLogFirstInterceptorStr, networkLogTailInterceptorStr);

        return cw.toByteArray();

    }

    private static void generateInterceptorsMethod(ClassWriter cw,
                                                   String networkLogFirstInterceptorStr, String networkLogTailInterceptorStr) {
        // 新增方法
        MethodVisitor interceptorsMethodVisitor = cw.visitMethod(ACC_PUBLIC, "interceptors", "()Ljava/util/List;", null, null);

        interceptorsMethodVisitor.visitCode();
        // 创建 ArrayList list =  new ArrayList(interceptors);
        Label labelNewList = new Label();
        interceptorsMethodVisitor.visitLabel(labelNewList);
        interceptorsMethodVisitor.visitTypeInsn(NEW, "java/util/ArrayList");
        interceptorsMethodVisitor.visitInsn(DUP);
        interceptorsMethodVisitor.visitVarInsn(ALOAD, 0);
        interceptorsMethodVisitor.visitFieldInsn(GETFIELD, "okhttp3/OkHttpClient", "interceptors", "Ljava/util/List;");
        interceptorsMethodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false);
        interceptorsMethodVisitor.visitVarInsn(ASTORE, 1);

        if (networkLogFirstInterceptorStr != null) {
            Label labelCreateNetworkLogInterceptor = new Label();
            interceptorsMethodVisitor.visitLabel(labelCreateNetworkLogInterceptor);
            interceptorsMethodVisitor.visitVarInsn(ALOAD, 1);
            interceptorsMethodVisitor.visitInsn(ICONST_0);
            interceptorsMethodVisitor.visitTypeInsn(NEW, networkLogFirstInterceptorStr);
            interceptorsMethodVisitor.visitInsn(DUP);
            interceptorsMethodVisitor.visitMethodInsn(INVOKESPECIAL, networkLogFirstInterceptorStr, "<init>", "()V", false);
            interceptorsMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(ILjava/lang/Object;)V", false);
        }

        if (networkLogTailInterceptorStr != null) {
            Label labelCreateNetworkLogProcessedInterceptor = new Label();
            interceptorsMethodVisitor.visitLabel(labelCreateNetworkLogProcessedInterceptor);
            interceptorsMethodVisitor.visitVarInsn(ALOAD, 1);
            interceptorsMethodVisitor.visitTypeInsn(NEW, networkLogTailInterceptorStr);
            interceptorsMethodVisitor.visitInsn(DUP);
            interceptorsMethodVisitor.visitMethodInsn(INVOKESPECIAL, networkLogTailInterceptorStr, "<init>", "()V", false);
            interceptorsMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
            interceptorsMethodVisitor.visitInsn(POP);
        }

        Label labelReturn = new Label();
        interceptorsMethodVisitor.visitLabel(labelReturn);
        interceptorsMethodVisitor.visitVarInsn(ALOAD, 1);
        interceptorsMethodVisitor.visitInsn(ARETURN);

        interceptorsMethodVisitor.visitEnd();

        System.out.println(IOUtil.MODULE_TAG + "新增了 OkHttpClient interceptors 方法");

    }

}

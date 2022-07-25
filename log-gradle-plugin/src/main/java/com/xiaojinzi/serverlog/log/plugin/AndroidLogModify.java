package com.xiaojinzi.serverlog.log.plugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;

public class AndroidLogModify implements Opcodes {

    public static final String ANDROID_LOG_CLASS = "com/xiaojinzi/serverlog/log/AndroidLog";

    public static byte[] doModify(InputStream inputStream) throws IOException {

        ClassWriter cw = new ClassWriter(0);

        ClassVisitor cv = new ClassVisitor(ASM5, cw) {

            private String classOwner;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                this.classOwner = name;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                AndroidLogMethodVisitor androidLogMethodVisitor = new AndroidLogMethodVisitor(mv, classOwner);
                return androidLogMethodVisitor;
            }
        };

        ClassReader cr = new ClassReader(inputStream);
        cr.accept(cv, ClassReader.SKIP_FRAMES);

        return cw.toByteArray();

    }

    // android/util/Log
    private static class AndroidLogMethodVisitor extends MethodVisitor {

        private String classOwner;

        public AndroidLogMethodVisitor(MethodVisitor mv, String classOwner) {
            super(ASM5, mv);
            this.classOwner = classOwner;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (!ANDROID_LOG_CLASS.equals(classOwner) && "android/util/Log".equals(owner)) {
                if (name != null) {
                    switch (name) {
                        case "v":
                        case "d":
                        case "i":
                        case "w":
                        case "e":
                            // System.out.println("classOwner = " + classOwner);
                            super.visitMethodInsn(opcode, ANDROID_LOG_CLASS, name, desc, itf);
                            return;
                    }
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

}

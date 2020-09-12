package com.demo.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.IRETURN;

public class AsmClassVisitor extends ClassVisitor {
    public AsmClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor annotationVisitor = super.visitAnnotation(descriptor, visible);
        if (descriptor.equals("Lcom/demo/annotation/Layout;")) {
            return new AnnotationVisitor(Opcodes.ASM6, annotationVisitor) {
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, value);
                    if (name.equals("value") && value instanceof Integer) {
                        int layoutResId = (Integer) value;

                        MethodVisitor methodVisitor = visitMethod(ACC_PUBLIC, "getContentViewId", "()I", null, null);
                        methodVisitor.visitCode();
                        Label label0 = new Label();
                        methodVisitor.visitLabel(label0);
                        methodVisitor.visitLineNumber(20, label0);
                        methodVisitor.visitLdcInsn(layoutResId);
                        methodVisitor.visitInsn(IRETURN);
                        Label label1 = new Label();
                        methodVisitor.visitLabel(label1);
                        methodVisitor.visitLocalVariable("this", "Lcom/demo/app/home/HomeActivity;", null, label0, label1, 0);
                        methodVisitor.visitMaxs(1, 1);
                        methodVisitor.visitEnd();
                    }
                }
            };
        }
        return annotationVisitor;
    }
}

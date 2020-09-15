package com.demo.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MyClassVisitor extends ClassVisitor implements Opcodes {
    private String classDescriptor;
    private String[] split;

    public MyClassVisitor(ClassVisitor classVisitor, String classDescriptor) {
        super(Opcodes.ASM7, classVisitor);
        this.classDescriptor = classDescriptor;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        if (signature == null || !signature.contains("<")) {
            return;
        }
        split = signature.substring(signature.indexOf('<') + 1, signature.indexOf('>')).split(";");
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
                        addGetContentViewIdMethod((Integer) value);
                    }
                }
            };
        }
        return annotationVisitor;
    }

    @Override
    public void visitEnd() {
        if (split != null && split.length == 2) {
            addBindViewModelMethod();
        }
        super.visitEnd();
    }

    private void addGetContentViewIdMethod(int layoutResId) {
        MethodVisitor methodVisitor = visitMethod(ACC_PUBLIC, "getContentViewId", "()I", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(20, label0);
        methodVisitor.visitLdcInsn(layoutResId);
        methodVisitor.visitInsn(IRETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", classDescriptor, null, label0, label1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void addBindViewModelMethod() {
        // com/demo/app/home/HomeActivity
        String classPathSub = classDescriptor.substring(1, classDescriptor.length() - 1);

        MethodVisitor methodVisitor = visitMethod(ACC_PUBLIC, "bindViewModel", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(40, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, classPathSub, "binding", "Landroidx/databinding/ViewDataBinding;");

        String bindingClass = split[0].substring(1);
        String viewModelClass = split[1].substring(1);

        methodVisitor.visitTypeInsn(CHECKCAST, bindingClass);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, classPathSub, "viewModel", "Landroidx/lifecycle/AndroidViewModel;");
        methodVisitor.visitTypeInsn(CHECKCAST, viewModelClass);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, bindingClass, "setViewModel", "(L" + viewModelClass + ";)V", false);
        methodVisitor.visitInsn(RETURN);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLocalVariable("this", classDescriptor, null, label0, label2, 0);
        methodVisitor.visitMaxs(2, 1);
        methodVisitor.visitEnd();
    }
}

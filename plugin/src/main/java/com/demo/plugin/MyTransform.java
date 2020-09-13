package com.demo.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MyTransform extends Transform {
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        if (!isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }

        for (TransformInput transformInput : transformInvocation.getInputs()) {
            for (JarInput jarInput : transformInput.getJarInputs()) {
                transformJar(transformInvocation, jarInput);
            }
            for (DirectoryInput directoryInput : transformInput.getDirectoryInputs()) {
                transformDirectory(transformInvocation, directoryInput);
            }
        }
    }

    /**
     * 对jar文件进行处理
     *
     * @param jarInput 例:lib
     */
    private void transformJar(TransformInvocation transformInvocation, JarInput jarInput) throws IOException {
        // /Users/guoxiaodong/Demos/AsmPluginDemo/lib/build/.transforms/e7f67a5d64774e8a4c1ce814209cae10/jetified-lib.jar
        File jarInputFile = jarInput.getFile();
        String hexName = DigestUtils.md5Hex(jarInputFile.getAbsolutePath()).substring(0, 8);

        String destName = jarInputFile.getName();
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4);
        }

        File dest = transformInvocation.getOutputProvider().getContentLocation(// 获取输出路径
                destName + "_" + hexName,
                jarInput.getContentTypes(),// [CLASSES]
                jarInput.getScopes(),// [SUB_PROJECTS]
                Format.JAR
        );
        JarFile originJar = new JarFile(jarInputFile);
        // /Users/guoxiaodong/Demos/AsmPluginDemo/app/build/tmp/transformClassesWithAsmTransformForDebug
        File tempDir = transformInvocation.getContext().getTemporaryDir();
        // /Users/guoxiaodong/Demos/AsmPluginDemo/app/build/tmp/transformClassesWithAsmTransformForDebug/temp_R.jar
        File outputJar = new File(tempDir, "temp_" + jarInputFile.getName());
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));

        // 遍历原jar文件寻找class文件
        Enumeration<JarEntry> enumeration = originJar.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry originEntry = enumeration.nextElement();
            // com/demo/asm/lib/MethodObservable.class
            String classPath = originEntry.getName();
            if (classPath.endsWith(".class")) {
                JarEntry destEntry = new JarEntry(classPath);
                jarOutputStream.putNextEntry(destEntry);

                InputStream inputStream = originJar.getInputStream(originEntry);
                byte[] sourceBytes = IOUtils.toByteArray(inputStream);

                // 修改class文件内容
                byte[] modifiedBytes = null;
                if (filterModifyClass(classPath)) {// com/demo/module/ModuleActivity.class
                    System.out.println("Jar Class-->" + classPath);
                    modifiedBytes = modifyClass(sourceBytes, classPath);
                }
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                jarOutputStream.write(modifiedBytes);
            }
            jarOutputStream.closeEntry();
        }
        jarOutputStream.close();
        originJar.close();
        // 复制修改后jar到输出路径
        FileUtils.copyFile(outputJar, dest);
    }

    /**
     * 对directory进行处理
     *
     * @param directoryInput app/build/intermediates/javac/debug/classes
     */
    private void transformDirectory(TransformInvocation transformInvocation, DirectoryInput directoryInput) throws IOException {
        File directoryInputFile = directoryInput.getFile();
        // app/build/intermediates/transforms/MyTransform/debug/34
        File transformDir = transformInvocation.getOutputProvider().getContentLocation(
                directoryInput.getName(),// 697da665ad33881799cfc4f0d6ee2efd6010a5bc
                directoryInput.getContentTypes(),// [CLASSES]
                directoryInput.getScopes(),// [PROJECT]
                Format.DIRECTORY
        );
        if (!directoryInputFile.exists()) {
            return;
        }
        // classes目录下的所有class文件先复制到transform目录下
        FileUtils.copyDirectory(directoryInputFile, transformDir);
        traversalDirectory(transformDir, transformDir);
    }

    /**
     * 遍历{@param transformDir}目录下要修改的class文件，修改后，覆盖原文件
     *
     * @param transformDir app/build/intermediates/transforms/MyTransform/debug/34
     * @param dir          app/build/intermediates/transforms/MyTransform/debug/34/com/demo/app/home
     */
    private void traversalDirectory(File transformDir, File dir) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                traversalDirectory(transformDir, file);
            } else if (file.getAbsolutePath().endsWith(".class")) {
                // com/demo/app/home/HomeActivity.class
                String classPath = file.getAbsolutePath().replace(transformDir.getAbsolutePath() + File.separator, "");
                if (filterModifyClass(classPath)) {
                    System.out.println("Dir Class-->" + classPath);
                    byte[] sourceBytes = IOUtils.toByteArray(new FileInputStream(file));
                    byte[] modifiedBytes = modifyClass(sourceBytes, classPath);
                    if (modifiedBytes != null) {
                        FileOutputStream outputStream = new FileOutputStream(file, false);
                        outputStream.write(modifiedBytes);
                        outputStream.close();
                    }
                }
            }
        }
    }

    /**
     * 判断是否修改
     */
    private boolean filterModifyClass(String classPath) {
        return classPath.endsWith("Activity.class") && classPath.startsWith("com/") && !classPath.contains("$") && !classPath.endsWith("BaseActivity.class");
    }

    /**
     * 修改字节码文件
     */
    private byte[] modifyClass(byte[] classBytes, String classPath) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String classDescriptor = String.format("L%s;", classPath.replace(".class", ""));
        ClassVisitor asmClassVisitor = new MyClassVisitor(classWriter, classDescriptor);
        classReader.accept(asmClassVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    @Override
    public String getName() {
        return MyTransform.class.getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    /**
     * 是否开启增量编译
     */
    @Override
    public boolean isIncremental() {
        return false;
    }
}

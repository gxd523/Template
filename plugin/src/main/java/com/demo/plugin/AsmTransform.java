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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class AsmTransform extends Transform {
    private Map<String, File> modifyMap = new HashMap<>();

    @Override
    public String getName() {
        return AsmTransform.class.getSimpleName();
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

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        if (!isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }

        traversalInput(transformInvocation);
    }

    /**
     * 遍历输入，分别遍历其中的jar以及directory
     */
    private void traversalInput(TransformInvocation transformInvocation) throws IOException {
        // 获取输入（消费型输入，需要传递给下一个Transform）
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
            String classFilePath = originEntry.getName();
            if (classFilePath.endsWith(".class")) {
                JarEntry destEntry = new JarEntry(classFilePath);
                jarOutputStream.putNextEntry(destEntry);

                InputStream inputStream = originJar.getInputStream(originEntry);
                byte[] sourceBytes = IOUtils.toByteArray(inputStream);

                // 修改class文件内容
                byte[] modifiedBytes = null;
                if (filterModifyClass(classFilePath)) {
                    System.out.printf("Jar Modify Class-->%s\n", classFilePath);
                    modifiedBytes = modifyClass(sourceBytes);
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
     * @param directoryInput /Users/guoxiaodong/Demos/AsmPluginDemo/app/build/intermediates/javac/debug/classes
     */
    private void transformDirectory(TransformInvocation transformInvocation, DirectoryInput directoryInput) throws IOException {
        File directoryInputFile = directoryInput.getFile();
        // /Users/guoxiaodong/Demos/AsmPluginDemo/app/build/intermediates/transforms/AsmTransform/debug/2
        File dest = transformInvocation.getOutputProvider().getContentLocation(
                directoryInput.getName(),// 697da665ad33881799cfc4f0d6ee2efd6010a5bc
                directoryInput.getContentTypes(),// [CLASSES]
                directoryInput.getScopes(),// [PROJECT]
                Format.DIRECTORY
        );
        if (!directoryInputFile.exists()) {
            return;
        }
        traverseDirectory(
                directoryInputFile.getAbsolutePath(),
                transformInvocation.getContext().getTemporaryDir(),
                directoryInputFile
        );

        FileUtils.copyDirectory(directoryInputFile, dest);

        for (Map.Entry<String, File> entry : modifyMap.entrySet()) {
            File target = new File(dest.getAbsolutePath() + File.separatorChar + entry.getKey().replace('.', File.separatorChar) + ".class");
            if (target.exists()) {
                target.delete();
            }
            FileUtils.copyFile(entry.getValue(), target);
            entry.getValue().delete();
        }
    }

    /**
     * 遍历目录下面的class文件
     *
     * @param basedir 基准目录，和dir对比需要找到包路径
     * @param tempDir 需要写入的临时目录 /Users/guoxiaodong/Demos/AsmPluginDemo/app/build/tmp/transformClassesWithAsmTransformForDebug
     * @param dir     class文件目录
     */
    private void traverseDirectory(String basedir, File tempDir, File dir) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                traverseDirectory(basedir, tempDir, file);
            } else if (file.getAbsolutePath().endsWith(".class")) {
                String pathName = file.getAbsolutePath().replace(basedir + File.separator, "");
                String className = pathName.replace(File.separator, ".").replace(".class", "");
                byte[] sourceBytes = IOUtils.toByteArray(new FileInputStream(file));
                byte[] modifiedBytes = null;
                if (filterModifyClass(className + ".class")) {
                    System.out.printf("Modify dir-->%s.class\n", className);
                    modifiedBytes = modifyClass(sourceBytes);
                }
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                File modified = new File(tempDir, className + ".class");
                if (modified.exists()) {
                    modified.delete();
                }
                modified.createNewFile();
                new FileOutputStream(modified).write(modifiedBytes);
                modifyMap.put(className, modified);
            }
        }
    }

    /**
     * 判断是否修改
     *
     * @param classPath 例：com/demo/asm/lib/MethodObservable.class
     */
    private boolean filterModifyClass(String classPath) {
        return classPath.endsWith("Activity.class") && classPath.startsWith("com/") && !classPath.contains("$");
    }

    /**
     * 修改字节码文件
     */
    private byte[] modifyClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor asmClassVisitor = new AsmClassVisitor(classWriter);
        classReader.accept(asmClassVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }
}

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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * transform目录  app/build/intermediates/transforms/MyTransform/debug
 * temp目录       app/build/tmp/transformClassesWithMyTransformForDebug
 * class文件目录   app/build/intermediates/javac/debug/classes
 * jar包目录
 */
public class MyTransform extends Transform {
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        if (!isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }
        long start = System.currentTimeMillis();
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        List<QualifiedContent> taskList = new ArrayList<>();
        for (TransformInput transformInput : transformInvocation.getInputs()) {
            taskList.addAll(transformInput.getJarInputs());
            taskList.addAll(transformInput.getDirectoryInputs());
        }
        CountDownLatch countDownLatch = new CountDownLatch(taskList.size());
        for (QualifiedContent task : taskList) {
            threadPool.execute(() -> {
                try {
                    if (task instanceof JarInput) {
                        transformJar(transformInvocation, (JarInput) task);
                    } else if (task instanceof DirectoryInput) {
                        transformDirectory(transformInvocation, (DirectoryInput) task);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }
        System.out.printf("%s个任务耗时...%s\n", taskList.size(), System.currentTimeMillis() - start);
    }

    /**
     * 对jar文件进行处理
     *
     * @param jarInput 包括以下4种目录
     *                 /Users/guoxiaodong/.gradle/caches/transforms-2/files-2.1/a48b2b4f5ed90ca7cdf39866c0f8be53/databinding-adapters-4.0.1-runtime.jar
     *                 /Users/guoxiaodong/.gradle/caches/modules-2/files-2.1/androidx.databinding/databinding-common/4.0.1/cdca8698ee545b79d656a4f1eeb103f60fad3ed1/databinding-common-4.0.1.jar
     *                 /Users/guoxiaodong/Demos/Template/base/build/intermediates/runtime_library_classes_jar/debug/classes.jar
     *                 /Users/guoxiaodong/Demos/Template/app/build/intermediates/compile_and_runtime_not_namespaced_r_class_jar/debug/R.jar
     */
    private void transformJar(TransformInvocation transformInvocation, JarInput jarInput) throws IOException {
        File originJarFile = jarInput.getFile();
        // databinding-adapters-4.0.1-runtime_d6cd7b73
        String destName = originJarFile.getName().replace(".jar", "") + "_" + DigestUtils.md5Hex(originJarFile.getAbsolutePath()).substring(0, 8);
        // app/build/intermediates/transforms/MyTransform/debug/0.jar
        File transformJarFile = transformInvocation.getOutputProvider().getContentLocation(
                destName,
                jarInput.getContentTypes(),// [CLASSES]
                jarInput.getScopes(),// [SUB_PROJECTS]
                Format.JAR
        );
        JarOutputStream transformJarOutputStream = new JarOutputStream(new FileOutputStream(transformJarFile));

        // 遍历原jar文件寻找class文件
        JarFile originJar = new JarFile(originJarFile);
        Enumeration<JarEntry> enumeration = originJar.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry originEntry = enumeration.nextElement();
            // com/demo/asm/lib/MethodObservable.class
            String classPath = originEntry.getName();
            if (classPath.endsWith(".class")) {
                JarEntry destEntry = new JarEntry(classPath);
                transformJarOutputStream.putNextEntry(destEntry);

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
                transformJarOutputStream.write(modifiedBytes);
            }
            transformJarOutputStream.closeEntry();
        }
        transformJarOutputStream.close();
        originJar.close();
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

    /**
     * @return 决定transform目录、temp目录的名字
     */
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

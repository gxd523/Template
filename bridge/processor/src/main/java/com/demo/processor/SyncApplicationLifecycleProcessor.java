package com.demo.processor;

import com.demo.annotation.ModuleSpec;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SyncApplicationLifecycleProcessor extends AbstractProcessor {
    public static final String GENERATE_PACKAGE = "com.demo.generate.lifecycle";
    private Filer filer;
    private Types typeUtils;
    private Elements elementUtils;
    private String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        Map<String, String> optionMap = processingEnvironment.getOptions();
        if (!Util.isEmpty(optionMap)) {
            moduleName = optionMap.get("moduleName");
        }
    }

    /**
     * 每个使用注解处理器的module都会走一遍
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (Util.isEmpty(set)) {
            return false;
        }
        Set<? extends Element> elementWithAnnotateSet = roundEnvironment.getElementsAnnotatedWith(ModuleSpec.class);
        if (Util.isEmpty(elementWithAnnotateSet)) {
            return false;
        }
        try {
            processAnnotation(elementWithAnnotateSet);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void processAnnotation(Set<? extends Element> elementWithAnnotateSet) throws IOException {
        Set<String> bridgeImplClassNameSet = new HashSet<>();
        TypeElement applicationLifecycleObserverElement = elementUtils.getTypeElement("com.demo.bridge.ApplicationLifecycleObserver");
        for (Element element : elementWithAnnotateSet) {
            if (!typeUtils.isSubtype(element.asType(), applicationLifecycleObserverElement.asType())) {
                throw new RuntimeException("错误的使用了Bridge注解..." + element);
            }
            bridgeImplClassNameSet.add(element.toString());
        }

        generateClass(bridgeImplClassNameSet);
    }

    private void generateClass(Set<String> bridgeImplClassNameSet) throws IOException {
        MethodSpec methodSpec = generateInitMethod(bridgeImplClassNameSet);

        TypeSpec classTypeSpec = TypeSpec.classBuilder(String.format("%sApplicationLifecycle", moduleName))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();
        JavaFile.builder(GENERATE_PACKAGE, classTypeSpec)
                .build()
                .writeTo(filer);
    }

    private MethodSpec generateInitMethod(Set<String> bridgeImplClassNameSet) {
        ParameterSpec parameterSpec = ParameterSpec.builder(ClassName.get("com.demo.bridge", "ApplicationLifecycleOwner"), "owner", Modifier.FINAL).build();

        MethodSpec.Builder initMethodSpec = MethodSpec.methodBuilder("addObserver")
                .addParameter(parameterSpec)
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception.class);
        for (String bridgeImplType : bridgeImplClassNameSet) {
            // ApplicationLifecycleOwner.INSTANCE.addObserver(new ModuleApplication());
            initMethodSpec.addStatement(
                    "$T.INSTANCE.addObserver(new $T());",
                    ClassName.get("com.demo.bridge", "ApplicationLifecycleOwner"),
                    ClassName.get(bridgeImplType.substring(0, bridgeImplType.lastIndexOf(".")), bridgeImplType.substring(bridgeImplType.lastIndexOf(".") + 1))
            );
        }
        return initMethodSpec.build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ModuleSpec.class.getCanonicalName());
    }
}
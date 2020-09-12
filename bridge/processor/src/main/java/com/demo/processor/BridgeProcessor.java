package com.demo.processor;

import com.demo.annotation.Bridge;
import com.demo.annotation.IBridge;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
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
public class BridgeProcessor extends AbstractProcessor {
    public static final String GENERATE_PACKAGE = "com.demo.generate.bridge";
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
        Set<? extends Element> elementWithAnnotateSet = roundEnvironment.getElementsAnnotatedWith(Bridge.class);
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
        Set<TypeElement> bridgeImplClassNameSet = new HashSet<>();
        TypeElement bridgeElement = elementUtils.getTypeElement(IBridge.class.getName());
        for (Element element : elementWithAnnotateSet) {
            if (!typeUtils.isSubtype(element.asType(), bridgeElement.asType())) {
                throw new RuntimeException("错误的使用了Bridge注解..." + element);
            }
            bridgeImplClassNameSet.add(((TypeElement) element));
        }

        generateClass(bridgeImplClassNameSet);
    }

    private void generateClass(Set<TypeElement> bridgeImplClassNameSet) throws IOException {
        MethodSpec methodSpec = generateInitMethod(bridgeImplClassNameSet);

        TypeSpec classTypeSpec = TypeSpec.classBuilder(String.format("%sBridge", moduleName))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();
        JavaFile.builder(GENERATE_PACKAGE, classTypeSpec)
                .build()
                .writeTo(filer);
    }

    private MethodSpec generateInitMethod(Set<TypeElement> bridgeImplClassNameSet) {
        // private Map<String, IBridge>
        ParameterizedTypeName bridgeMapTypeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(IBridge.class)
        );

        MethodSpec.Builder initMethodSpec = MethodSpec.methodBuilder("addBridge")
                .addParameter(bridgeMapTypeName, "bridgeMap", Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception.class);
        for (TypeElement bridgeImplType : bridgeImplClassNameSet) {
            // Class<? extends IBridge> aaa = (Class<? extends IBridge>) Class.forName("aaa");
            initMethodSpec.addStatement(
                    "$T<? extends $T> $N = ($T<? extends $T>) $T.forName($S)",
                    ClassName.get(Class.class),
                    ClassName.get(IBridge.class),
                    bridgeImplType.getSimpleName(),
                    ClassName.get(Class.class),
                    ClassName.get(IBridge.class),
                    ClassName.get(Class.class),
                    bridgeImplType
            );
            // bridgeMap.put("com.demo.app.AppBridge", AppBridgeImpl.newInstance());
            initMethodSpec.addStatement(
                    "bridgeMap.put($S, $N.newInstance())",
                    bridgeImplType.getInterfaces().get(0),
                    bridgeImplType.getSimpleName()
            );
        }
        return initMethodSpec.build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Bridge.class.getCanonicalName());
    }
}
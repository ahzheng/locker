package com.popdue.locker;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LockImportSeletor implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        // 存在注解扫描
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(ComponentScan.class.getName());
        String[] basePackages = null;
        if (null != annotationAttributes) {
            basePackages = (String[]) annotationAttributes.get("basePackages");
        }

        // 不存在注解扫描默认
        if (null == annotationAttributes || annotationAttributes.size() == 0) {
            basePackages = new String[]{"com.popdue.locker.aspect"};
        }

        if (null == basePackages) {
            return new String[0];
        }

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        // 这里实现包含,相当@ComponentScan  includeFilters
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

        // 这里可以实现排除，相当@ComponentScan  excludeFilters
        // scanner.addExcludeFilter(new AssignableTypeFilter(Object.class));

        Set<String> data = new HashSet<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            candidateComponents.forEach(e -> {
                data.add(e.getBeanClassName());
            });
        }

        return data.toArray(new String[data.size()]);
    }
}

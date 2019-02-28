package hu.blackbelt.judo.meta.liquibase;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

public interface LiquibaseMetaModel {

    Resource.Factory getFactory();

    void registerExpressionMetamodel(ResourceSet resourceSet);
}

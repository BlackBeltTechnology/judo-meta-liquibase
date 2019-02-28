package hu.blackbelt.judo.meta.liquibase;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import hu.blackbelt.judo.meta.liquibase.LiquibasePackage;
import hu.blackbelt.judo.meta.liquibase.util.LiquibaseResourceFactoryImpl;

import java.util.Dictionary;
import java.util.Hashtable;

@Component(immediate = true, service = LiquibaseMetaModel.class)
public class LiquibaseMetaModelRegistration implements LiquibaseMetaModel {

    ServiceRegistration<Resource.Factory> liquibaseFactoryRegistration;
    Resource.Factory factory;

    @Activate
    public void activate(ComponentContext componentContext) {
        Dictionary<String, Object> params = new Hashtable<>();
        params.put("meta", "psm");
        params.put("version", componentContext.getBundleContext().getBundle().getVersion());
        params.put("bundle", componentContext.getBundleContext().getBundle());

        factory = new LiquibaseResourceFactoryImpl();
        liquibaseFactoryRegistration = componentContext.getBundleContext()
                .registerService(Resource.Factory.class, factory, params);
    }

    @Deactivate
    public void deactivate() {
        liquibaseFactoryRegistration.unregister();
    }

    @Override
    public Resource.Factory getFactory() {
        return factory;
    }

    @Override
    public void registerExpressionMetamodel(ResourceSet resourceSet) {
        resourceSet.getPackageRegistry().put(LiquibasePackage.eINSTANCE.getNsURI(), LiquibasePackage.eINSTANCE);
    }
}

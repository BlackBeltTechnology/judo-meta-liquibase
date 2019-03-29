package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.judo.meta.liquibase.LiquibasePackage;
import hu.blackbelt.judo.meta.liquibase.util.LiquibaseResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LiquibaseModelLoader {

    public static void registerLiquibaseMetamodel(ResourceSet resourceSet) {
        resourceSet.getPackageRegistry().put(LiquibasePackage.eINSTANCE.getNsURI(), LiquibasePackage.eINSTANCE);
    }


    public static Resource.Factory getLiquibaseFactory() {
        return new LiquibaseResourceFactoryImpl();
    }

    public static ResourceSet createLiquibaseResourceSet() {
        return createLiquibaseResourceSet(null);
    }

    public static ResourceSet createLiquibaseResourceSet(URIHandler uriHandler) {
        ResourceSet resourceSet = new ResourceSetImpl();
        registerLiquibaseMetamodel(resourceSet);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(ResourceFactoryRegistryImpl.DEFAULT_EXTENSION, getLiquibaseFactory());
        if (uriHandler != null) {
            resourceSet.getURIConverter().getURIHandlers().add(0, uriHandler);
        }
        return resourceSet;
    }


    public static LiquibaseModel loadLiquibaseModel(URI uri, String name, String version) throws IOException {
        return loadLiquibaseModel(createLiquibaseResourceSet(), uri, name, version, null, null);
    }

    public static LiquibaseModel loadLiquibaseModel(ResourceSet resourceSet, URI uri, String name, String version) throws IOException {
        return loadLiquibaseModel(resourceSet, uri, name, version, null, null);
    }

    public static LiquibaseModel loadLiquibaseModel(ResourceSet resourceSet, URI uri, String name, String version, String checksum, String acceptedMetaVersionRange) throws IOException {
        registerLiquibaseMetamodel(resourceSet);
        Resource resource = resourceSet.createResource(uri);
        Map<Object, Object> loadOptions = new HashMap<>();
        //loadOptions.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
        //loadOptions.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);
        resource.load(loadOptions);

        LiquibaseModel.LiquibaseModelBuilder b = LiquibaseModel.buildLiquibaseModel();

        b.name(name)
                .version(version)
                .uri(uri)
                .checksum(checksum)
                .resourceSet(resourceSet);

        if (checksum != null) {
            b.checksum(checksum);
        }

        if (acceptedMetaVersionRange != null)  {
            b.metaVersionRange(acceptedMetaVersionRange);
        }
        return b.build();
    }

}

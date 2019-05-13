package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.judo.meta.liquibase.LiquibasePackage;
import hu.blackbelt.judo.meta.liquibase.impl.LiquibasePackageImpl;
import hu.blackbelt.judo.meta.liquibase.util.LiquibaseResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LiquibaseModelLoader {

    public static void registerLiquibaseMetamodel(ResourceSet resourceSet) {
        //        .getEFactory(LiquibasePackage.eNS_URI);

        resourceSet.getPackageRegistry().put(LiquibasePackage.eINSTANCE.getNsURI(), LiquibasePackageImpl.eINSTANCE);
        // TODO: Find a way to set the schema locaation.
        // http://eclipsedriven.blogspot.com/2010/12/saving-xsischemalocation-into-emf-xmi.html
        /*
        How can I ensure that an xsi:schemaLocation is serialized for my packages?
        The save option XMLResource.OPTION_SCHEMA_LOCATION must be used to indicate that you want to serialize schema
        location information. Even when this option is used, the schema location is serialized only if the
        ePackage.getNsURI() is different from ePackage.eResource().getURI() to avoid useless schema locations of the
        form xsi:schemaLocation="http://www.example.org/Xyz http://www.example.org/Xyz". For a generated package,
        the initializePackageContents contains this:

        // Create resource
        createResource(eNS_URI);
        Adding a method like this:

        @Override
        protected Resource createResource(String uri)
        {
          return super.createResource("http://www.example.org/Xyz.ecore");
        }
        is a good way to specify a physical location of the schema. It's best to specify an absolute URI as the
        location. EMF will serialize a relative location if there is a relative path from the URI of document being
        serialized to the schema location itself.
        */
        // LiquibasePackageImpl.eINSTANCE("http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd");
    }


    public static Resource.Factory getLiquibaseFactory() {
        return new LiquibaseResourceFactoryImpl();
    }

    public static ResourceSet createLiquibaseResourceSet() {
        return createLiquibaseResourceSet(null);
    }

    public static ResourceSet createLiquibaseResourceSet(URIHandler uriHandler) {
        ResourceSet resourceSet = new ResourceSetImpl() {
            @Override
            public Resource createResource(URI uri) {
                return super.createResource(uri);
            }

            @Override
            public Resource createResource(URI uri, String contentType) {
                return super.createResource(uri, contentType);
            }
        };
        registerLiquibaseMetamodel(resourceSet);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(ResourceFactoryRegistryImpl.DEFAULT_EXTENSION, getLiquibaseFactory());
        if (uriHandler != null) {
            resourceSet.getURIConverter().getURIHandlers().add(0, new LiquibaseNamespaceFixUriHandler(uriHandler));
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


    public static Map<Object, Object> getLiquibaseDefaultSaveOptions() {
        final Map<Object, Object> saveOptions = new HashMap<>();
        saveOptions.put(XMLResource.OPTION_DECLARE_XML,Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF,XMIResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);

        /*
        saveOptions.put(XMLResource.OPTION_URI_HANDLER, new URIHandlerImpl() {
            @Override
            public URI deresolve(URI uri) {
                if (uri.hasFragment() && uri.hasOpaquePart() && baseURI.hasOpaquePart()) {
                    if (uri.opaquePart().equals(baseURI.opaquePart())) {
                        return URI.createURI("#" + uri.fragment());
                    }
                }
                return super.deresolve(uri);
            }
        });
        */
        saveOptions.put(XMLResource.OPTION_SCHEMA_LOCATION,Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION,Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_SKIP_ESCAPE_URI,Boolean.FALSE);
        saveOptions.put(XMIResource.OPTION_ENCODING,"UTF-8");
        return saveOptions;
    }


    public static void saveLiquibaseModel(LiquibaseModel liquibaseModel) throws IOException {
        liquibaseModel.getResourceSet().getResource(liquibaseModel.getUri(), false).save(getLiquibaseDefaultSaveOptions());
    }

}

package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;

import org.eclipse.epsilon.common.util.UriUtil;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.net.URISyntaxException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;

public class LiquibaseEpsilonValidator {
	
	public static void validateLiquibase(Log log,
										LiquibaseModel liquibaseModel,
										URI scriptRoot) throws ScriptExecutionException, URISyntaxException {
		validateLiquibase(log, liquibaseModel, scriptRoot, emptyList(), emptyList());
	}
	
	public static void validateLiquibase(Log log,
										LiquibaseModel liquibaseModel,
										URI scriptRoot,
										Collection<String> expectedErrors,
										Collection<String> expectedWarnings) throws ScriptExecutionException, URISyntaxException {
		
		ExecutionContext executionContext = executionContextBuilder()
				.log(log)
				.resourceSet(liquibaseModel.getResourceSet())
				.metaModels(emptyList())
				.modelContexts(Arrays.asList(
							wrappedEmfModelContextBuilder()
									.log(log)
									.name("LIQUIBASE")
									.validateModel(false)
									.resource(liquibaseModel.getResource())
									.build()))
				.injectContexts(singletonMap("liquibaseUtils", new LiquibaseUtils(liquibaseModel.getResourceSet())))
				.build();
		
		try {
			executionContext.load();
			
			executionContext.executeProgram(
					evlExecutionContextBuilder()
							.source(UriUtil.resolve("liquibase.evl", scriptRoot))
							.expectedErrors(expectedErrors)
							.expectedWarnings(expectedWarnings)
							.build());
		} finally {
			executionContext.commit();
			try {
				executionContext.close();
			} catch (Exception e) {
			}
		}
	}
}

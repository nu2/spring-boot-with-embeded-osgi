package ge.vakho.spring_boot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ge.vakho.spring_boot.aop.InsertBundleEntryFailMarker;
import ge.vakho.spring_boot.aop.InstallBundleFailMarker;
import ge.vakho.spring_boot.configuration.BundleConfig;

/**
 * Serves as API for bundle installation, start, stop and deletion.
 * 
 * @author v.laluashvili
 */
@Service
public class BundleInstallService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BundleInstallService.class);

	private final BundleConfig bundleConfig;
	private final BundleContext bundleContext;
	
	@Autowired
	public BundleInstallService(BundleConfig bundleConfig, BundleContext bundleContext) {
		this.bundleConfig = bundleConfig;
		this.bundleContext = bundleContext;
	}

	public Path createJarFileInBundlesFolder(String fileName, byte[] jarBytes) throws IOException {
		Path newBundle = bundleConfig.getBundleFolder().resolve(fileName);
		LOGGER.debug("Creating JAR file: {} in configuration folder...", newBundle.toAbsolutePath());
		newBundle = Files.write(newBundle, jarBytes);
		LOGGER.debug("Created JAR file: {} in configuration folder", newBundle.toAbsolutePath());
		return newBundle;
	}
	
	@InstallBundleFailMarker
	public Bundle installBundleFromPath(Path newBundlePath) throws BundleException {
		LOGGER.debug("Installing bundle file: {} ...", newBundlePath.toAbsolutePath());
		Bundle installedBundle = bundleContext.installBundle("file:" + newBundlePath.toAbsolutePath().toString());
		LOGGER.debug("Installed bundle: {} with bundle id: {}", installedBundle.getSymbolicName(), installedBundle.getBundleId());
		return installedBundle;
	}
	
	@InsertBundleEntryFailMarker
	public void insertEntryInConfigurationFile(Bundle bundle, Path newBundlePath) throws JsonGenerationException, JsonMappingException, IOException {
		LOGGER.debug("Inserting new bundle: {} entry in configuration file as: {}...", bundle.getSymbolicName(), newBundlePath.getFileName());
		
		String fileName = newBundlePath.getFileName().toString();
		
		// Remove old entry (if it exists)
		bundleConfig.setEntry(bundle.getBundleId(), fileName);
		
		LOGGER.debug("Inserted new bundle entry: {} as: {} in configuration file", bundle.getSymbolicName(), fileName);
	}	
	
}
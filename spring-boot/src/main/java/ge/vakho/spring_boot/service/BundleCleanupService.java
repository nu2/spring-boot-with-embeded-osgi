package ge.vakho.spring_boot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import ge.vakho.spring_boot.property.BundleProperties;

/**
 * Methods for cleanup operations.
 * 
 * @author v.laluashvili
 */
@Service
@EnableConfigurationProperties(BundleProperties.class)
public class BundleCleanupService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BundleCleanupService.class);

	private final BundleProperties bundleProperties;
	private final BundleConfigFile bundleConfigFile;

	@Autowired
	public BundleCleanupService(BundleProperties bundleProperties, BundleConfigFile bundleConfigFile) {
		this.bundleProperties = bundleProperties;
		this.bundleConfigFile = bundleConfigFile;
	}

	public void removeJar(String fileName) {
		removeJar(bundleProperties.getFolderPath().resolve(fileName));
	}

	public void removeJar(Path bundlePath) {
		LOGGER.debug("Removing bundle JAR: {} from bundle's folder...", bundlePath.getFileName());
		try {
			if (Files.deleteIfExists(bundlePath)) {
				LOGGER.debug("Successfully removed bundle JAR: {} from bundle's folder", bundlePath.getFileName());
			} else {
				LOGGER.warn("Couldn't remove bundle JAR: {} from bundle's folder!", bundlePath.getFileName());
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't remove bundle JAR file!", e);
		}
	}

	public void uninstallBundle(Bundle newInstalledBundle) {
		LOGGER.debug("Uninstalling bundle: {} from framework...", newInstalledBundle.getSymbolicName());
		try {
			newInstalledBundle.uninstall(); // Stops and then uninstalls
		} catch (BundleException e) {
			LOGGER.error("Couldn't uninstall bundle: " + newInstalledBundle.getSymbolicName() + " from framework", e);
			return;
		}

		// Remove entry from in-memory configuration
		bundleConfigFile.removeBy(newInstalledBundle.getBundleId());
		LOGGER.debug("Successfully uninstalled bundle: {} from framework", newInstalledBundle.getSymbolicName());
	}

}
/*
 * Copyright © 2025 delker <jd_github+maven-jinja-filter@onix.de>
 *
 * This file is part of the maven-jinja-filtering project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * File: de.delker.maven.resources.filtering.JinjaMavenFileFilter
 * Last Updated: 2025-08-22 10:35:09
 */

package de.delker.maven.resources.filtering;

import com.hubspot.jinjava.Jinjava;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.filtering.AbstractMavenFilteringRequest;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.FilterWrapper;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Jinja Maven File Filter
 *
 * @author jdelker
 */
@Singleton
@Named("jinjaFileFilter")
public class JinjaMavenFileFilter extends DefaultMavenFileFilter {

    /**
     * Jinja file extension
     */
    public static final String JINJA_EXT = ".j2";

    private List<FilterWrapper> jinjaFilterWrappers;

    @Inject
    public JinjaMavenFileFilter(BuildContext buildContext) {
        super(buildContext);
    }

    /** {@inheritDoc} */
    @Override
    public List<FilterWrapper> getDefaultFilterWrappers(AbstractMavenFilteringRequest request) throws MavenFilteringException {
        var defaultWrappers = super.getDefaultFilterWrappers(request);

        // Initialize Jinja wrapper
        if (jinjaFilterWrappers == null) {
            try {
                jinjaFilterWrappers = new ArrayList<>(defaultWrappers);
                jinjaFilterWrappers.add(new Wrapper(
                        getProperties(request)
                ));
            } catch (IOException e) {
                throw new MavenFilteringException("Failed to initialize jinjaFilterWrapper", e);
            }
        }

        // Return only the default wrappers. The JinjaWrapper will be used selectively.
        return defaultWrappers;
    }

    /** {@inheritDoc} */
    @Override
    public void copyFile(
            File from,
            File to,
            boolean filtering,
            List<FilterWrapper> filterWrappers,
            String encoding,
            boolean overwrite)
            throws MavenFilteringException {

        // if "to" ends in JINJA_EXT then remove extension and apply JinjaFilterWrappers
        String toFileName = to.getName();
        if (filtering && toFileName.endsWith(JINJA_EXT)) {
            File newTo = new File(to.getParentFile(), StringUtils.removeEnd(toFileName, JINJA_EXT));
            super.copyFile(
                    from,
                    newTo,
                    true,
                    jinjaFilterWrappers,
                    encoding,
                    overwrite);

        } else {
            // otherwise do normal file copy
            super.copyFile(from, to, filtering, filterWrappers, encoding, overwrite);
        }
    }

    private Properties getProperties(AbstractMavenFilteringRequest request) throws IOException {

        Properties props = new Properties();

        // 1. Load properties from <filters> files
        if (request.getFilters() != null) {
            for (String filterPath : request.getFilters()) {
                File filterFile = new File(filterPath);
                if (filterFile.exists()) {
                    try (FileReader reader = new FileReader(filterFile)) {
                        Properties fileProps = new Properties();
                        fileProps.load(reader);
                        props.putAll(fileProps);
                    }
                }
            }
        }

        // 2. Project properties
        if (request.getMavenProject() != null) {
            props.putAll(request.getMavenProject().getProperties());
        }

        // 3. Session properties (system + user)
        if (request.getMavenSession() != null) {
            props.putAll(request.getMavenSession().getSystemProperties());
            props.putAll(request.getMavenSession().getUserProperties());
        }

        // 4. Additional properties (internal)
        if (request.getAdditionalProperties() != null) {
            props.putAll(request.getAdditionalProperties());
        }

        return props;
    }

    private static final class Wrapper extends FilterWrapper {

        private final Properties properties;
        private final Jinjava jinjava = new Jinjava();

        Wrapper(Properties properties) {
            super();
            this.properties = properties;
        }

        @Override
        public Reader getReader(Reader reader) {
            // Read all template content into a String
            String template;

            try {
                template = IOUtils.toString(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            // Build template bindings
            var bindings = getBindings(properties);

            // Render template
            String rendered = jinjava.render(template, bindings);

            // Return as Reader
            return new StringReader(rendered);
        }

        // Convert properties to map
        private Map<String, String> getBindings(Properties prop) {
            return prop == null ? null : prop.entrySet().stream().collect(
                    Collectors.toMap(
                            e -> String.valueOf(e.getKey()).replace('.', '_'),
                            e -> String.valueOf(e.getValue()),
                            (prev, next) -> next, HashMap::new
                    ));
        }
    }
}

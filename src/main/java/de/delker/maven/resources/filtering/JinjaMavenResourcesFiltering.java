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
 * File: de.delker.maven.resources.filtering.JinjaMavenResourcesFiltering
 * Last Updated: 2025-08-22 10:35:09
 */

package de.delker.maven.resources.filtering;

import org.apache.maven.shared.filtering.DefaultMavenResourcesFiltering;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Jinja Maven Resource Filter
 *
 * @author jdelker
 */
@Singleton
@Named("default") // register as the default MavenResourceFiltering class through Maven Sisu
public class JinjaMavenResourcesFiltering extends DefaultMavenResourcesFiltering {

    @Inject
    public JinjaMavenResourcesFiltering(@Named("jinjaFileFilter") MavenFileFilter mavenFileFilter, BuildContext buildContext) {
        super(mavenFileFilter, buildContext);
    }

}

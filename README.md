# maven-jinja-filter

<!-- Badges / Banner Tags -->
<p>
  <a href="https://search.maven.org/artifact/de.delker.maven/maven-jinja-filter"><img src="https://img.shields.io/maven-central/v/de.delker.maven/maven-jinja-filter" alt="Maven Central"></a>
  <a href="https://github.com/jdelker/maven-jinja-filter/releases"><img src="https://img.shields.io/github/v/release/jdelker/maven-jinja-filter" alt="Latest Release"></a>
  <a href="https://github.com/jdelker/maven-jinja-filter/blob/main/LICENSE"><img src="https://img.shields.io/github/license/jdelker/maven-jinja-filter" alt="License"></a>
  <a href="https://github.com/jdelker/maven-jinja-filter/issues"><img src="https://img.shields.io/github/issues/jdelker/maven-jinja-filter" alt="Issues"></a>
</p>

<img src="https://raw.githubusercontent.com/jdelker/maven-jinja-filter/refs/heads/main/logo.svg" alt="maven-jinja-filter">

A Maven Jinja filter module for `maven-resource-plugin`.

## Overview

**maven-jinja-filter** integrates the [Jinja](https://jinja.palletsprojects.com/) templating engine into Maven's resource filtering process. 
It allows you to use Jinja syntax (`{{ ... }}`) in your resource files and have them processed with Maven properties, filter files, and additional context during your build. 
This is especially useful for generating configuration files or code templates with advanced logic not supported by standard Maven resource filtering.

- **Seamless integration:** Add-on module for standard `maven-resources-plugin`.
- **Jinja syntax support:** Use the full Jinja syntax for sophisticated templating.
- **Extension-based filtering:** Only files with `.j2` extension are automatically processed by the Jinja filter.
- **Full Maven property support:** Properties from Maven project, session, and filter files are available to your templates.
- **Drop-in replacement:** Can be used alongside or as a replacement for standard Maven filtering.

## Getting Started

### Usage Example

Suppose you have a template file: `src/main/resources/config.yaml.j2`

```jinja
server:
  host: {{ host_name }}
  port: {{ server_port }}
  tls:  {{ server_port == "443" }}

{% if server_port == 443 %}
pki:
  key:  {{ key_file }}
  cert: {{ cert_file }}
{% endif %} 
```

If your Maven properties contain `server_port` and `project_name`, the output will be a fully rendered `config.yaml` without the `.j2` extension.

NOTE: Resources which are not enabled for filtering, or having files without `.j2` extension are not processed by the jinja-filter.  

### Integration

Just declare this filter module as a dependency to the maven-resources-plugin:

```xml
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <version>3.3.1</version>
    <dependencies>
        <dependency>
            <groupId>de.delker.maven</groupId>
            <artifactId>maven-jinja-filter</artifactId>
        </dependency>
    </dependencies>
</plugin>
```

All config options of the `maven-resources-plugin` apply.
The `maven-jinja-filter` behaves completely neutral for any files without `.j2` extension.
For files with that extension, the Jinja filtering is applied in **addition** to any default filtering, and the final file has the `.j2` extension removed.

IMPORTANT:
The semantics of variables in Jinja differs somewhat from the Maven world.
A common Maven variable like `project.build.directory` is not resolvable in Jinja, because the dot (`.`) is interpreted as a selector for sub-attributes (like `project.get(build).get(directory)`).
Therefore, the `maven-jinja-filter` transposes all properties to a safe notation, where `.` is replaced with `_`.
So use `{{ project_build_directory }}` for resolving the former Maven property.

## License

This project is licensed under the [Apache License 2.0](LICENSE).

## Contributing & Issues

Feel free to open issues or submit pull requests on [GitHub](https://github.com/jdelker/maven-jinja-filter).

package org.sonatype.maven.polyglot.atom.parsing;

import org.apache.maven.model.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Project extends Element {
  private final Id projectId;
  private final Id parent;
  private String packaging = "jar";
  private List<Property> properties;
  private final Repositories repositories;
  private final String description;
  private final String url;
  private List<Id> deps;
  private List<Id> overrides;
  private List<String> modules;
  private List<Plugin> plugins;
  private Map<String, String> dirs;
  private static final String MAVEN_CENTRAL_URL = "http://repo1.maven.org/maven2";
  private final ScmElement scm;

  public Project(Id projectId, Id parent, String packaging, List<Property> properties, Repositories repositories, String description, String url,
                 List<Id> overrides, List<Id> deps, List<String> modules, List<Plugin> plugins, Map<String, String> dirs, ScmElement scm) {
    this.projectId = projectId;
    this.parent = parent;
    this.packaging = packaging;
    this.properties = properties;
    this.repositories = repositories;
    this.description = description;
    this.url = url;
    this.overrides = overrides;
    this.deps = deps;
    this.modules = modules;
    this.plugins = plugins;
    this.dirs = dirs;
    this.scm = scm;
  }

  public Id getProjectId() {
    return projectId;
  }

  public Id getParent() {
    return parent;
  }
  
  public String getPackaging() {
    return packaging;
  }
  
  public Repositories getRepositories() {
    return repositories;
  }

  public String getDescription() {
    return description;
  }

  public String getUrl() {
    return url;
  }

  public List<Id> getDeps() {
    return deps;
  }

  public Map<String, String> getDirs() {
    return dirs;
  }

  public Model toMavenModel() {
    Model model = new Model();
    model.setBuild(new Build());
    model.setDescription(description);
    model.setUrl(url);
    model.setName(projectId.getArtifact());
    model.setGroupId(projectId.getGroup());
    model.setVersion(projectId.getVersion());
    model.setArtifactId(projectId.getArtifact());
    model.setModelVersion("4.0.0");
    
    // parent
    if (parent != null) {
      Parent p = new Parent();
      p.setGroupId(parent.getGroup());
      p.setArtifactId(parent.getArtifact());
      p.setVersion(parent.getVersion());
      model.setParent(p);
    }
        
    model.setPackaging(packaging);
    
    if (properties != null) {
      Properties modelProperties = new Properties();
      for (Property p : properties) {
        modelProperties.setProperty(p.getKey(), p.getValue());
      }
      model.setProperties(modelProperties);
    }
    
    // Add jar repository urls.
    if (null == repositories) {
      // Add maven central if no repos exist.
      Repository repository = new Repository();
      repository.setId("Maven Central");
      repository.setUrl(MAVEN_CENTRAL_URL);
      model.addRepository(repository);

    } else {
      for (String repoUrl : repositories.getRepositories()) {
        Repository repository = new Repository();
        repository.setId(Integer.toString(repoUrl.hashCode()));
        repository.setUrl(repoUrl);
        model.addRepository(repository);
      }
    }

    // Add dependency management
    if (overrides != null) {
      DependencyManagement depMan = new DependencyManagement();
      for (Id dep : overrides) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(dep.getGroup());
        dependency.setArtifactId(dep.getArtifact());
        dependency.setVersion(dep.getVersion());

        if (null != dep.getClassifier()) {
          dependency.setClassifier(dep.getClassifier());
        }
        depMan.addDependency(dependency);
      }
      model.setDependencyManagement(depMan);
    }
    
    
    // Add project dependencies.
    if (deps != null) {
      for (Id dep : deps) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(dep.getGroup());
        dependency.setArtifactId(dep.getArtifact());
        dependency.setVersion(dep.getVersion());

        if (null != dep.getClassifier())
          dependency.setClassifier(dep.getClassifier());
        model.addDependency(dependency);
      }
    }

    if (modules != null) {
      model.setModules(modules);
    }
    
    if (plugins != null) {
      model.getBuild().setPlugins(plugins);
    }
    
    // Optional source dirs customization.
    if (dirs != null) {
      Build build = new Build();
      String srcDir = dirs.get("src");
      String testDir = dirs.get("test");

      if (null != srcDir)
        build.setSourceDirectory(srcDir);

      if (null != testDir)
        build.setTestSourceDirectory(testDir);

      model.setBuild(build);
    }

    if (null != scm) {
      Scm scm = new Scm();
      scm.setConnection(this.scm.getConnection());
      scm.setDeveloperConnection(this.scm.getDeveloperConnection());
      scm.setUrl(this.scm.getUrl());

      model.setScm(scm);
    }
    return model;
  }

  public ScmElement getScm() {
    return scm;
  }
}

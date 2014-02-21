package org.drools.example.osgi;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.util.FileManager;
import org.drools.example.model.Cheese;
import org.drools.example.rule.EntityHelper;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.builder.IncrementalResults;
import org.kie.internal.builder.InternalKieBuilder;
import org.kie.scanner.MavenRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.kie.scanner.MavenRepository.getMavenRepository;

public class FetchExternalAndScanResourceOsgiActivator implements BundleActivator {

    static final Logger LOG = LoggerFactory.getLogger(FetchExternalAndScanResourceOsgiActivator.class);

    static final String EXTERNAL_XLS_FILE = "/Users/chmoulli/MyProjects/droolsjbpm-osgi-examples/documentation/decision-table/cheeseDecisionTable.xls";
    //static final String DIR_TO_WATCH = "/Users/chmoulli/Temp/test";
    static final String DIR_TO_WATCH = "/Users/chmoulli/MyProjects/droolsjbpm-osgi-examples/documentation/decision-table";
    static final String PACKAGE_NAME = "org.drools.example.cheese";
    static final String RELEASE_MAJOR_NUM = "1.0";
    static final String GROUP_ID = "org.drools.example";
    static final String ARTIFACT_ID = "watch-resource";

    static Integer count = 0;

    ExecutorService service;
    KieServices ks;
    KieFileSystem kfs;
    KieBuilder kbuilder;
    KieContainer kc;
    KieSession ksession;
    MavenRepository repository;
    FileManager fileManager;
    File kPom;

    public FetchExternalAndScanResourceOsgiActivator() throws Exception {
        watchFileModified();
    }

    @Override
    public void start(BundleContext context) throws Exception {

        ks = KieServices.Factory.get();
        kc = ks.newKieClasspathContainer(getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        repository = getMavenRepository();

        ReleaseId releaseId = populateReleaseId(GROUP_ID, ARTIFACT_ID, RELEASE_MAJOR_NUM);
        fileManager = new FileManager();
        kPom = createKPom(releaseId);

        InternalKieModule kJar = createKieJarWithClass(ks, releaseId);
        repository.deployArtifact(releaseId, kJar, kPom);

        ksession = kc.newKieSession("KSession");
        System.out.println("KieSession created.");

        for (int i = 0; i < 10; i++) {
            // Create a Cheese
            Cheese aCheese = EntityHelper.createCheese();
            ksession.insert(aCheese);

            // Fire the rules
            ksession.fireAllRules();

            // Check Cheese Price
            EntityHelper.cheesePrice(aCheese);
        }

        System.out.println("Cheese added and rules fired.");

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (ksession != null) {
            ksession.dispose();
            System.out.println("KieSession disposed.");
        }
        // We have to stop also the Executor Service watching for file(s) modified
        service.shutdown();
    }

    private KieBase createKieBase(String file) throws Exception {

        Resource rs = ks.getResources().newUrlResource(filePath(file));
        ReleaseId releaseId = populateReleaseId(GROUP_ID,ARTIFACT_ID,RELEASE_MAJOR_NUM);
        System.out.println("Release ID created : " + releaseId.toString());

        kfs = ks.newKieFileSystem()
                .generateAndWritePomXML(releaseId)
                .write(rs)
                .writeKModuleXML(createKieProjectWithPackages(ks, PACKAGE_NAME).toXML());

        kbuilder = ks.newKieBuilder(kfs);
        kbuilder.buildAll();

        return ks.newKieContainer(releaseId).getKieBase();
    }

    private InternalKieModule createKieJarWithClass(KieServices ks, ReleaseId releaseId) throws IOException {
        kfs = createKieFileSystemWithKProject(ks, PACKAGE_NAME);
        kfs.writePomXML(getPom(releaseId));

        Resource rs = ks.getResources().newUrlResource(filePath(EXTERNAL_XLS_FILE));
        kfs.write(rs);

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        return (InternalKieModule) kieBuilder.getKieModule();
    }

    protected String getPom(ReleaseId releaseId, ReleaseId... dependencies) {
        String pom =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "  <groupId>" + releaseId.getGroupId() + "</groupId>\n" +
                        "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" +
                        "  <version>" + releaseId.getVersion() + "</version>\n" +
                        "\n";
        if (dependencies != null && dependencies.length > 0) {
            pom += "<dependencies>\n";
            for (ReleaseId dep : dependencies) {
                pom += "<dependency>\n";
                pom += "  <groupId>" + dep.getGroupId() + "</groupId>\n";
                pom += "  <artifactId>" + dep.getArtifactId() + "</artifactId>\n";
                pom += "  <version>" + dep.getVersion() + "</version>\n";
                pom += "</dependency>\n";
            }
            pom += "</dependencies>\n";
        }
        pom += "</project>";
        return pom;
    }

    protected KieFileSystem createKieFileSystemWithKProject(KieServices ks, String pkg) {
        KieModuleModel kmodule = ks.newKieModuleModel();
        kmodule.newKieBaseModel("KBase")
                .addPackage(pkg)
                .setDefault(true)
                .newKieSessionModel("KSession")
                .setDefault(true);
        kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kmodule.toXML());
        return kfs;
    }

    private KieModuleModel createKieProjectWithPackages(KieServices ks, String pkg) {
        KieModuleModel kmodule = ks.newKieModuleModel();
        kmodule.newKieBaseModel("KBase")
                .addPackage(pkg)
                .setDefault(true)
                .newKieSessionModel("defaultSession")
                .setDefault(true);

        return kmodule;
    }

    void watchFileModified() throws Exception {

        service = Executors.newCachedThreadPool();

        final WatchService ws = FileSystems.getDefault().newWatchService();

        final Path dir = Paths.get(DIR_TO_WATCH);
        dir.register(ws, ENTRY_MODIFY);

        service.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("START :: Wait for a file modification");
                while (Thread.interrupted() == false) {

                    // wait for key to be signaled
                    WatchKey key;
                    try {
                        key = ws.take();
                    } catch (InterruptedException x) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind kind = event.kind();

                        if (kind == OVERFLOW) {
                            continue;
                        }

                        // The filename is the context of the event.
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        // File Modified
                        Path child = dir.resolve(filename);
                        BasicFileAttributes attrs = null;
                        try {
                            attrs = Files.readAttributes(child, BasicFileAttributes.class);

                            // File Modified
                            System.out.println("Modified attr " + attrs.lastModifiedTime());

                            // Get a New Release id & Resource
                            ReleaseId newReleaseId = populateReleaseId(GROUP_ID,ARTIFACT_ID,RELEASE_MAJOR_NUM);
                            Resource rs = ks.getResources().newUrlResource(filePath(EXTERNAL_XLS_FILE));

                            System.out.println("Release ID updated : " + newReleaseId.toString());

                            System.out.println(";-) Fired rules");

/*                            // Update Kfs
                            kfs = ks.newKieFileSystem()
                                    .generateAndWritePomXML(newReleaseId)
                                    .write(rs)
                                    .writeKModuleXML(createKieProjectWithPackages(ks, PACKAGE_NAME).toXML());

                            InternalKieBuilder internalKieBuilder = (InternalKieBuilder) kbuilder;
                            IncrementalResults results = internalKieBuilder.incrementalBuild();
                            Results updateResults = kc.updateToVersion( newReleaseId );

                            System.out.println("Add a new Cheese to the session");
                            Cheese cheese = new Cheese();
                            cheese.setType("Gouda");
                            cheese.setPrice(100);
                            ksession.insert( cheese );

                            System.out.println("Fired rules");
                            ksession.fireAllRules();*/

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    //Reset the key -- this step is critical if you want to receive
                    //further watch events. If the key is no longer valid, the directory
                    //is inaccessible so exit the loop.
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                } // end while Thread
            } // run method
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                service.shutdown();
            }
        });
    }

    private File createKPom(ReleaseId releaseId) throws IOException {
        File pomFile = fileManager.newFile("pom.xml");
        fileManager.write(pomFile, getPom(releaseId));
        return pomFile;
    }

    private ReleaseId populateReleaseId(String groupId, String artifactId, String version) {
        Integer minor_number = ++count;
        String release_number = version + "." + minor_number.toString();
        ReleaseId releaseID = ks.newReleaseId(groupId, artifactId, release_number);
        return releaseID;
    }

    private URL filePath(String location) throws MalformedURLException {
        URL url = (new File(location)).toURI().toURL();
        LOG.info("External Resource Location : " + url.toString());
        return url;
    }


}

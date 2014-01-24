package org.drools.example.osgi;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import org.drools.example.model.Cheese;
import org.drools.example.rule.EntityHelper;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.IncrementalResults;
import org.kie.internal.builder.InternalKieBuilder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncrementAndFetchExternalResourceOsgiActivator implements BundleActivator {

    static final Logger LOG = LoggerFactory.getLogger(IncrementAndFetchExternalResourceOsgiActivator.class);

    static final String EXTERNAL_XLS_FILE = "/Users/chmoulli/MyProjects/droolsjbpm-osgi-examples/documentation/decision-table/cheeseDecisionTable.xls";
    //static final String DIR_TO_WATCH = "/Users/chmoulli/Temp/test";
    static final String DIR_TO_WATCH = "/Users/chmoulli/MyProjects/droolsjbpm-osgi-examples/documentation/decision-table";
    static final String PACKAGE_NAME = "org.drools.example.cheese";
    static final String RELEASE_MAJOR_NUM = "1.0";

    static Integer count = 0;

    ExecutorService service;
    KieServices ks;
    KieFileSystem kfs;
    KieBuilder kbuilder;
    KieContainer kc;

    private KieSession ksession;

    public IncrementAndFetchExternalResourceOsgiActivator() throws Exception {
        watchFileModified();
    }

    @Override
    public void start(BundleContext context) throws Exception {

        ks = KieServices.Factory.get();
        kc = ks.newKieClasspathContainer(getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        KieBase kbase = createKieBase(EXTERNAL_XLS_FILE);

        ksession = kbase.newKieSession();
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

    private KieBase createKieBase(String file) throws MalformedURLException {

        Resource rs = ks.getResources().newUrlResource(filePath(file));
        ReleaseId releaseId = populateReleaseId();
        System.out.println("Release ID created : " + releaseId.toString());

        kfs = ks.newKieFileSystem()
                .generateAndWritePomXML(releaseId)
                .write(rs)
                .writeKModuleXML(createKieProjectWithPackages(ks, PACKAGE_NAME).toXML());

        kbuilder = ks.newKieBuilder(kfs);
        kbuilder.buildAll();

        return ks.newKieContainer(releaseId).getKieBase();
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
                            ReleaseId newReleaseId = populateReleaseId();
                            Resource rs = ks.getResources().newUrlResource(filePath(EXTERNAL_XLS_FILE));

                            System.out.println("Release ID updated : " + newReleaseId.toString());

                            // Update Kfs
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
                            ksession.fireAllRules();

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

    private ReleaseId populateReleaseId() {
        Integer minor_number = ++count;
        String release_number = RELEASE_MAJOR_NUM + "." + minor_number.toString();
        ReleaseId releaseID = ks.newReleaseId("org.drools.example", "fetch-external-resource", release_number);
        return releaseID;
    }

    private URL filePath(String location) throws MalformedURLException {
        URL url = (new File(location)).toURI().toURL();
        LOG.info("External Resource Location : " + url.toString());
        return url;
    }


}

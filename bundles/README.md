Bundles to be deployed on karaf
===============================

    addurl mvn:org.drools.example/features/1.0.0-SNAPSHOT/xml/features
    features:install drools-module

    install -s mvn:org.apache.xbean/xbean-reflect/3.9
    install -s mvn:org.apache.xbean/xbean-asm-shaded/3.9
    install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.ant/1.9.2_1
    install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.aopalliance-1.0/
    
    install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.maven-3.0.5/1.0
    install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.codehaus/1.0
    install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.aether-1.13/1.0

    install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.guava/11_1
    install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.asm/3.1_3

    install -s wrap:mvn:org.apache.maven.wagon/wagon-http/2.0
    install -s wrap:mvn:org.apache.maven.wagon/wagon-provider-api/1.0
    install -s wrap:mvn:org.sonatype.plexus/plexus-sec-dispatcher/1.4

    install -s mvn:org.sonatype.sisu/sisu-guice/3.1.1
    install -s mvn:org.sonatype.sisu/sisu-guava/0.11.1
    install -s wrap:mvn:org.sonatype.sisu/sisu-inject-bean/2.3.0
    install -s wrap:mvn:org.sonatype.sisu/sisu-inject-plexus/2.3.0

    install -s mvn:org.kie/kie-ci/6.1.0-SNAPSHOT
    install -s mvn:org.drools.example/simple-kie-ci/1.0.0-SNAPSHOT
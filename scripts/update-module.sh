#/bin/sh
export JBOSS_HOME=/home/mlinhard/dev/projects/jbossas7_demo/jboss-7.0.0.Beta3-dictionary-demo
cp -f ../target/dictionary-demo-1.0.0-SNAPSHOT.jar $JBOSS_HOME/modules/org/jboss/as7demo/dict/main/
cp -f ../module/module.xml $JBOSS_HOME/modules/org/jboss/as7demo/dict/main/
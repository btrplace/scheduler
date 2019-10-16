module org.btrplace.safeplace {
  requires transitive org.btrplace.scheduler.json;
  requires transitive org.btrplace.scheduler.api;
  requires transitive org.btrplace.scheduler.choco;
  requires org.antlr.antlr4.runtime;
  requires org.testng;
  requires fast.classpath.scanner;
  requires sisu.guava;
  exports org.btrplace.safeplace.spec;
  exports org.btrplace.safeplace.testing;
  exports org.btrplace.safeplace.util;
}

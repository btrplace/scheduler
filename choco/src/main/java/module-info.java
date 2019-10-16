module org.btrplace.scheduler.choco {
  requires transitive org.btrplace.scheduler.api;
  requires choco.solver;
  requires org.slf4j;
  exports org.btrplace.scheduler.choco;
  exports org.btrplace.scheduler.choco.constraint;
  exports org.btrplace.scheduler.choco.constraint.migration;
  exports org.btrplace.scheduler.choco.constraint.mttr;
  exports org.btrplace.scheduler.choco.duration;
  exports org.btrplace.scheduler.choco.extensions;
  exports org.btrplace.scheduler.choco.extensions.pack;
  exports org.btrplace.scheduler.choco.runner;
  exports org.btrplace.scheduler.choco.runner.single;
  exports org.btrplace.scheduler.choco.transition;
  exports org.btrplace.scheduler.choco.view;
}

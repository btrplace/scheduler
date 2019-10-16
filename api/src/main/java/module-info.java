module org.btrplace.scheduler.api {
  requires trove4j;
  exports org.btrplace;
  exports org.btrplace.scheduler;
  exports org.btrplace.model;
  exports org.btrplace.model.constraint;
  exports org.btrplace.model.constraint.migration;
  exports org.btrplace.model.view;
  exports org.btrplace.model.view.network;
  exports org.btrplace.plan;
  exports org.btrplace.plan.event;
}

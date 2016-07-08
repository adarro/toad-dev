package io.truthencode.toad.cluster;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ClusterMembershipListener
  implements MembershipListener {
  private final Logger log = LoggerFactory.getLogger(ClusterMembershipListener.class  );

  public void memberAdded(MembershipEvent membershipEvent) {
    log.info("Added: " + membershipEvent);
  }

  public void memberRemoved(MembershipEvent membershipEvent) {
    log.info("Removed: " + membershipEvent);
  }

  public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
    log.info("Member attribute changed: " + memberAttributeEvent);
  }

}

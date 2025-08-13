package ${package};

import org.springframework.boot.SpringApplication;
import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;

@SIPIntegrationAdapter
public class SIPApplication {
    public static void main(String[] args) {
        SpringApplication.run(SIPApplication.class, args);
    }

}

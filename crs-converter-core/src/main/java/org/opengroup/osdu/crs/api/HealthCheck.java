package org.opengroup.osdu.crs.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_ah")
public class HealthCheck {

	@GetMapping("/liveness_check")
	public ResponseEntity<?> livenessCheck() {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/readiness_check")
	public ResponseEntity<?> readinessCheck() {
		// Commented logging - lets us monitor VM state while running
		// Logger log = Logger.getLogger("Readiness");
		// String message = String.format("Ready|%d|%d" , System.nanoTime(), Runtime.getRuntime().freeMemory());
		// log.info(message);
		return ResponseEntity.ok().build();
	}
}

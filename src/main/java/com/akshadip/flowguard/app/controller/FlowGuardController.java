package com.akshadip.flowguard.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * FlowGuard Controller - Endpoints for rate-limited requests
 * Author: Akshadip
 */
@RestController
@RequestMapping("/v1/flowguard")
public class FlowGuardController {

  // a rate limiter endpoint
  @RequestMapping(method = RequestMethod.GET)
  public String getFlowGuardStatus() {

    return "FlowGuard is active! Request rate limiting enabled.";
  }
}

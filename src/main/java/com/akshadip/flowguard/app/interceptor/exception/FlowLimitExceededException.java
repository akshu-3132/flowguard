package com.akshadip.flowguard.app.interceptor.exception;

/**
 * Exception thrown when a request exceeds the flow limit
 * Author: Akshadip
 */
public class FlowLimitExceededException extends Exception {

  public FlowLimitExceededException() {
    super();
  }
}

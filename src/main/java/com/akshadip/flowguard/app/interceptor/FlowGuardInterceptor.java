package com.akshadip.flowguard.app.interceptor;

import com.akshadip.flowguard.app.interceptor.exception.FlowLimitExceededException;
import com.akshadip.flowguard.app.service.RateLimiterService;
import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * FlowGuard Interceptor - Handles request rate limiting
 * Author: Akshadip
 * 
 * This interceptor now uses constructor injection and depends only on the
 * RateLimiterService, eliminating circular dependencies with configuration classes.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class FlowGuardInterceptor extends OncePerRequestFilter {

  private final RateLimiterService rateLimiterService;

  /**
   * Constructor injection ensures all dependencies are available before this component is used
   * 
   * @param rateLimiterService the rate limiter service for validating requests
   */
  public FlowGuardInterceptor(RateLimiterService rateLimiterService) {
    this.rateLimiterService = rateLimiterService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return false; //filter everything
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // get IP from request
    String ip = request.getRemoteAddr();

    log.debug(String.format("Getting request from ip: %s", ip));

    try {
      rateLimiterService.validateFlowLimitForIp(ip);
    } catch (FlowLimitExceededException e) {
      // return flow limit exceeded response
      response.sendError(429, "Request rate limit exceeded. Please try again later.");
      return;
    }

    filterChain.doFilter(request, response);
  }

}

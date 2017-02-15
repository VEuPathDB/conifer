package org.gusdb.fgputil.web;

import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

/**
 * Facade over HttpServletRequest that simplifies some common calls to and manipulations of HttpServletRequest.
 * 
 * @author rdoherty
 */
public class HttpRequestData implements RequestData {

  private HttpServletRequest _request;

  public HttpRequestData(HttpServletRequest request) {
    _request = request;
  }

  public HttpServletRequest getUnderlyingRequest() {
    return _request;
  }

  @Override
  public String getWebAppBaseUrl() {
    return new StringBuilder()
      .append(getNoContextUrl())
      .append(_request.getContextPath())
      .toString();
  }

  @Override
  public String getNoContextUrl() {
    return new StringBuilder()
      .append(_request.getScheme())
      .append("://")
      .append(_request.getServerName())
      .append(_request.getServerPort() == 80 ||
              _request.getServerPort() == 443 ?
              "" : ":" + _request.getServerPort())
      .toString();
  }

  @Override
  public String getRequestUri() {
    return _request.getRequestURI();
  }

  @Override
  public String getRequestUrl() {
    return _request.getRequestURL().toString();
  }

  @Override
  public String getQueryString() {
    return _request.getQueryString();
  }

  @Override
  public String getFullRequestUrl() {
    StringBuffer buf = _request.getRequestURL();
    String qString = _request.getQueryString();
    return (buf == null ? new StringBuffer() : buf)
      .append(qString == null ? "" : "?" + qString)
      .toString();
  }

  @Override
  public String getBrowser() {
    return _request.getHeader("User-Agent");
  }

  @Override
  public String getReferrer() {
    return _request.getHeader("Referer");
  }

  @Override
  public String getUserAgent() {
    return _request.getHeader("user-agent");
  }

  @Override
  public String getIpAddress() {
    return _request.getRemoteAddr();
  }

  @Override
  public Object getRequestAttribute(String key) {
    return _request.getAttribute(key);
  }

  @Override
  public String getRequestHeader(String key) {
    return _request.getHeader(key);
  }

  @Override
  public String getRemoteHost() {
    return _request.getRemoteHost();
  }

  @Override
  public String getServerName() {
    return _request.getServerName();
  }

  /**
    * name of host running application (this might be different from
    * any proxy webserver the terminal client is talking to)
  **/
  @Override
  public String getAppHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
     } catch (UnknownHostException e) {
      return "N/A";
     }
  }

  /**
    * IP address of host running application (this might be different from
    * any proxy webserver the terminal client is talking to)
  **/
  @Override
  public String getAppHostAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
     } catch (UnknownHostException e) {
      return "N/A";
     }
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Map<String, String[]> getTypedParamMap() {
    Map parameterMap = _request.getParameterMap();
    @SuppressWarnings({ "unchecked", "cast" })
    Map<String, String[]> parameters = (Map<String, String[]>) (parameterMap == null ?
        new HashMap<>() : new HashMap<>((Map<String, String[]>)parameterMap));
    return parameters;
  }

  @Override
  public HttpMethod getMethod() {
    return HttpMethod.getValueOf(_request.getMethod());
  }
}

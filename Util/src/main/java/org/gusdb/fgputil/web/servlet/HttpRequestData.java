package org.gusdb.fgputil.web.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.gusdb.fgputil.web.HttpMethod;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.fgputil.web.SessionProxy;

/**
 * Facade over HttpServletRequest that simplifies some common calls to and manipulations of
 * HttpServletRequest.
 * 
 * @author rdoherty
 */
public class HttpRequestData implements RequestData {

  private final HttpServletRequest _request;

  public HttpRequestData(HttpServletRequest request) {
    _request = request;
  }

  @Override
  public String getNoContextUri() {
    int port = _request.getServerPort();
    String portPart = port == 80 || port == 443 ? "" : ":" + port;
    return (
      _request.getScheme() + "://" +
      getServerName() + portPart
    );
  }

  @Override
  public String getContextUri() {
    return getNoContextUri() + _request.getContextPath();
  }

  @Override
  public String getQueryString() {
    return _request.getQueryString();
  }

  @Override
  public HttpMethod getMethod() {
    return HttpMethod.getValueOf(_request.getMethod());
  }

  @Override
  public Map<String, List<String>> getRequestParamMap() {
    return _request.getParameterMap();
  }

  @Override
  public SessionProxy getSession() {
    return new HttpSessionProxy(_request.getSession());
  }

  @Override
  public Map<String, Object> getAttributeMap() {
    Map<String,Object> attributes = new HashMap<>();
    Enumeration<String> attributeNames = _request.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
      String key = attributeNames.nextElement();
      attributes.put(key, _request.getAttribute(key));
    }
    return attributes;
  }

  @Override
  public void setAttribute(String name, Object value) {
    _request.setAttribute(name, value);
  }

  @Override
  public String getUserAgent() {
    return _request.getHeader("User-Agent");
  }

  @Override
  public String getReferrer() {
    return _request.getHeader("Referer");
  }

  @Override
  public String getHeader(String name) {
    return _request.getHeader(name);
  }

  @Override
  public String getServerName() {
    return _request.getServerName();
  }

  @Override
  public String getRemoteHost() {
    return _request.getRemoteHost();
  }

  @Override
  public String getRemoteIpAddress() {
    return _request.getRemoteAddr();
  }

}

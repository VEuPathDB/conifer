package org.gusdb.fgputil.solr;

import static org.gusdb.fgputil.json.JsonIterators.arrayStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.json.JsonUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Solr {

  private static final Logger LOG = Logger.getLogger(Solr.class);

  public static final class FacetCounts extends HashMap<String,Map<String,Integer>> { }
  public static final class Highlighting extends HashMap<String,Map<String,List<String>>> { }

  private final String _solrUrl;

  public Solr(String solrUrl) {
    _solrUrl = solrUrl;
  }

  public <T> T executeQuery(String urlSubpath, boolean closeResponseOnExit, FunctionWithException<Response, T> handler) {
    Response response = null;
    try {
      Client client = ClientBuilder.newClient();
      String finalUrl = _solrUrl + urlSubpath;
      LOG.info("Querying SOLR with: " + finalUrl);
      WebTarget webTarget = client.target(finalUrl);
      Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
      response = invocationBuilder.get();
      if (response.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
        try {
          return handler.apply(response);
        }
        catch(Exception e) {
          throw handleError("Unable to process SOLR response", urlSubpath, e);
        }
      }
      else {
        String responseBody = null;
        try {
          responseBody = IoUtil.readAllChars(new InputStreamReader((InputStream)response.getEntity()));
          if (closeResponseOnExit) {
            // only log if we think response will fit nicely into memory; don't want to blow out logs
            LOG.info("Received response from SOLR: " + responseBody);
          }
        }
        catch (IOException e) {
          LOG.error("Unable to read response body in SOLR response with error code " + response.getStatus());
        }
        String bodyContent = responseBody == null ? "" : "; response body:\n" + responseBody;
        throw handleError("SOLR request failed with code " + response.getStatus() + bodyContent, urlSubpath, null);
      }
    }
    catch (JSONException e) {
      throw handleError("SOLR response not valid JSON", urlSubpath, e);
    }
    finally {
      if (response != null && closeResponseOnExit) response.close();
    }
  }

  private static SolrRuntimeException handleError(String message, String urlSubpath, Exception e) {
    String runtimeMsg = "Error: " + message + FormatUtil.NL + "SOLR_REQUEST_URL: " + urlSubpath;
    return e == null ? new SolrRuntimeException(runtimeMsg) : new SolrRuntimeException(runtimeMsg, e);
  }

  public static SolrResponse parseResponse(String requestSubpath, Response response) throws IOException {
    String data = IoUtil.readAllChars(new InputStreamReader((InputStream)response.getEntity()));
    JSONObject responseBody = new JSONObject(data);
    LOG.info("Received response from SOLR: " + responseBody.toString(2));
    int responseStatus = responseBody.getJSONObject("responseHeader").getInt("status");
    if (responseStatus != 0) {
      throw handleError("SOLR response had non-zero embedded status (" + responseStatus + ")", requestSubpath, null);
    }
    String nextCursorMark = responseBody.optString("nextCursorMark", null);
    JSONObject responseJson = responseBody.getJSONObject("response");
    int totalCount = responseJson.getInt("numFound");
    List<JSONObject> documents = arrayStream(responseJson.getJSONArray("docs"))
        .map(val -> val.getJSONObject())
        .collect(Collectors.toList());
    FacetCounts facetCounts = parseFacetCounts(responseBody);
    Highlighting highlighting = parseHighlighting(responseBody);
    SolrResponse respObj = new SolrResponse(
        totalCount,
        documents,
        facetCounts,
        highlighting,
        Optional.ofNullable(nextCursorMark)
    );
    return respObj;
  }

  private static Highlighting parseHighlighting(JSONObject responseBody) {
    Highlighting highlighting = new Highlighting();
    if (responseBody.has("highlighting")) {
      JSONObject highlights = responseBody.getJSONObject("highlighting");
      for (String documentId : highlights.keySet()) {
        Map<String,List<String>> highlightedFields = new LinkedHashMap<>();
        JSONObject fieldMap = highlights.getJSONObject(documentId);
        for (String fieldName : fieldMap.keySet()) {
          JSONArray matches = fieldMap.getJSONArray(fieldName);
          if (matches.length() > 0) {
            highlightedFields.put(fieldName, Arrays.asList(JsonUtil.toStringArray(matches)));
          }
        }
        highlighting.put(documentId, highlightedFields);
      }
    }
    return highlighting;
  }

  private static FacetCounts parseFacetCounts(JSONObject responseBody) {
    FacetCounts facetCountMap = new FacetCounts();
    if (!responseBody.has("facet_counts")) return facetCountMap;
    if (!responseBody.getJSONObject("facet_counts").has("facet_fields")) return facetCountMap;
    JSONObject facets = responseBody.getJSONObject("facet_counts").getJSONObject("facet_fields");
    for (String facetField : facets.keySet()) {
      JSONArray rawFacets = facets.getJSONArray(facetField);
      Map<String,Integer> facetCounts = new HashMap<>();
      for (int i = 0; i < rawFacets.length(); i+=2) {
        facetCounts.put(rawFacets.getString(i), rawFacets.getInt(i+1));
      }
      facetCountMap.put(facetField, facetCounts);
    }
    return facetCountMap;
  }

}

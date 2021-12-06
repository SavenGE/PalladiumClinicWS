/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APalladiumclinicws;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author neptune
 */
/*Handler Class to handle incoming requests*/
public class RequestsHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response) throws
            IOException, ServletException {

        Logger.doPrint("target", target);
        Logger.doPrint("baseRequest.toString()", baseRequest.toString());

        try (InputStream incoming = request.getInputStream()) {

            /*convert request input stream to string using apache commons library*/
            String jsonReq = org.apache.commons.io.IOUtils.toString(incoming, "UTF-8");

            Logger.doPrint("JSON DATA RECEIVED", jsonReq);
            /*Convert the string to json objects*/
            JSONObject jsonRequest = new JSONObject(jsonReq),
                    jsonResponse = jsonRequest;
            /*Fetch Opeartion being performed*/
            String operationName = jsonRequest.getString(Constants.OPERATION);
            
            /*Initialise a transaction processor; 
            idea here is to have several existing in a pool;
            when a request comes in, pick one, process, return to pool*/
            TXNProcessor txnprocessor = new TXNProcessor();

            /*Switch over opearation names, and process requests*/
            switch (operationName) {
                case Constants.AUTHENTICATE_USER:
                    jsonResponse = txnprocessor.authenticateUser(jsonRequest, jsonResponse);
                    break;
                case Constants.SAVE_PATIENT:
                    jsonResponse = txnprocessor.savePatient(jsonRequest, jsonResponse);
                    break;
                case Constants.SEARCH_PATIENT:
                    jsonResponse = txnprocessor.searchPatient(jsonRequest, jsonResponse);
                    break;
                case Constants.SEARCH_TRIAGE_PATIENT:
                    jsonResponse = txnprocessor.searchTriagePatient(jsonRequest, jsonResponse);
                    break;
                case Constants.SAVE_TRIAGE_DATA:
                    jsonResponse = txnprocessor.saveTriageData(jsonRequest, jsonResponse);
                    break;
                case Constants.SUSPEND_TRIAGE_DATA:
                    jsonResponse = txnprocessor.saveTriageData(jsonRequest, jsonResponse);
                    break;
                case Constants.SEARCH_SCREENED_PATIENT:
                    jsonResponse = txnprocessor.searchScreenedPatient(jsonRequest, jsonResponse);
                    break;

            }
            /*send back response*/
            sendResponse(baseRequest, response, jsonRequest, jsonResponse, operationName);
            /*release/dispose transaction processor*/
            txnprocessor.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendResponse(Request baseRequest, HttpServletResponse response,
            JSONObject jsonRequest, JSONObject jsonResponse,
            String operationName) {
        try {
            response.setContentLength(jsonResponse.toString().length());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(200);
            response.getWriter().println(jsonResponse.toString());
            response.flushBuffer();
            baseRequest.setHandled(true);

            Logger.doPrint("JSON RESPONSE SENT", jsonResponse.toString(Constants.JSON_INDENT_FACTOR));

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }
    }

}

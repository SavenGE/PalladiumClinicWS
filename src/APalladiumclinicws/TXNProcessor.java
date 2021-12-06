/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APalladiumclinicws;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author neptune
 */
/*Class to process Transactions upon receipt by Web Service;
idea here is to have several existing in a pool; 
when a request comes in, pick one, process, return to pool*/
public class TXNProcessor {

    /**
     * @return the dbClient
     */
    public PDBClient getDbClient() {
        return dbClient;
    }

    /**
     * @param dbClient the dbClient to set
     */
    private void setDbClient(PDBClient dbClient) {
        this.dbClient = dbClient;
    }

    /*Has a DB Client; for DB actions*/
    private PDBClient dbClient;

    public TXNProcessor() {
        setDbClient(new PDBClient());
    }

    public void dispose() {
        getDbClient().dispose();
    }

    /*Method to Validate user credentials*/
    public JSONObject authenticateUser(JSONObject jsonRequest, JSONObject jsonResponse) {
        String username = jsonRequest.getString(Constants.USERNAME),
                password = jsonRequest.getString(Constants.PASSWORD);

        if (username.equals("palladium") && password.equals("p@ll@di0m")) {
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.APPROVED).put(Constants.RESPONSE_MSG, "APPROVED");
        } else {
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_INVALID_CREDENTIALS).put(Constants.RESPONSE_MSG, "Invalid Credentials");
        }
        return jsonResponse;
    }

    /*Method to save Patient Data and register for triage in DB*/
    public JSONObject savePatient(JSONObject jsonRequest, JSONObject jsonResponse) {
        /*{
  "PHONENO": "254788558585",
  "OPERATION": "SAP",
  "LASTNAME": "lname",
  "DOB": "8/87/8526",
  "ADDRESS": "jh 87878",
  "FIRSTNAME": "fname",
  "IDNO": "85967485"
}*/
        String firstName = jsonRequest.getString(Constants.FIRSTNAME),
                lastName = jsonRequest.getString(Constants.LASTNAME),
                phoneNo = jsonRequest.getString(Constants.PHONENO),
                idNo = jsonRequest.getString(Constants.IDNO),
                address = jsonRequest.getString(Constants.ADDRESS),
                dob = jsonRequest.getString(Constants.DOB);

        String validatePhone = "SELECT PATIENT_ID FROM PATIENT WHERE PATIENT_PHONE = '" + phoneNo + "'",
                validateId = "SELECT PATIENT_ID FROM PATIENT WHERE PATIENT_ID_NO = '" + idNo + "'",
                patientId = generateUniqueID(),
                savePatient = "INSERT INTO PATIENT "
                + "(PATIENT_ID, PATIENT_FNAME, PATIENT_LNAME, PATIENT_PHONE, PATIENT_ID_NO, "
                + "PATIENT_ADDRESS, PATIENT_DOB) "
                + "VALUES "
                + "('" + patientId + "', '" + firstName + "', '" + lastName + "', '" + phoneNo + "', '" + idNo + "', "
                + "'" + address + "', '" + dob + "')",
                triageId = generateUniqueID(),
                saveTriagePatient = "INSERT INTO TRIAGE "
                + "(TRIAGE_ID, PATIENT_ID) "
                + "VALUES "
                + "('" + triageId + "', '" + patientId + "')";
        /*Validate Phone Number*/
        if (getDbClient().checkIfExists(validatePhone)) {
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_PATIENT_PHONE_EXISTS)
                    .put(Constants.RESPONSE_MSG, "Phone Number Already Saved For Another Patient.");
            /*Validate ID Number*/
        } else if (getDbClient().checkIfExists(validateId)) {
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_PATIENT_ID_EXISTS)
                    .put(Constants.RESPONSE_MSG, "ID No Already Saved For Another Patient.");
        } else {
            /*Save Patient Data*/
            if (getDbClient().executeUpdate(savePatient)) {
                /*Register For Triage*/
                if (getDbClient().executeUpdate(saveTriagePatient)) {
                    jsonResponse.put(Constants.RESPONSE_CODE, Constants.APPROVED)
                            .put(Constants.RESPONSE_MSG, "Patient Data Saved.");
                }
            } else {
                jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_PROCESSING_ERROR)
                        .put(Constants.RESPONSE_MSG, "Request Could Not Be Processed. Contact Admin.");
            }
        }
        return jsonResponse;
    }

    /*Method to Search Patient Data using search data provided from DB*/
    public JSONObject searchPatient(JSONObject jsonRequest, JSONObject jsonResponse) {
        /*{
  "OPERATION": "SEP",
  "SEARCH_TAG": "IDNO",
  "SEARCH_VALUE": "748596"
}*/
        String searchTag = jsonRequest.optString(Constants.SEARCH_TAG),
                searchValue = jsonRequest.optString(Constants.SEARCH_VALUE);

        String query = "SELECT * FROM PATIENT WHERE 1 = 1 ";

        if (searchTag.equals(Constants.IDNO)) {
            query = query + " AND PATIENT_ID_NO = '" + searchValue + "' ";
        } else if (searchTag.equals(Constants.PHONENO)) {
            query = query + " AND PATIENT_PHONE = '" + searchValue + "' ";
        }

        try {
            /*Build a list of fetched patients*/
            JSONArray patientsList = new JSONArray();
            try (ResultSet rs = getDbClient().executeQuery(query)) {
                while (rs.next()) {
                    JSONObject patient = new JSONObject();
                    patient.put(Constants.PATIENTID, rs.getString("PATIENT_ID"))
                            .put(Constants.FIRSTNAME, rs.getString("PATIENT_FNAME"))
                            .put(Constants.LASTNAME, rs.getString("PATIENT_LNAME"))
                            .put(Constants.PHONENO, rs.getString("PATIENT_PHONE"))
                            .put(Constants.IDNO, rs.getString("PATIENT_ID_NO"))
                            .put(Constants.ADDRESS, rs.getString("PATIENT_ADDRESS"))
                            .put(Constants.DOB, rs.getString("PATIENT_DOB"));
                    patientsList.put(patient);
                }
            }
            jsonResponse.put(Constants.PATIENTS, patientsList);
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.APPROVED)
                    .put(Constants.RESPONSE_MSG, "APPROVED");
        } catch (Exception ex) {
            ex.printStackTrace();
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_PROCESSING_ERROR)
                    .put(Constants.RESPONSE_MSG, "Request Could Not Be Processed. Contact Admin.");
        }
        return jsonResponse;
    }

    /*Method to search Triage Patient top of Queue using Triage Date registered Data from DB*/
    public JSONObject searchTriagePatient(JSONObject jsonRequest, JSONObject jsonResponse) {
        /*
	{"OPERATION": "SETP"}
         */

        String query = "SELECT   (SELECT   COUNT ( * ) FROM   PATIENT P, TRIAGE T "
                + "WHERE P.PATIENT_ID = T.PATIENT_ID AND FEVER IS NULL) NUM, "
                + "P.PATIENT_ID, TRIAGE_ID, PATIENT_FNAME, PATIENT_LNAME,"
                + "PATIENT_PHONE,PATIENT_ID_NO,PATIENT_ADDRESS,PATIENT_DOB "
                + "FROM   PATIENT P, TRIAGE T WHERE P.PATIENT_ID = T.PATIENT_ID "
                + "AND FEVER IS NULL ORDER BY   TRIAGE_DT_TIME ASC";

        try {
            /*Retuen top of the queue patient for triage*/
            JSONObject patient = new JSONObject();
            try (ResultSet rs = getDbClient().executeQuery(query)) {
                if (rs.next()) {
                    patient.put(Constants.PATIENTID, rs.getString("PATIENT_ID"))
                            .put(Constants.TRIAGEID, rs.getString("TRIAGE_ID"))
                            .put(Constants.FIRSTNAME, rs.getString("PATIENT_FNAME"))
                            .put(Constants.LASTNAME, rs.getString("PATIENT_LNAME"))
                            .put(Constants.PHONENO, rs.getString("PATIENT_PHONE"))
                            .put(Constants.IDNO, rs.getString("PATIENT_ID_NO"))
                            .put(Constants.ADDRESS, rs.getString("PATIENT_ADDRESS"))
                            .put(Constants.DOB, rs.getString("PATIENT_DOB"))
                            .put(Constants.TRIAGE_QUEUE_NUM, rs.getString("NUM"));
                }
            }
            if (patient.has(Constants.TRIAGEID)) {
                jsonResponse.put(Constants.PATIENT, patient);
                jsonResponse.put(Constants.RESPONSE_CODE, Constants.APPROVED)
                        .put(Constants.RESPONSE_MSG, "APPROVED");
            } else {
                jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_NO_TRIAGE_PATIENT)
                        .put(Constants.RESPONSE_MSG, "No Patients awating Triage. Try again Later.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_PROCESSING_ERROR)
                    .put(Constants.RESPONSE_MSG, "Request Could Not Be Processed. Contact Admin.");
        }
        return jsonResponse;
    }

    /*SAve or Suspend Triage Data for A Patient*/
    public JSONObject saveTriageData(JSONObject jsonRequest, JSONObject jsonResponse) {
        /*	{
  "SORE_THROAT": false,
  "TRIAGEID": "04122117175884",
  "LOSS_OF_SMELL": false,
  "CHEST_PAIN": false,
  "TEMPERATURE": "45",
  "OPERATION": "STD",
  "LOSS_OF_TASTE": false,
  "HEADACHE": false,
  "DIFFICULT_BREATHING": false,
  "TIREDNESS": false,
  "BREATH_SHORTNESS": false,
  "WEIGHT": "85",
  "PATIENTID": "04122117175884",
  "FEVER": false
}*/
        String operationName = jsonRequest.getString(Constants.OPERATION),
                patientId = jsonRequest.getString(Constants.PATIENTID),
                triageId = jsonRequest.getString(Constants.TRIAGEID),
                weight = jsonRequest.getString(Constants.WEIGHT),
                temperature = jsonRequest.getString(Constants.TEMPERATURE),
                fever = jsonRequest.optBoolean(Constants.FEVER) ? "Y" : "N",
                tiredness = jsonRequest.optBoolean(Constants.TIREDNESS) ? "Y" : "N",
                lossOfTaste = jsonRequest.optBoolean(Constants.LOSS_OF_TASTE) ? "Y" : "N",
                lossOfSmell = jsonRequest.optBoolean(Constants.LOSS_OF_SMELL) ? "Y" : "N",
                soreThroat = jsonRequest.optBoolean(Constants.SORE_THROAT) ? "Y" : "N",
                headAche = jsonRequest.optBoolean(Constants.HEADACHE) ? "Y" : "N",
                chestPain = jsonRequest.optBoolean(Constants.CHEST_PAIN) ? "Y" : "N",
                difficultyBreathing = jsonRequest.optBoolean(Constants.DIFFICULT_BREATHING) ? "Y" : "N",
                breathShortness = jsonRequest.optBoolean(Constants.BREATH_SHORTNESS) ? "Y" : "N";

        String updateTriageData = "";
        if (operationName.equals(Constants.SAVE_TRIAGE_DATA)) {
            updateTriageData = "UPDATE TRIAGE SET "
                    + "WEIGHT = '" + weight + "', TEMPERATURE = '" + temperature + "',"
                    + "TRIAGE_DT_TIME = SYSDATE, "
                    + "FEVER = '" + fever + "',TIREDNESS = '" + tiredness + "',"
                    + "LOSS_OF_TASTE = '" + lossOfTaste + "',LOSS_OF_SMELL = '" + lossOfSmell + "',"
                    + "SORE_THROAT = '" + soreThroat + "',HEADACHE = '" + headAche + "',"
                    + "CHEST_PAIN = '" + chestPain + "',DIFFICULT_BREATHING = '" + difficultyBreathing + "',"
                    + "BREATH_SHORTNESS = '" + breathShortness + "' "
                    + "WHERE TRIAGE_ID = '" + triageId + "'";
        } else {
            /*Suspend By 1 minute*/
            updateTriageData = "UPDATE TRIAGE SET "
                    + "TRIAGE_DT_TIME = TRIAGE_DT_TIME + (1/1440) "
                    + "WHERE TRIAGE_ID = '" + triageId + "'";
        }

        if (getDbClient().executeUpdate(updateTriageData)) {
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.APPROVED)
                    .put(Constants.RESPONSE_MSG, "APPROVED");
        } else {
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_PROCESSING_ERROR)
                    .put(Constants.RESPONSE_MSG, "Request Could Not Be Processed. Contact Admin.");
        }

        return jsonResponse;
    }

    /*FEtch all successfully screened patients*/
    public JSONObject searchScreenedPatient(JSONObject jsonRequest, JSONObject jsonResponse) {
        /*{
  "OPERATION": "SECP",
  "SEARCH_TAG": "ALL",
  "SEARCH_VALUE": ""
}*/
        String searchTag = jsonRequest.optString(Constants.SEARCH_TAG),
                searchValue = jsonRequest.optString(Constants.SEARCH_VALUE);

        String query = "SELECT * FROM PATIENT P, TRIAGE T "
                + "WHERE T.PATIENT_ID = P.PATIENT_ID AND FEVER IS NOT NULL ";

        if (searchTag.equals(Constants.IDNO)) {
            query = query + "AND PATIENT_ID_NO = '" + searchValue + "' ";
        } else if (searchTag.equals(Constants.PHONENO)) {
            query = query + "AND PATIENT_PHONE = '" + searchValue + "' ";
        }

        try {
            /*Build screened patients List with all symptoms*/
            JSONArray patientsList = new JSONArray();
            try (ResultSet rs = getDbClient().executeQuery(query)) {
                while (rs.next()) {
                    JSONObject patient = new JSONObject();
                    patient.put(Constants.PATIENTID, rs.getString("PATIENT_ID"))
                            .put(Constants.FIRSTNAME, rs.getString("PATIENT_FNAME"))
                            .put(Constants.LASTNAME, rs.getString("PATIENT_LNAME"))
                            .put(Constants.PHONENO, rs.getString("PATIENT_PHONE"))
                            .put(Constants.IDNO, rs.getString("PATIENT_ID_NO"))
                            .put(Constants.ADDRESS, rs.getString("PATIENT_ADDRESS"))
                            .put(Constants.DOB, rs.getString("PATIENT_DOB"))
                            .put(Constants.WEIGHT, rs.getString("WEIGHT"))
                            .put(Constants.TEMPERATURE, rs.getString("TEMPERATURE"))
                            .put(Constants.FEVER, rs.getString("FEVER"))
                            .put(Constants.TIREDNESS, rs.getString("TIREDNESS"))
                            .put(Constants.LOSS_OF_TASTE, rs.getString("LOSS_OF_TASTE"))
                            .put(Constants.LOSS_OF_SMELL, rs.getString("LOSS_OF_SMELL"))
                            .put(Constants.SORE_THROAT, rs.getString("SORE_THROAT"))
                            .put(Constants.HEADACHE, rs.getString("HEADACHE"))
                            .put(Constants.CHEST_PAIN, rs.getString("CHEST_PAIN"))
                            .put(Constants.DIFFICULT_BREATHING, rs.getString("DIFFICULT_BREATHING"))
                            .put(Constants.BREATH_SHORTNESS, rs.getString("BREATH_SHORTNESS"));

                    patientsList.put(patient);
                }
            }
            jsonResponse.put(Constants.PATIENTS, patientsList);
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.APPROVED)
                    .put(Constants.RESPONSE_MSG, "APPROVED");
        } catch (Exception ex) {
            ex.printStackTrace();
            jsonResponse.put(Constants.RESPONSE_CODE, Constants.ERR_PROCESSING_ERROR)
                    .put(Constants.RESPONSE_MSG, "Request Could Not Be Processed. Contact Admin.");
        }
        return jsonResponse;
    }

    /*get unique sequence to for primary key DB fields*/
    public String generateUniqueID() {
        return new SimpleDateFormat("ddMMyyHHmmssS").format(new Date());
    }

}

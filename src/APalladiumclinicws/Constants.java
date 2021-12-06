/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APalladiumclinicws;

/**
 *
 * @author neptune
 */
public class Constants {

    public static final int JSON_INDENT_FACTOR = 2;
    public static final String OPERATION = "OPERATION";

    /*Operation Names*/
    public static final String AUTHENTICATE_USER = "AU";
    public static final String SAVE_PATIENT = "SAP";
    public static final String SEARCH_PATIENT = "SEP";
    public static final String SEARCH_TRIAGE_PATIENT = "SETP";
    public static final String SUSPEND_TRIAGE_DATA = "SUTD";
    public static final String SAVE_TRIAGE_DATA = "STD";
    public static final String SEARCH_SCREENED_PATIENT = "SECP";

    /*Json Tags*/
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String PATIENTID = "PATIENTID";
    public static final String FIRSTNAME = "FIRSTNAME";
    public static final String LASTNAME = "LASTNAME";
    public static final String PHONENO = "PHONENO";
    public static final String IDNO = "IDNO";
    public static final String ADDRESS = "ADDRESS";
    public static final String DOB = "DOB";
    public static final String SEARCH_TAG = "SEARCH_TAG";
    public static final String SEARCH_VALUE = "SEARCH_VALUE";
    public static final String ALL = "ALL";
    public static final String TRIAGE_QUEUE_NUM = "TRIAGE_QUEUE_NUM";

    public static final String TRIAGEID = "TRIAGEID";
    public static final String WEIGHT = "WEIGHT";
    public static final String TEMPERATURE = "TEMPERATURE";
    public static final String FEVER = "FEVER";
    public static final String TIREDNESS = "TIREDNESS";
    public static final String LOSS_OF_TASTE = "LOSS_OF_TASTE";
    public static final String LOSS_OF_SMELL = "LOSS_OF_SMELL";
    public static final String SORE_THROAT = "SORE_THROAT";
    public static final String HEADACHE = "HEADACHE";
    public static final String CHEST_PAIN = "CHEST_PAIN";
    public static final String DIFFICULT_BREATHING = "DIFFICULT_BREATHING";
    public static final String BREATH_SHORTNESS = "BREATH_SHORTNESS";

    public static final String PATIENTS = "PATIENTS";
    public static final String PATIENT = "PATIENT";

    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_MSG = "RESPONSE_MSG";

    /*Error Codes*/
    public static final String APPROVED = "00";
    public static final String ERR_INVALID_CREDENTIALS = "ERR_001";
    public static final String ERR_PATIENT_PHONE_EXISTS = "ERR_002";
    public static final String ERR_PATIENT_ID_EXISTS = "ERR_003";
    public static final String ERR_NO_PATIENT_FOUND = "ERR_004";
    public static final String ERR_NO_TRIAGE_PATIENT = "ERR_005";
    public static final String ERR_PROCESSING_ERROR = "ERR_999";

}

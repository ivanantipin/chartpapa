import at.feedapi.ActiveTickServerAPI
import at.feedapi.ATCallback
import at.feedapi.ATCallback.ATLoginResponseCallback
import at.feedapi.ATCallback.ATServerTimeUpdateCallback
import at.feedapi.ATCallback.ATRequestTimeoutCallback
import at.feedapi.ATCallback.ATSessionStatusChangeCallback
import at.feedapi.ATCallback.ATOutputMessageCallback
import at.feedapi.Helpers
import at.feedapi.Session
import at.shared.ATServerAPIDefines.ATGUID
import at.shared.ATServerAPIDefines.ATLOGIN_RESPONSE
import at.shared.ATServerAPIDefines
import at.shared.ATServerAPIDefines.SYSTEMTIME
import at.shared.ATServerAPIDefines.ATSessionStatusType
import at.utils.jlib.Errors
import at.utils.jlib.OutputMessage

class APISession(var m_serverapi: ActiveTickServerAPI) : ATCallback(), ATLoginResponseCallback,
    ATServerTimeUpdateCallback, ATRequestTimeoutCallback, ATSessionStatusChangeCallback, ATOutputMessageCallback {
    var m_session: Session? = null
    var m_requestor: Requestor? = null
    var m_streamer: Streamer? = null
    var m_lastRequest: Long = 0
    var m_userid: String? = null
    var m_password: String? = null
    var m_apiKey: ATGUID? = null
    fun GetServerAPI(): ActiveTickServerAPI {
        return m_serverapi
    }

    fun GetSession(): Session {
        return m_session!!
    }

    fun GetStreamer(): Streamer {
        return m_streamer!!
    }

    fun GetRequestor(): Requestor {
        return m_requestor!!
    }

    fun Init(apiKey: ATGUID?, serverHostname: String?, serverPort: Int, userId: String?, password: String?): Boolean {
        if (m_session != null) m_serverapi.ATShutdownSession(m_session)
        m_session = m_serverapi.ATCreateSession()
        m_streamer = Streamer(this)
        m_requestor = Requestor(this, m_streamer)
        m_userid = userId
        m_password = password
        m_apiKey = apiKey
        val rc = m_serverapi.ATSetAPIKey(m_session, m_apiKey)
        m_session!!.SetServerTimeUpdateCallback(this)
        m_session!!.SetOutputMessageCallback(this)
        var initrc = false
        if (rc == Errors.ERROR_SUCCESS.toLong()) initrc =
            m_serverapi.ATInitSession(m_session, serverHostname, serverHostname, serverPort, this)
        println(m_serverapi.GetAPIVersionInformation())
        println("--------------------------------------------------------------------")
        return initrc
    }

    fun UnInit(): Boolean {
        if (m_session != null) {
            m_serverapi.ATShutdownSession(m_session)
            m_session = null
        }
        return true
    }

    //ATLoginResponseCallback
    override fun process(session: Session, requestId: Long, response: ATLOGIN_RESPONSE) {
        var strLoginResponseType = ""
        strLoginResponseType = when (response.loginResponse.m_atLoginResponseType) {
            ATServerAPIDefines.ATLoginResponseType.LoginResponseSuccess -> "LoginResponseSuccess"
            ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidUserid -> "LoginResponseInvalidUserid"
            ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidPassword -> "LoginResponseInvalidPassword"
            ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidRequest -> "LoginResponseInvalidRequest"
            ATServerAPIDefines.ATLoginResponseType.LoginResponseLoginDenied -> "LoginResponseLoginDenied"
            ATServerAPIDefines.ATLoginResponseType.LoginResponseServerError -> "LoginResponseServerError"
            else -> "unknown"
        }
        println("RECV $requestId: Login Response [$strLoginResponseType]")
    }

    //ATServerTimeUpdateCallback
    override fun process(serverTime: SYSTEMTIME) {}

    //ATRequestTimeoutCallback
    override fun process(origRequest: Long) {
        println("($origRequest): Request timed-out\n")
    }

    //ATSessionStatusChangeCallback
    override fun process(session: Session, type: ATSessionStatusType) {
        val strStatusType = when (type.m_atSessionStatusType) {
            ATSessionStatusType.SessionStatusConnected -> "SessionStatusConnected"
            ATSessionStatusType.SessionStatusDisconnected -> "SessionStatusDisconnected"
            ATSessionStatusType.SessionStatusDisconnectedDuplicateLogin -> "SessionStatusDisconnectedDuplicateLogin"
            else -> {
                ""
            }
        }
        println("RECV Status change [$strStatusType]")

        //if we are connected to the server, send a login request
        if (type.m_atSessionStatusType == ATSessionStatusType.SessionStatusConnected) {
            m_lastRequest = m_serverapi.ATCreateLoginRequest(session, m_userid, m_password, this)
            val rc = m_serverapi.ATSendRequest(session, m_lastRequest, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT, this)
            println(
                "SEND ($m_lastRequest): Login request [$m_userid] (rc = " + Helpers.ConvertBooleanToByte(rc)
                    .toChar() + ")"
            )
        }
    }

    override fun process(outputMessage: OutputMessage) {
        println(outputMessage.GetMessage())
    }
}
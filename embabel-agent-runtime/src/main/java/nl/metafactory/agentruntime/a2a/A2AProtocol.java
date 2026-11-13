package nl.metafactory.agentruntime.a2a;

public final class A2AProtocol {

    public static final String PROTOCOL_VERSION = "0.3.0";
    public static final String TRANSPORT = "JSONRPC";

    public static final String MESSAGE_SEND = "message/send";
    public static final String MESSAGE_STREAM = "message/stream";
    public static final String TASKS_GET = "tasks/get";
    public static final String TASKS_CANCEL = "tasks/cancel";

    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    public static final int TASK_NOT_FOUND = -32001;
    public static final int UNSUPPORTED_OPERATION = -32004;
    public static final int CONTENT_TYPE_NOT_SUPPORTED = -32005;

    public static final int MAX_MESSAGE_CHARS = 32 * 1024;

    private A2AProtocol() {
    }
}

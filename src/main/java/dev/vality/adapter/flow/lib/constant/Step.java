package dev.vality.adapter.flow.lib.constant;

public enum Step {

    PRE_AUTH,
    AUTH,
    PAY,
    GENERATE_TOKEN,
    FINISH_THREE_DS_V1,
    FINISH_THREE_DS_V2,
    CANCEL,
    REFUND,
    CHECK_STATUS,
    CHECK_STATUS_3DS_V2,
    CHECK_NEED_3DS_V2,
    CAPTURE,
    DO_NOTHING,
    RECURRING_GENERATE_TOKEN,
    RECURRING_PAY,
    HANDLE_REDIRECT,
    HANDLE_CALLBACK

}

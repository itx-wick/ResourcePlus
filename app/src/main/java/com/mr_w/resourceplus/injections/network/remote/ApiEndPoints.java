package com.mr_w.resourceplus.injections.network.remote;

import static com.mr_w.resourceplus.activities.splash_activity.SplashActivity.baseAPIURL;

public final class ApiEndPoints {

    public static final String ENDPOINT_SIGN_IN = baseAPIURL() + "api/users/sign_in";
    public static final String ENDPOINT_SIGN_UP = baseAPIURL() + "api/users/sign_up";
    public static final String ENDPOINT_UPDATE_PROFILE = baseAPIURL() + "api/users/update_profile";
    public static final String ENDPOINT_FILE_UPLOAD = baseAPIURL() + "api/users/upload/avatar";

    public static final String ENDPOINT_CONTACTS_VALIDATION = baseAPIURL() + "api/contact/validation";

    public static final String ENDPOINT_CONVERSATION_LIST = baseAPIURL() + "api/conversation/conversation_list";
    public static final String ENDPOINT_CONVERSATION_CREATE = baseAPIURL() + "api/conversation/conversation_create";
    public static final String ENDPOINT_CONVERSATION_UPDATE = baseAPIURL() + "api/conversation/update_credentials";
    public static final String ENDPOINT_CONVERSATION_ADD_MEMBER = baseAPIURL() + "api/conversation/add_members";
    public static final String ENDPOINT_CONVERSATION_REMOVE_MEMBER = baseAPIURL() + "api/conversation/remove_user";
    public static final String ENDPOINT_MESSAGES_LIST = baseAPIURL() + "api/messages/get_messages";
    public static final String ENDPOINT_MESSAGES_CREATE = baseAPIURL() + "api/messages/create_messages";

    private ApiEndPoints() {
        // This class is not publicly instantiable
    }
}

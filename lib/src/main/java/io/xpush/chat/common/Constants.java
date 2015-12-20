package io.xpush.chat.common;

import com.squareup.okhttp.MediaType;

public class Constants {
    public static final int REQUEST_SIGNUP = 100;

    public static final int REQUEST_EDIT_IMAGE = 111;
    public static final int REQUEST_EDIT_NICKNAME = 112;
    public static final int REQUEST_EDIT_STATUS_MESSAGE = 113;

    public static final int REQUEST_INVITE_USER = 201;

    public static final String CRASH_TEXT = "oooops ! I crashed, but a report has been sent to my developer to help fix the issue !";
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
}

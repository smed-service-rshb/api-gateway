package ru.softlab.efr.infrastructure.apigateway;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang.StringUtils;
import ru.softlab.efr.services.auth.Right;
import ru.softlab.efr.services.auth.exchange.BadEntityRs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 * @author krenev
 */
public class Utils {

    public static final String DOMAIN_USER_CORRECT_LOGIN = "it-search-first@go.rshbank.ru";

    public static final String USER_EMPTY_ROLE_LOGIN = "it-empty-role@go.rshbank.ru";
    public static final String USER_WRONG_ROLE_LOGIN = "it-wrong-role@go.rshbank.ru";
    public static final String USER_WRONG_DEPARTMENT_LOGIN = "it-wrong-department@go.rshbank.ru";
    public static final String USER_BLOCKED_LOGIN = "it-blocked@go.rshbank.ru";

    public static final String CORRECT_LOGIN = "gogo";
    public static final String INCORRECT_LOGIN = "INCORRECT_LOGIN";
    public static final String CORRECT_PASSWORD = "ia4uV1EeKait";
    public static final String INCORRECT_PASSWORD = "INCORRECT_PASSWORD";
    public static final String EXCEPTION_LOGIN = "EXCEPTION_LOGIN";


    private static final String LOGIN_TAG = "login";
    private static final String PASSWORD_TAG = "passwd";
    private static final String ERROR_CODE_TAG = "errorCode";
    private static final String ERROR_MESSAGE_TAG = "errorMessage";
    private static final String TYPE_TAG = "type";
    private static final String MESSAGE_TAG = "message";

    private static final String USER_DATA_TAG_NAME = "user";
    private static final String AUTHORIZATION_DATA_TAG_NAME = "rights";


    public static String createLoginRequest(String login, String password) throws IOException {
        StringWriter stringWriter = new StringWriter();
        new GsonBuilder().create().newJsonWriter(stringWriter)
                .beginObject()
                .name(LOGIN_TAG).value(login)
                .name(PASSWORD_TAG).value(password)
                .endObject();
        return stringWriter.toString();
    }

    public static void checkErrorResponse(String response, String errorCode, String errorMessage) throws IOException {
        JsonReader reader = new GsonBuilder().create().newJsonReader(new StringReader(response));
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (ERROR_CODE_TAG.equals(name)) {
                assertThat("errorCode", reader.nextString(), is(errorCode));
            } else if (ERROR_MESSAGE_TAG.equals(name)) {
                assertThat("errorMessage", reader.nextString(), is(errorMessage));
            } else {
                fail("Неожиданный тег " + name);
            }
        }
        reader.endObject();
    }

    public static void checkNotAcceptableErrorResponse(String response, BadEntityRs.Type type) throws IOException {
        JsonReader reader = new GsonBuilder().create().newJsonReader(new StringReader(response));
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (TYPE_TAG.equals(name)) {
                assertThat(TYPE_TAG, reader.nextString(), is(type.name()));
            } else if (MESSAGE_TAG.equals(name)) {
                assertThat(MESSAGE_TAG, reader.nextString(), is(notNullValue()));
            } else {
                fail("Неожиданный тег " + name);
            }
        }
        reader.endObject();
    }

    public static void checkSessionResponse(String response) throws IOException {
        Set<String> requiredTags = new HashSet<>();
        requiredTags.add(USER_DATA_TAG_NAME);
        requiredTags.add(AUTHORIZATION_DATA_TAG_NAME);

        System.out.println(response);
        JsonReader reader = new GsonBuilder().create().newJsonReader(new StringReader(response));
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (USER_DATA_TAG_NAME.equals(name)) {
                checkUser(reader);
                requiredTags.remove(name);
            } else if (AUTHORIZATION_DATA_TAG_NAME.equals(name)) {
                checkRights(reader);
                requiredTags.remove(name);
            } else {
                fail("Неожиданный тег " + name);
            }
        }
        reader.endObject();

        if (requiredTags.size() != 0) {
            fail("В корневом объекте ответа ожидаются теги [" + StringUtils.join(requiredTags, ", ") + "]");
        }
    }


    private static void checkUser(JsonReader reader) throws IOException {
        Set<String> userTags = new HashSet<>();
        userTags.add("surname");
        userTags.add("name");
        userTags.add("middleName");
        userTags.add("mobilePhone");
        userTags.add("email");
        userTags.add("position");
        userTags.add("personnelNumber");
        userTags.add("office");
        userTags.add("branch");

        Set<String> requiredTags = new HashSet<>();
        requiredTags.addAll(userTags);

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (userTags.contains(name)) {
                assertNotNull(name, reader.nextString());
                requiredTags.remove(name);
            } else {
                fail("Неожиданный тег " + name);
            }
        }
        reader.endObject();

        if (requiredTags.size() != 0) {
            fail("В данных по клиенту ожидаются теги [" + StringUtils.join(requiredTags, ", ") + "]");
        }
    }

    private static void checkRights(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            checkRight(reader);
        }
        reader.endArray();
    }

    private static void checkRight(JsonReader reader) throws IOException {
        String rightValue = reader.nextString();
        assertNotNull(rightValue);
        assertNotNull(Right.valueOf(rightValue));
    }

}

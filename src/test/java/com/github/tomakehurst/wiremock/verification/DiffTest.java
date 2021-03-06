package com.github.tomakehurst.wiremock.verification;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.verification.Diff.junitStyleDiffMessage;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DiffTest {

    @Test
    public void correctlyRendersJUnitStyleDiffMessage() {
        String diff = Diff.junitStyleDiffMessage("expected", "actual");

        assertThat(diff, is(" expected:<\nexpected> but was:<\nactual>"));
    }

    @Test
    public void showsDiffForNonMatchingRequestMethod() {
        Diff diff = new Diff(
            newRequestPattern(GET, urlEqualTo("/thing"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "GET\n" +
                "/thing\n",
                "POST\n" +
                "/thing\n")
        ));
    }

    @Test
    public void showsDiffForUrlEqualTo() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/expected")).build(),
            mockRequest().url("/actual")
            );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/expected\n",

                "ANY\n" +
                "/actual\n")
        ));
    }

    @Test
    public void showsDiffForUrlPathMatching() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlPathMatching("/expected/.*")).build(),
            mockRequest().url("/actual")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/expected/.*\n",

                "ANY\n" +
                "/actual\n")
        ));
    }

    @Test
    public void showsDiffsForSingleNonMatchingHeaderAndMatchingHeader() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-My-Header", equalTo("expected"))
            .build(),
            mockRequest().url("/thing")
                .header("Content-Type", "application/json")
                .header("X-My-Header", "actual")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "Content-Type: application/json\n" +
                "X-My-Header: expected\n",

                "ANY\n" +
                "/thing\n" +
                "Content-Type: application/json\n" +
                "X-My-Header: actual\n"
            )
        ));
    }

    @Test
    public void showsDiffWhenRequestHeaderIsAbsent() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("X-My-Header", equalTo("expected"))
                .build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "X-My-Header: expected\n",

                "ANY\n" +
                "/thing\n" +
                "\n")
        ));
    }

    @Test
    public void showsHeaders() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("X-My-Header", equalTo("expected"))
                .build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "X-My-Header: expected\n",

                "ANY\n" +
                "/thing\n\n")
        ));
    }

    @Test
    public void showsRequestBody() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(equalToJson(
                    "{\n" +
                    "    \"outer\": {\n" +
                    "        \"inner\": {\n" +
                    "            \"thing\": 1\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"))
                .build(),
            mockRequest().url("/thing").body(
                    "{\n" +
                    "    \"outer\": {}\n" +
                    "}"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "{" + System.lineSeparator() +
                "  \"outer\" : {" + System.lineSeparator() +
                "    \"inner\" : {" + System.lineSeparator() +
                "      \"thing\" : 1" + System.lineSeparator() +
                "    }" + System.lineSeparator() +
                "  }" + System.lineSeparator() +
                "}",

                "ANY\n" +
                "/thing\n" +
                "{" + System.lineSeparator() +
                "  \"outer\" : { }" + System.lineSeparator() +
                "}")
        ));
    }

    @Test
    public void prettyPrintsJsonRequestBody() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(equalToJson(
                    "{\"outer\": {\"inner:\": {\"thing\": 1}}}"))
                .build(),
            mockRequest().url("/thing").body(
                "{\"outer\": {}}"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "{" + System.lineSeparator() +
                "  \"outer\" : {" + System.lineSeparator() +
                "    \"inner:\" : {" + System.lineSeparator() +
                "      \"thing\" : 1" + System.lineSeparator() +
                "    }" + System.lineSeparator() +
                "  }" + System.lineSeparator() +
                "}",

                "ANY\n" +
                "/thing\n" +
                "{" + System.lineSeparator() +
                "  \"outer\" : { }" + System.lineSeparator() +
                "}")
        ));
    }

    @Test
    public void showsJsonPathExpectations() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(matchingJsonPath("@.notfound"))
                .withRequestBody(matchingJsonPath("@.nothereeither"))
                .build(),
            mockRequest().url("/thing").body(
                "{\n" +
                "    \"outer\": {\n" +
                "        \"inner:\": {\n" +
                "            \"thing\": 1\n" +
                "        }\n" +
                "    }\n" +
                "}"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "@.notfound\n" +
                "@.nothereeither",

                "ANY\n" +
                "/thing\n" +
                "{\n" +
                "    \"outer\": {\n" +
                "        \"inner:\": {\n" +
                "            \"thing\": 1\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "{\n" +
                "    \"outer\": {\n" +
                "        \"inner:\": {\n" +
                "            \"thing\": 1\n" +
                "        }\n" +
                "    }\n" +
                "}")
        ));
    }

    @Test
    public void prettyPrintsXml() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(equalToXml(
                    "<my-elements><one attr-one=\"1111\" /><two /><three /></my-elements>"))
                .build(),
            mockRequest().url("/thing").body(
                "<my-elements><one attr-one=\"2222\" /><two /><three /></my-elements>"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "<my-elements>" + System.lineSeparator() +
                "  <one attr-one=\"1111\"/>" + System.lineSeparator() +
                "  <two/>" + System.lineSeparator() +
                "  <three/>" + System.lineSeparator() +
                "</my-elements>" + System.lineSeparator(),

                "ANY\n" +
                "/thing\n" +
                "<my-elements>" + System.lineSeparator() +
                "  <one attr-one=\"2222\"/>" + System.lineSeparator() +
                "  <two/>" + System.lineSeparator() +
                "  <three/>" + System.lineSeparator() +
                "</my-elements>" + System.lineSeparator())
        ));
    }

    @Test
    public void showsCookiesInDiffWhenNotMatching() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withCookie("my_cookie", equalTo("expected-cookie"))
                .build(),
            mockRequest().url("/thing")
                .cookie("my_cookie", "actual-cookie")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "Cookie: my_cookie=expected-cookie\n",

                "ANY\n" +
                "/thing\n" +
                "Cookie: my_cookie=actual-cookie\n"
            )
        ));
    }

    @Test
    public void showsCookiesInDiffAbsentFromRequest() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withCookie("my_cookie", equalTo("expected-cookie"))
                .build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "Cookie: my_cookie=expected-cookie\n",

                "ANY\n" +
                "/thing\n\n"
            )
        ));
    }
}

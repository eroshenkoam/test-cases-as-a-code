package io.github.erosenkoam.tcaac;

import com.codepine.api.testrail.TestRail;
import com.codepine.api.testrail.model.Case;
import com.codepine.api.testrail.model.CaseField;
import com.codepine.api.testrail.model.Field;
import com.codepine.api.testrail.model.Result;
import com.codepine.api.testrail.model.ResultField;
import com.codepine.api.testrail.model.Run;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestCaseExportTest {

    private static final String ENDPOINT = "";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private static final int PROJECT_ID = 1;
    private static final int SECTION_ID = 1;

    private static final String RUN_NAME = "New run from Allure";

    private static final String CF_FULLNAME = "fullname";
    private static final String CF_STEPS = "steps_separated";

    private static final Result STATUS_PASSED = new Result().setStatusId(1);
    private static final Result STATUS_RETEST = new Result().setStatusId(4);

    private final TestRail testRail = TestRail.builder(ENDPOINT, USERNAME, PASSWORD).build();

    private AllureTestResult testResult;

    @BeforeEach
    public void initTestCase() {
        testResult = new AllureTestResult();
        testResult.setName("Проверка авторизации");
        testResult.setManual(true);
        testResult.setFullName("io.github.erosenkoam.tcaac.GithubTest.test");
        testResult.setSteps(Arrays.asList(
                "Открываем главную страницу",
                "Авторизуемся как пользователь `%s`",
                "Проверяем что авторизованы правильно"
        ));
    }

    @Test
    public void exportTestCase() {
        final List<CaseField> customCaseFields = testRail.caseFields().list().execute();
        final List<Case> response = testRail.cases().list(PROJECT_ID, customCaseFields).execute();

        final Optional<Case> possible = response.stream()
                .filter(tc -> fullNameEquals(tc, testResult.getFullName()))
                .findAny();
        if (possible.isPresent()) {
            final Case existing = possible.get();
            existing.setTitle(testResult.getName());
            existing.getCustomFields().put(CF_STEPS, convert(testResult.getSteps()));
            testRail.cases().update(existing, customCaseFields).execute();
        } else {
            final Case toCreate = new Case()
                    .setTitle(testResult.getName());
            final Map<String, Object> fields = new HashMap<>();
            fields.put(CF_FULLNAME, testResult.getFullName());
            fields.put(CF_STEPS, convert(testResult.getSteps()));
            toCreate.setCustomFields(fields);
            testRail.cases().add(SECTION_ID, toCreate, customCaseFields).execute();
        }
    }

    @Test
    public void exportTestRun() {
        final List<CaseField> customCaseFields = testRail.caseFields().list().execute();
        final List<Case> response = testRail.cases().list(PROJECT_ID, customCaseFields).execute();
        final Optional<Case> possible = response.stream()
                .filter(tc -> fullNameEquals(tc, testResult.getFullName()))
                .findAny();


        if (possible.isPresent()) {
            final Case existing = possible.get();
            final Run toCreateRun = new Run().setName(RUN_NAME)
                    .setCaseIds(Arrays.asList(existing.getId()))
                    .setIncludeAll(false);
            final Run createdRun = testRail.runs().add(PROJECT_ID, toCreateRun).execute();
            final List<ResultField> customResultFields = testRail.resultFields().list().execute();
            final Result result = testResult.isManual() ? STATUS_RETEST : STATUS_PASSED;
            testRail.results().addForCase(
                    createdRun.getId(), existing.getId(), result, customResultFields
            ).execute();
        }
    }

    private boolean fullNameEquals(final Case tc, final String value) {
        String fullName = tc.getCustomField(CF_FULLNAME);
        return Objects.nonNull(fullName) && fullName.equalsIgnoreCase(value);
    }

    private List<Field.Step> convert(final List<String> steps) {
        return steps.stream()
                .map(s -> new Field.Step().setContent(s))
                .collect(Collectors.toList());
    }

}

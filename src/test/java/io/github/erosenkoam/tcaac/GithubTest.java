package io.github.erosenkoam.tcaac;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eroshenkoam.tcaac.Manual.resource;
import static io.qameta.allure.Allure.addAttachment;
import static io.qameta.allure.Allure.step;
import static java.lang.String.format;

public class GithubTest {

    private final String USERNAME = "eroshenkoam";
    private final String PASSWORD = "123123123123123";

    @Test
    @DisplayName("Проверка авторизации")
    public void testAuth() {
        step("Открываем главную страницу", step -> {
            step.parameter("url", "https://google.com");
        });
        step(format("Авторизуемся как пользователь `%s`", USERNAME), step -> {
            step(format("Вводим логин `%s`", USERNAME));
            step(format("Вводим пароль `%s`", PASSWORD));
            step(format("Нажимаем кнопку %s", "Войти"));
        });
        step("Проверяем что авторизованы правильно", () -> {
            step(format("Ожимаем имя пользователя `%s`", USERNAME));
            step("Ожидаем что аватарка подтянется из google", () -> {
                addAttachment("Аватар", "image/jpg", resource("avatar.jpg"));
            });
        });
    }

}

package be.cegeka.codepop.controllers;

import be.cegeka.codepop.AbstractIntegrationTest;
import org.junit.Test;
import starter.controllers.DemoController;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class DemoControllerIntegrationTest extends AbstractIntegrationTest {

    @Inject
    private DemoController controller;

    @Test
    public void test() {
        String result = controller.hello("Frans");

        assertThat(result).isEqualTo("Hello, Frans!");
    }
}
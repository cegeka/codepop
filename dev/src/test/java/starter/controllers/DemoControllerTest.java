package starter.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DemoControllerTest {

    public static final String NAME = "someName";
    public static final String LAST_NAME = "lastName";
    public static final String EMAIL = "test@user.be";

    @InjectMocks
    private DemoController demoController;

    @Test
    public void testName() throws Exception {
        String result = demoController.hello(NAME);

        assertThat(result).isEqualTo("Hello, someName!");
    }
}
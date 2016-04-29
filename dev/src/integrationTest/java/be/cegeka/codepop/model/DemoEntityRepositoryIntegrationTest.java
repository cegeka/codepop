package be.cegeka.codepop.model;

import be.cegeka.codepop.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import starter.model.DemoEntity;
import starter.model.DemoEntityRepository;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class DemoEntityRepositoryIntegrationTest extends AbstractIntegrationTest {
    @Inject
    private DemoEntityRepository repository;

    private DemoEntity insertedEntity;

    @Before
    public void clearRepository() {
        repository.deleteAll();

        DemoEntity entity = new DemoEntity("email", "first", "last");
        insertedEntity = repository.insert(entity);
    }

    @Test
    public void canGetById() {
        DemoEntity actual = repository.findOne(insertedEntity.getId().toHexString());

        assertEqualToInserted(actual);
    }

    @Test
    public void canFindByFirstName() {
        DemoEntity actual = repository.findByFirstName(insertedEntity.getFirstName());

        assertEqualToInserted(actual);
    }

    private void assertEqualToInserted(DemoEntity actual) {
        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo(insertedEntity.getEmail());
        assertThat(actual.getFirstName()).isEqualTo(insertedEntity.getFirstName());
        assertThat(actual.getLastName()).isEqualTo(insertedEntity.getLastName());
    }
}

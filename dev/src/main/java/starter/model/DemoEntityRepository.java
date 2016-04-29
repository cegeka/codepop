package starter.model;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DemoEntityRepository extends MongoRepository<DemoEntity, String> {

     DemoEntity findByFirstName(String firstName);

}

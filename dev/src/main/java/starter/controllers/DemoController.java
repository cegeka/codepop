package starter.controllers;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starter.model.DemoEntity;
import starter.model.DemoEntityRepository;

import javax.inject.Inject;
import java.util.List;

@RestController
public class DemoController {

    @Inject
    private DemoEntityRepository demoEntityRepository;
    
    @RequestMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
    	return "Hello, " + name + "!";
    }

//    @RequestMapping("/livedemo")
    public String livedemo() {
        return "livedemo";
    }

    @RequestMapping("/add/{firstName}/{lastName}/{email}")
    public String add(@PathVariable String firstName, @PathVariable String lastName, @PathVariable String email) {
        DemoEntity newEntity = new DemoEntity(firstName, lastName, email);
        demoEntityRepository.insert(newEntity);
        return "Hi hackathon people, I added " + firstName + " to db.";
    }

    @RequestMapping("/getall")
    public List<DemoEntity> getAll() {
        List<DemoEntity> all = demoEntityRepository.findAll();
        return all;
    }

}

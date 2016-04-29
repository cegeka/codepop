package starter.controllers;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import starter.model.DemoEntity;
import starter.model.DemoEntityRepository;

import javax.inject.Inject;

@RestController
@RequestMapping("/control")
public class InsertController {
    
    @Inject
    private DemoEntityRepository demoEntityRepository;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public void insertItem(@RequestBody DemoEntity newEntity) {
        demoEntityRepository.insert(newEntity);
    }
}

package com.security.demo.controller;

import com.security.demo.config.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/stream")
public class NotificationController {

    private final Map<Integer, List<SseEmitter>>  emitters = new HashMap<>();

    @GetMapping("/notif/{matchid}")
    public SseEmitter stream(@PathVariable("matchid") Integer id) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout

        if(!emitters.containsKey(id) ){
            emitters.put(id,new CopyOnWriteArrayList<>());
        }
        emitters.get(id).add(emitter);
        emitter.onCompletion(() -> emitters.get(id).remove(emitter));
        emitter.onTimeout(() -> emitters.get(id).remove(emitter));
        emitter.onError((e) -> emitters.get(id).remove(emitter));

        return emitter;
    }

    // Call this when you want to notify frontend
    public void sendEvent(String message ,Integer matchid) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        if(emitters.containsKey(matchid)) {
            for (SseEmitter emitter : emitters.get(matchid)) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("refresh")
                            .data(message));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }

            emitters.get(matchid).removeAll(deadEmitters);
        }
    }
}
package bgaebalja.bsherpa.user.controller;

import static bgaebalja.bsherpa.validation.Valid.JOIN;
import static org.springframework.http.HttpStatus.CREATED;

import bgaebalja.bsherpa.user.domain.UserJoinRequest;
import bgaebalja.bsherpa.user.dto.request.UserSwaggerLoginRequest;
import bgaebalja.bsherpa.user.dto.response.UserSwaggerLoginResponse;
import bgaebalja.bsherpa.user.service.UserService;
import bgaebalja.bsherpa.validation.CustomValid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @CustomValid(schema = JOIN)
  @PostMapping("/join")
  public ResponseEntity<Map<String, String>> postJoin(@RequestBody UserJoinRequest userJoinRequest) {
      log.info("POST join request: {}", userJoinRequest);
      if (userJoinRequest.getRole().equals("STUDENT") && !userJoinRequest.getClazz().isEmpty() && !userJoinRequest.getGrade().isEmpty()) {
        userService.saveStudent(userJoinRequest);
      return ResponseEntity.status(CREATED).body(Map.of("SUCCESS","JOIN"));
      }
      userService.saveUser(userJoinRequest);
      return ResponseEntity.status(CREATED).body(Map.of("SUCCESS","JOIN"));
  }

  @PostMapping("/login/swagger")
  public ResponseEntity<UserSwaggerLoginResponse> postLoginSwagger(@RequestBody UserSwaggerLoginRequest userSwaggerLoginRequest){
    UserSwaggerLoginResponse response = userService.getUser(userSwaggerLoginRequest);
    return ResponseEntity.status(CREATED).body(response);
  }

}
package webserver.controler;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import webserver.model.api.Greeting;
import webserver.model.api.HelloMessage;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

    @GetMapping("/")
    public String readCookie(@CookieValue(value = "username", defaultValue = "Gland sec") String username) {

        return "Hey! My username is " + username;

    }

    @GetMapping("/set-cookie")
    public String setCookie(@RequestParam String name, HttpServletResponse response) {
        Cookie cookie = new Cookie("username", name);
        response.addCookie(cookie);
        return "ok";
    }
}
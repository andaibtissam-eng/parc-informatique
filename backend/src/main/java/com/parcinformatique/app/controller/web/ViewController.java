package com.parcinformatique.app.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({
        "/",
        "/login",
        "/register",
        "/forgot-password",
        "/reset-password",
        "/verify-email",
        "/dashboard",
        "/analytics",
        "/equipments",
        "/categories",
        "/users",
        "/assignments",
        "/returns",
        "/maintenance",
        "/maintenances",
        "/board",
        "/tickets",
        "/calendar",
        "/notifications",
        "/reports",
        "/history",
        "/profile",
        "/settings"
    })
    public String index() {
        return "forward:/index.html";
    }
}

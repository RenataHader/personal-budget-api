package com.example.budget.account;

import jakarta.validation.constraints.NotBlank;

public record AccountRequest (@NotBlank(message = "Account name must not be blank") String name){}

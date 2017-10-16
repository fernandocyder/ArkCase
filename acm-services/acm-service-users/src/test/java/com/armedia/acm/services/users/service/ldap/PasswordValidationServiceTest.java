package com.armedia.acm.services.users.service.ldap;

import com.armedia.acm.services.users.model.ldap.PasswordShouldMatchPattern;
import com.armedia.acm.services.users.model.ldap.PasswordShouldNotContainUserId;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PasswordValidationServiceTest
{
    private PasswordValidationService unit = new PasswordValidationService();

    @Before
    public void setUp()
    {
        /**
         * These are the patterns declared in `spring-library-user-service.xml`
         */
        PasswordShouldMatchPattern lowercaseCharRule = new PasswordShouldMatchPattern("^.*?[a-z].*$",
                "Password must contain at least one lowercase character");

        PasswordShouldMatchPattern uppercaseCharRule = new PasswordShouldMatchPattern("^.*?[A-Z].*$",
                "Password must contain at least one uppercase character");

        PasswordShouldMatchPattern digitRule = new PasswordShouldMatchPattern("^.*?[0-9].*$",
                "Password must contain at least one digit (0-9)");

        PasswordShouldMatchPattern specialCharRule = new PasswordShouldMatchPattern("^.*?[[~!@#$%^&*_+=`|\\(){}:;\"'<>,.?/-]].*$",
                "Password must contain at least one special character");

        PasswordShouldMatchPattern minLengthRule = new PasswordShouldMatchPattern("^.{7,}$",
                "Password must be of minimum length of 7.");

        unit.setPasswordRules(Arrays.asList(new PasswordShouldNotContainUserId(), lowercaseCharRule, uppercaseCharRule, digitRule,
                specialCharRule, minLengthRule));
    }

    @Test
    public void passwordIsValid()
    {
        List<String> errorMessages = unit.validate("ann-acm", "AcMd3v$");

        assertTrue(errorMessages.isEmpty());
    }

    @Test
    public void passwordIsTooShort()
    {
        List<String> errorMessages = unit.validate("ann-acm", "Ac3$");

        assertTrue(errorMessages.size() == 1);
        assertEquals("Password must be of minimum length of 7.", errorMessages.get(0));
    }

    @Test
    public void invalidPassword()
    {
        List<String> errorMessages = unit.validate("ann-acm", "ann-acm\\$\"'?");

        // password is less then 7 chars, no uppercase letter and contains userId
        assertTrue(errorMessages.size() == 3);
    }
}
